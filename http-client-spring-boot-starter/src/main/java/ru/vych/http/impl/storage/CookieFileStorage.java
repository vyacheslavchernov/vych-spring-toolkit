package ru.vych.http.impl.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import ru.vych.http.impl.entities.SerializedCookie;
import ru.vych.logger.impl.LogService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Файловое persistent хранилище HTTP-cookie.
 * <p>
 * Обеспечивает загрузку, сохранение и фильтрацию cookies по TTL.
 * Поддерживает асинхронное автосохранение после каждого изменения хранилища
 * и синхронное финальное сохранение при shutdown.
 * Запись в файл выполняется атомарно через временный файл + rename.
 * </p>
 *
 * @see SerializedCookie
 */
public final class CookieFileStorage {

    private static final int VERSION = 1;

    private final Path filePath;
    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    private final ObjectReader reader;
    private final LogService logService;
    private final String serviceCode;
    private final String clientUuid;

    /**
     * Создаёт файловое хранилище cookies.
     * <p>
     * Инициализирует Jackson ObjectMapper для сериализации/десериализации
     * cookie-файлов в JSON-формате.
     * </p>
     *
     * @param filePath    путь к файлу cookies
     * @param logService  сервис логирования
     * @param serviceCode код сервиса
     * @param clientUuid  уникальный идентификатор клиента
     */
    public CookieFileStorage(
            Path filePath,
            LogService logService,
            String serviceCode,
            String clientUuid
    ) {
        this.filePath = filePath;
        this.logService = logService;
        this.serviceCode = serviceCode;
        this.clientUuid = clientUuid;

        this.mapper = new ObjectMapper();
        this.writer = mapper.writerWithDefaultPrettyPrinter();
        this.reader = mapper.readerFor(CookieFileData.class);
    }

    /**
     * Загружает cookies из файла в хранилище.
     * <p>
     * Если файл не существует — возвращает пустое хранилище.
     * Если файл повреждён (невалидный JSON) — логирует ошибку и возвращает пустое хранилище.
     * Cookies с истёкшим TTL отфильтровываются и логируются в debug.
     * Session cookies (без maxAge) не добавляются в хранилище.
     * </p>
     *
     * @param cookieStore хранилище cookies в памяти, в которое загружаются куки
     */
    public void load(ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> cookieStore) {
        if (!Files.exists(filePath)) {
            return;
        }

        String content;
        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
            logError("Не удалось прочитать cookie-файл", filePath, e);
            return;
        }

        CookieFileData fileData;
        try {
            fileData = reader.readValue(content, CookieFileData.class);
        } catch (IOException e) {
            logError("Cookie-файл повреждён (невалидный JSON)", filePath, e);
            return;
        }

        if (fileData.hosts == null) {
            return;
        }

