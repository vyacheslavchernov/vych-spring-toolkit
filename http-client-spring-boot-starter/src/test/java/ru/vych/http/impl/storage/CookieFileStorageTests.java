package ru.vych.http.impl.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.logger.impl.LogService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты для файлового persistent хранилища cookies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты CookieFileStorage")
class CookieFileStorageTests {

    @TempDir
    Path tempDir;

    @Mock
    private LogService logService;

    private Path cookiesFile;
    private CookieFileStorage storage;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> cookieStore;

    @BeforeEach
    void setUp() {
        cookiesFile = tempDir.resolve("TestService-hostname.cookies");
        storage = new CookieFileStorage(cookiesFile, logService, "TestService", "test-uuid");
        cookieStore = new ConcurrentHashMap<>();
    }

    /**
     * Проверяет, что загрузка из несуществующего файла возвращает пустое хранилище.
     */
    @Test
    @DisplayName("Загрузка из несуществующего файла")
    void loadNonExistentFile() {
        storage.load(cookieStore);

        assertThat(cookieStore)
                .describedAs("Хранилище должно быть пустым")
                .isEmpty();
    }

    /**
     * Проверяет, что cookies сохраняются в файл и загружаются обратно.
     */
    @Test
    @DisplayName("Сохранение и загрузка cookies")
    void saveAndLoadCookies() {
        // Добавляем cookie с maxAge > 0
        java.net.HttpCookie cookie = new java.net.HttpCookie("session", "abc123");
        cookie.setMaxAge(3600L);
        cookieStore.computeIfAbsent("example.com", k -> new CopyOnWriteArrayList<>()).add(cookie);

        // Сохраняем синхронно
        storage.syncSave(cookieStore);

        // Проверяем, что файл создан
        assertThat(Files.exists(cookiesFile))
                .describedAs("Cookie-файл должен быть создан")
                .isTrue();

        // Очищаем хранилище
        cookieStore.clear();

        // Загружаем из файла
        storage.load(cookieStore);

        // Проверяем, что cookie загружен
        assertThat(cookieStore)
                .describedAs("Хранилище должно содержать загруженный cookie")
                .hasSize(1);
        assertThat(cookieStore.get("example.com"))
                .hasSize(1)
                .extracting(java.net.HttpCookie::getName)
                .containsExactly("session");
    }

    /**
     * Проверяет, что асинхронное сохранение работает без исключений.
     */
    @Test
    @DisplayName("Асинхронное сохранение")
    void asyncSave() throws InterruptedException {
        java.net.HttpCookie cookie = new java.net.HttpCookie("token", "xyz");
        cookie.setMaxAge(1800L);
        cookieStore.computeIfAbsent("api.example.com", k -> new CopyOnWriteArrayList<>()).add(cookie);

        // Асинхронное сохранение
        assertThatCode(() -> storage.asyncSave(cookieStore))
                .describedAs("asyncSave не должен бросать исключения")
                .doesNotThrowAnyException();

        // Ждём завершения фонового потока
        Thread.sleep(1000);

        // Проверяем, что файл создан
        assertThat(Files.exists(cookiesFile))
                .describedAs("Файл должен быть создан после asyncSave")
                .isTrue();
    }

    /**
     * Проверяет, что повреждённый JSON-файл игнорируется.
     */
    @Test
    @DisplayName("Повреждённый cookie-файл игнорируется")
    void corruptedFileIgnored() throws IOException {
        // Создаём повреждённый файл
        Files.writeString(cookiesFile, "not valid json {{{");

        // Загрузка не должна бросить исключение
        assertThatCode(() -> storage.load(cookieStore))
                .describedAs("Загрузка повреждённого файла не должна бросать исключение")
                .doesNotThrowAnyException();

        assertThat(cookieStore)
                .describedAs("Хранилище должно быть пустым")
                .isEmpty();
    }

    /**
     * Проверяет, что session cookies (maxAge < 0) не сохраняются в файл.
     */
    @Test
    @DisplayName("Session cookies не сохраняются в файл")
    void sessionCookiesNotSaved() {
        // Session cookie (maxAge = -1 по умолчанию)
        java.net.HttpCookie sessionCookie = new java.net.HttpCookie("session_id", "xyz");

        // Persistent cookie (maxAge > 0)
        java.net.HttpCookie persistentCookie = new java.net.HttpCookie("auth_token", "abc");
        persistentCookie.setMaxAge(7200L);

        CopyOnWriteArrayList<java.net.HttpCookie> cookies = new CopyOnWriteArrayList<>();
        cookies.add(sessionCookie);
        cookies.add(persistentCookie);
        cookieStore.put("example.com", cookies);

        storage.syncSave(cookieStore);

        // Загружаем в новое хранилище
        ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> loadedStore = new ConcurrentHashMap<>();
        storage.load(loadedStore);

        // Должен быть только persistent cookie
        assertThat(loadedStore.get("example.com"))
                .hasSize(1)
                .extracting(java.net.HttpCookie::getName)
                .containsExactly("auth_token");
    }

