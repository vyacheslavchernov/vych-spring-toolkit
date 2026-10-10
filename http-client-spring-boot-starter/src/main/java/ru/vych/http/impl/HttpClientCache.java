package ru.vych.http.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.vych.http.impl.common.UrlUtils;
import ru.vych.http.impl.entities.CachedEntry;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientHandleResponseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.CREATION_ERROR_HASH_ALGORITHM_NOT_AVAILABLE;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION;

/**
 * Внутренний LRU-кеш для HTTP-клиента.
 * <p>
 * Хранит кешированные ответы в памяти с поддержкой TTL, LRU-eviction
 * и генерации SHA-256 ключей из параметров запроса.
 * </p>
 * <p>
 * <b>Потокобезопасен:</b> использует {@code ConcurrentHashMap} + {@code LinkedHashMap}
 * для синхронизированного доступа с access-order.
 * </p>
 *
 * @see CachedEntry
 * @see ru.vych.http.impl.HttpClientImpl
 */
public class HttpClientCache {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Внутреннее хранилище кеша.
     * <p>
     * Ключ — SHA-256 хеш запроса (строка).
     * Значение — запись кеша с TTL.
     * {@code LinkedHashMap} с access-order обеспечивает LRU-поведение.
     * </p>
     */
    private final ConcurrentHashMap<String, CachedEntry> cache;

    /**
     * Мапа URL → set of cache keys.
     * <p>
     * Используется для инвалидации кеша по паттерну URL.
     * Ключ — URL (без query-параметров), значение — set SHA-256 хешей.
     * </p>
     */
    private final ConcurrentHashMap<String, Set<String>> urlToCacheKeys;

    /**
     * Мапа ключ → timestamp последнего доступа.
     * <p>
     * Используется для определения самой старой записи (LRU).
     * Ключ — SHA-256 хеш, значение — время последнего доступа (nanoTime).
     * </p>
     */
    private final ConcurrentHashMap<String, Long> accessTimestamps;

    /**
     * Jackson ObjectMapper для сериализации payload.
     */
    private final ObjectMapper mapper;

    /**
     * Максимальное количество записей в кеше.
     * При переполнении удаляется самая старая запись (LRU).
     */
    private final int maxSize;

    /**
     * Глобальный TTL кеширования в секундах по умолчанию.
     */
    private final long defaultTtlSeconds;

    /**
     * Создаёт новый экземпляр кеша.
     *
     * @param maxSize        максимальное количество записей; должно быть больше нуля
     * @param defaultTtlSeconds глобальный TTL в секундах; должно быть больше нуля
     * @param serviceCode   код сервиса для логирования
     */
    public HttpClientCache(int maxSize, long defaultTtlSeconds, String serviceCode) {
        this.maxSize = maxSize;
        this.defaultTtlSeconds = defaultTtlSeconds;
        this.cache = new ConcurrentHashMap<>(16, 0.75f, 1);
        this.urlToCacheKeys = new ConcurrentHashMap<>();
        this.accessTimestamps = new ConcurrentHashMap<>();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    /**
     * Возвращает кешированный ответ для данного запроса.
     * <p>
     * Если запись найдена и не истекла — возвращает {@link CachedEntry}.
     * Иначе возвращает {@code null} (cache miss).
     * Истекшие записи удаляются лениво при доступе.
     * </p>
     *
     * @param request запрос для поиска в кеше
     * @return кешированная запись или {@code null} если miss или истекла
     */
    public CachedEntry get(Request request) {
        String key;
        try {
            key = generateCacheKey(request);
        } catch (HttpClientHandleResponseException e) {
            return null;
        }
        CachedEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            accessTimestamps.remove(key);
            urlToCacheKeys.values().forEach(keys -> keys.remove(key));
            return null;
        }

        // LRU: обновляем timestamp доступа
        accessTimestamps.put(key, System.nanoTime());

        return entry;
    }

    /**
     * Сохраняет ответ в кеш.
     * <p>
     * Если кеш переполнен, удаляет самую старую запись (LRU-eviction).
     * </p>
     *
     * @param request  исходный запрос
     * @param response ответ для кеширования
     * @param ttlSeconds TTL записи в секундах
     */
    public void put(Request request, Response response, long ttlSeconds) {
        String key;
        String urlPath;
        try {
            key = generateCacheKey(request);
            urlPath = UrlUtils.extractUrlPath(request.getUrl());
        } catch (HttpClientHandleResponseException e) {
            return;
        }
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        CachedEntry entry = new CachedEntry(response, now, expiresAt);

        // LRU-eviction: если кеш переполнен, удаляем самую старую запись
        if (cache.size() >= maxSize) {
            evictLRU();
        }

        cache.put(key, entry);
        accessTimestamps.put(key, System.nanoTime());

        // Добавляем URL → key мапу для инвалидации
        urlToCacheKeys.computeIfAbsent(urlPath, k -> ConcurrentHashMap.newKeySet()).add(key);
    }