        for (Map.Entry<String, List<SerializedCookie>> entry : fileData.hosts.entrySet()) {
            String host = entry.getKey();
            List<SerializedCookie> serializedCookies = entry.getValue();

            if (serializedCookies == null) {
                continue;
            }

            for (SerializedCookie serialized : serializedCookies) {
                // Session cookies не сохраняются
                if (!serialized.hasTtl()) {
                    continue;
                }

                // Проверяем TTL по expires или maxAge
                if (!serialized.isNotExpired()) {
                    logDebug(host, "Cookie '" + serialized.getName() + "' истёк (TTL)", serialized);
                    continue;
                }

                // Восстанавливаем cookie
                java.net.HttpCookie cookie = serialized.toHttpCookie();
                CopyOnWriteArrayList<java.net.HttpCookie> cookies =
                        cookieStore.computeIfAbsent(host, k -> new CopyOnWriteArrayList<>());
                cookies.add(cookie);
            }
        }
    }

    /**
     * Выполняет асинхронное сохранение cookies в файл.
     * <p>
     * Сериализует хранилище в JSON, записывает во временный файл,
     * затем атомарно переименовывает в целевой файл.
     * Выполняется в фоновом потоке, не блокирует вызывающий поток.
     * При ошибке записи логирует ошибку, cookie-хранилище в памяти остаётся корректным.
     * </p>
     *
     * @param cookieStore хранилище cookies для сохранения
     */
    public void asyncSave(ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> cookieStore) {
        // Создаём снимок хранилища для безопасной асинхронной сериализации
        Map<String, List<SerializedCookie>> snapshot = takeSnapshot(cookieStore);

        // Асинхронная запись в фоновом потоке
        Thread saveThread = new Thread(() -> {
            try {
                saveSnapshot(snapshot);
            } catch (Exception e) {
                logError("Ошибка асинхронного сохранения cookie-файла", filePath, e);
            }
        }, "cookie-file-storage-save-" + clientUuid);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    /**
     * Выполняет синхронное финальное сохранение cookies в файл.
     * <p>
     * Используется при shutdown приложения. Запись выполняется синхронно,
     * с ожиданием завершения. При ошибке логирует в error.
     * </p>
     *
     * @param cookieStore хранилище cookies для сохранения
     */
    public void syncSave(ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> cookieStore) {
        Map<String, List<SerializedCookie>> snapshot = takeSnapshot(cookieStore);

        try {
            saveSnapshot(snapshot);
        } catch (Exception e) {
            logError("Ошибка финального сохранения cookie-файла при shutdown", filePath, e);
        }
    }

    /**
     * Создаёт снимок хранилища cookies для сериализации.
     * <p>
     * Конвертирует {@link java.net.HttpCookie} в {@link SerializedCookie},
     * устанавливая {@code createdAt} для cookies с maxAge > 0.
     * Session cookies (без maxAge) не включаются в снимок.
     * </p>
     *
     * @param cookieStore хранилище cookies
     * @return снимок хранилища для сериализации
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<SerializedCookie>> takeSnapshot(
            ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> cookieStore
    ) {
        Map<String, List<SerializedCookie>> snapshot = new HashMap<>();

        for (Map.Entry<String, CopyOnWriteArrayList<java.net.HttpCookie>> entry : cookieStore.entrySet()) {
            String host = entry.getKey();
            CopyOnWriteArrayList<java.net.HttpCookie> cookies = entry.getValue();

            List<SerializedCookie> serializedHostCookies = new ArrayList<>();

            for (java.net.HttpCookie cookie : cookies) {
                Long maxAgeObj = cookie.getMaxAge();
                // Session cookies (maxAge < 0) не сохраняются
                if (maxAgeObj == null || maxAgeObj < 0) {
                    continue;
                }

                // Cookies с maxAge == 0 считаются истёкшими
                if (maxAgeObj == 0) {
                    continue;
                }

                serializedHostCookies.add(SerializedCookie.fromHttpCookie(cookie));
            }

            if (!serializedHostCookies.isEmpty()) {
                snapshot.put(host, serializedHostCookies);
            }
        }

        return snapshot;
    }

    /**
     * Выполняет атомарную запись снимка в файл.
     * <p>
     * 1. Сериализует снимок в JSON.
     * 2. Записывает во временный файл с суффиксом {@code .tmp}.
     * 3. Атомарно переименовывает временный файл в целевой.
     * </p>
     *
     * @param snapshot снимок хранилища для записи
     * @throws IOException если не удалось записать файл
     */
    private void saveSnapshot(Map<String, List<SerializedCookie>> snapshot) throws IOException {
        // Создаём директорию если не существует
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        // Формируем данные файла
        CookieFileData fileData = new CookieFileData(VERSION, System.currentTimeMillis(), snapshot);

        // Сериализуем в JSON
        String json = writer.writeValueAsString(fileData);

        // Записываем во временный файл
        Path tmpFile = Path.of(filePath.toString() + ".tmp");
        Files.writeString(tmpFile, json);

        // Атомарно переименовываем
        Files.move(tmpFile, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Логирует ошибку сохранения/чтения cookie-файла.
     *
     * @param message  сообщение об ошибке
     * @param filePath путь к файлу
     * @param cause    причина ошибки
     */
    private void logError(String message, Path filePath, Throwable cause) {
        if (logService != null) {
            logService.error(serviceCode, clientUuid, message + ": " + filePath, cause);
        }
    }

    /**
     * Логирует отладочное сообщение об истёкшем cookie.
     *
     * @param host       хост cookie
     * @param message    сообщение
     * @param serialized сериализованный cookie
     */
    private void logDebug(String host, String message, SerializedCookie serialized) {
        if (logService != null) {
            logService.debug(serviceCode, clientUuid,
                    message + " [host=" + host + ", name=" + serialized.getName() + "]", serialized);
        }
    }

    /**
     * Внутренний класс данных cookie-файла.
     * <p>
     * Структура JSON:
     * </p>
     * <pre>
     * {
     *   "version": 1,
     *   "savedAt": 1728000001000,
     *   "hosts": {
     *     "api.example.com": [...serialized cookies...]
     *   }
     * }
     * </pre>
     */
    static final class CookieFileData {
        final int version;
        final long savedAt;
        final Map<String, List<SerializedCookie>> hosts;

        @JsonCreator
        CookieFileData(
                @JsonProperty("version") int version,
                @JsonProperty("savedAt") long savedAt,
                @JsonProperty("hosts") Map<String, List<SerializedCookie>> hosts
        ) {
            this.version = version;
            this.savedAt = savedAt;
            this.hosts = hosts != null ? hosts : Collections.emptyMap();
        }

        public int getVersion() {
            return version;
        }

        public long getSavedAt() {
            return savedAt;
        }

        public Map<String, List<SerializedCookie>> getHosts() {
            return hosts;
        }
    }
}