    /**
     * Проверяет, что cookies с валидным TTL загружаются корректно.
     */
    @Test
    @DisplayName("Cookies с валидным TTL загружаются")
    void validCookiesLoaded() {
        java.net.HttpCookie cookie = new java.net.HttpCookie("valid", "notExpired");
        cookie.setMaxAge(86400L); // 24 часа
        cookieStore.computeIfAbsent("cdn.example.com", k -> new CopyOnWriteArrayList<>()).add(cookie);

        storage.syncSave(cookieStore);

        ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> loadedStore = new ConcurrentHashMap<>();
        storage.load(loadedStore);

        assertThat(loadedStore.get("cdn.example.com"))
                .hasSize(1)
                .extracting(java.net.HttpCookie::getName)
                .containsExactly("valid");
    }

    /**
     * Проверяет, что cookies с истёкшим TTL отфильтровываются при загрузке.
     */
    @Test
    @DisplayName("Cookies с истёкшим TTL отфильтровываются при загрузке")
    void expiredCookiesFilteredOnLoad() throws IOException {
        // Сохраняем cookie
        java.net.HttpCookie cookie = new java.net.HttpCookie("old", "expired");
        cookie.setMaxAge(3600L);
        cookieStore.computeIfAbsent("example.com", k -> new CopyOnWriteArrayList<>()).add(cookie);
        storage.syncSave(cookieStore);

        // Модифицируем JSON: устанавливаем expires в прошлом
        String json = Files.readString(cookiesFile);
        long pastExpires = 1L; // 1 миллисекуда от epoch — точно в прошлом
        // Заменяем expires на старую дату
        json = json.replaceFirst("\"expires\"\\s*:\\s*\\d{13}", "\"expires\": " + pastExpires);
        Files.writeString(cookiesFile, json);

        // Загружаем
        ConcurrentHashMap<String, CopyOnWriteArrayList<java.net.HttpCookie>> loadedStore = new ConcurrentHashMap<>();
        storage.load(loadedStore);

        // Cookie с истёкшим TTL должен быть отфильтрован
        // loadedStore должен быть пустым или example.com должен отсутствовать/быть пустым
        assertThat(loadedStore)
                .describedAs("Хранилище должно быть пустым после фильтрации истёкших cookies")
                .isEmpty();
    }

    /**
     * Проверяет атомарность записи (tmp file + rename).
     */
    @Test
    @DisplayName("Атомарная запись файла")
    void atomicWrite() {
        java.net.HttpCookie cookie = new java.net.HttpCookie("test", "value");
        cookie.setMaxAge(600L);
        cookieStore.computeIfAbsent("test.com", k -> new CopyOnWriteArrayList<>()).add(cookie);

        storage.syncSave(cookieStore);

        // Временный файл должен быть удалён
        Path tmpFile = Path.of(cookiesFile.toString() + ".tmp");
        assertThat(Files.exists(tmpFile))
                .describedAs("Временный файл должен быть удалён после атомарной записи")
                .isFalse();

        // Целевой файл должен существовать
        assertThat(Files.exists(cookiesFile))
                .describedAs("Целевой файл должен существовать")
                .isTrue();
    }

    /**
     * Проверяет, что создаётся директория если её нет.
     */
    @Test
    @DisplayName("Создание директории если её нет")
    void createDirectoryIfNotExists() {
        Path nestedFile = tempDir.resolve("subdir").resolve("deep").resolve("cookies.cookies");
        CookieFileStorage nestedStorage = new CookieFileStorage(
                nestedFile, logService, "TestService", "test-uuid"
        );

        java.net.HttpCookie cookie = new java.net.HttpCookie("deep", "cookie");
        cookie.setMaxAge(300L);
        cookieStore.computeIfAbsent("deep.com", k -> new CopyOnWriteArrayList<>()).add(cookie);

        assertThatCode(() -> nestedStorage.syncSave(cookieStore))
                .describedAs("Запись должна создать отсутствующие директории")
                .doesNotThrowAnyException();

        assertThat(Files.exists(nestedFile))
                .describedAs("Файл должен быть создан вместе с директориями")
                .isTrue();
    }
}