    /**
     * Удаляет записи из кеша по паттерну URL.
     * <p>
     * Удаляет все записи, ключ которых начинается с данного префикса.
     * Используется для инвалидации кеша при WRITE-запросах.
     * </p>
     *
     * @param urlPrefix префикс URL для инвалидации (без query-параметров)
     * @return количество удалённых записей
     */
    public int invalidateByUrlPrefix(String urlPrefix) {
        Set<String> keysToRemove = new HashSet<>();

        // Находим все keys, чьи URL начинаются с префикса
        for (Map.Entry<String, Set<String>> entry : urlToCacheKeys.entrySet()) {
            if (entry.getKey().startsWith(urlPrefix) || entry.getKey().equals(urlPrefix)) {
                keysToRemove.addAll(entry.getValue());
            }
        }

        // Удаляем записи
        int removedCount = 0;
        for (String key : keysToRemove) {
            if (cache.remove(key) != null) {
                removedCount++;
            }
            urlToCacheKeys.values().forEach(keys -> keys.remove(key));
        }

        // Очищаем мапу URL → keys для пустых групп
        urlToCacheKeys.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return removedCount;
    }

    /**
     * Удаляет все записи из кеша.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Возвращает текущее количество записей в кеше.
     *
     * @return размер кеша
     */
    public int size() {
        return cache.size();
    }

    /**
     * Генерирует SHA-256 ключ кеша из параметров запроса.
     * <p>
     * Ключ формируется из:
     * <ol>
     *   <li>HTTP-метода запроса</li>
     *   <li>Полного URL (root + path + path params)</li>
     *   <li>Отсортированных по ключу query-параметров</li>
     *   <li>Тела запроса (payload) — сериализованного в byte array</li>
     * </ol>
     * </p>
     *
     * @param request запрос для генерации ключа
     * @return SHA-256 хеш в base64-представлении
     * @throws RuntimeException если SHA-256 недоступен в JVM
     * @throws HttpClientHandleResponseException если не удалось сериализовать payload
     */
    protected String generateCacheKey(Request request) throws HttpClientHandleResponseException {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            // HTTP-метод
            digest.update(request.getMethod().name().getBytes(StandardCharsets.UTF_8));

            // URL (без query-параметров, они добавляются отдельно)
            String url = request.getUrl();
            digest.update(url.getBytes(StandardCharsets.UTF_8));

            // Path-параметры
            request.getPathParams().forEach(param ->
                    digest.update(param.getBytes(StandardCharsets.UTF_8))
            );

            // Query-параметры (отсортированные по ключу)
            request.getQueryParams().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        digest.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
                        digest.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    });

            // Payload
            Object payload = request.getPayload();
            if (payload != null) {
                byte[] payloadBytes = serializePayload(payload);
                digest.update(payloadBytes);
            }

            byte[] hash = digest.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 должен быть доступен во всех JVM
            throw new RuntimeException(CREATION_ERROR_HASH_ALGORITHM_NOT_AVAILABLE, e);
        } catch (HttpClientHandleResponseException e) {
            throw new HttpClientHandleResponseException(RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION, e);
        }
    }

    /**
     * Сериализует payload в byte array для включения в cache key.
     * <p>
     * Поддерживает {@code String}, {@code byte[]} и JSON-сериализуемые объекты через Jackson.
     * </p>
     *
     * @param payload тело запроса
     * @return сериализованные байты
     * @throws HttpClientHandleResponseException если не удалось сериализовать
     */
    private byte[] serializePayload(Object payload) throws HttpClientHandleResponseException {
        if (payload instanceof String text) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        // JSON-сериализация через Jackson
        try {
            return mapper.writeValueAsBytes(payload);
        } catch (Exception e) {
            throw new HttpClientHandleResponseException(
                    RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION, e
            );
        }
    }

    /**
     * Удаляет самую старую запись (LRU) из кеша.
     * <p>
     * Использует {@code accessTimestamps} для определения
     * самой старой записи по минимальному timestamp.
     * </p>
     */
    private void evictLRU() {
        String lruKey = null;
        long oldestTimestamp = Long.MAX_VALUE;

        // Находим запись с минимальным timestamp
        for (Map.Entry<String, Long> entry : accessTimestamps.entrySet()) {
            if (cache.containsKey(entry.getKey())) {
                long timestamp = entry.getValue();
                if (timestamp < oldestTimestamp) {
                    oldestTimestamp = timestamp;
                    lruKey = entry.getKey();
                }
            }
        }

        if (lruKey != null) {
            cache.remove(lruKey);
            accessTimestamps.remove(lruKey);
            String keyToRemove = lruKey;
            urlToCacheKeys.values().forEach(keys -> keys.remove(keyToRemove));
        }
    }
}
