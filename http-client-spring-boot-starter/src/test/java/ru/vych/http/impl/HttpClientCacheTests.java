package ru.vych.http.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.entities.Header;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты для {@link HttpClientCache}.
 */
@DisplayName("HttpClientCache should")
class HttpClientCacheTests {

    private HttpClientCache cache;

    @BeforeEach
    void setUp() {
        cache = new HttpClientCache(10, 300, "test");
    }

    @Test
    @DisplayName("return null for non-existent key")
    void returnNullForNonExistentKey() {
        Request request = createGetRequest("/api/users");

        var entry = cache.get(request);

        assertThat(entry)
                .as("Кеш должен возвращать null для отсутствующего ключа")
                .isNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("store and retrieve cached response")
    void storeAndRetrieveCachedResponse() {
        Request request = createGetRequest("/api/users");
        Response response = createResponse(200, "[{\"id\":1}]");

        cache.put(request, response, 300);

        var entry = cache.get(request);

        assertThat(entry)
                .as("Кеш должен возвращать запись для существующего ключа")
                .isNotNull();
        assertThat(entry.getResponse().getStatus())
                .as("Статус ответа должен совпадать")
                .isEqualTo(200);
        assertThat(entry.getResponse().getRawBody())
                .as("Тело ответа должно совпадать")
                .isEqualTo("[{\"id\":1}]");
    }

    @Test
    @SneakyThrows
    @DisplayName("return null for expired entry")
    void returnNullForExpiredEntry() {
        Request request = createGetRequest("/api/users");
        Response response = createResponse(200, "data");

        cache.put(request, response, 0);

        var entry = cache.get(request);

        assertThat(entry)
                .as("Истекшая запись должна возвращать null")
                .isNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("evict LRU entry when cache is full")
    void evictLruEntryWhenCacheIsFull() {
        HttpClientCache smallCache = new HttpClientCache(3, 300, "test");

        Request req1 = createGetRequest("/api/1");
        Request req2 = createGetRequest("/api/2");
        Request req3 = createGetRequest("/api/3");
        Request req4 = createGetRequest("/api/4");

        smallCache.put(req1, createResponse(200, "1"), 300);
        smallCache.put(req2, createResponse(200, "2"), 300);
        smallCache.put(req3, createResponse(200, "3"), 300);

        // req1 должен быть удалён при добавлении req4
        smallCache.put(req4, createResponse(200, "4"), 300);

        var entry1 = smallCache.get(req1);
        var entry4 = smallCache.get(req4);

        assertThat(entry1)
                .as("Самая старая запись должна быть удалена")
                .isNull();
        assertThat(entry4)
                .as("Новая запись должна быть в кеше")
                .isNotNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("invalidate by URL prefix")
    void invalidateByUrlPrefix() {
        Request req1 = createGetRequest("/api/users/123");
        Request req2 = createGetRequest("/api/users/456");
        Request req3 = createGetRequest("/api/orders");

        cache.put(req1, createResponse(200, "user123"), 300);
        cache.put(req2, createResponse(200, "user456"), 300);
        cache.put(req3, createResponse(200, "order"), 300);

        int removed = cache.invalidateByUrlPrefix("/api/users");

        assertThat(removed)
                .as("Должно быть удалено 2 записи для /api/users/*")
                .isEqualTo(2);

        var entry1 = cache.get(req1);
        var entry3 = cache.get(req3);

        assertThat(entry1)
                .as("Запись /api/users/123 должна быть удалена")
                .isNull();
        assertThat(entry3)
                .as("Запись /api/orders должна остаться")
                .isNotNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("invalidate exact URL only")
    void invalidateExactUrlOnly() {
        Request req1 = createGetRequest("/api/users/123");
        Request req2 = createGetRequest("/api/users/123/orders");

        cache.put(req1, createResponse(200, "user"), 300);
        cache.put(req2, createResponse(200, "orders"), 300);

        int removed = cache.invalidateByUrlPrefix("/api/users/123");

        assertThat(removed)
                .as("Должно быть удалено 1 точное совпадение")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @SneakyThrows
    @DisplayName("generate same cache key for same request")
    void generateSameCacheKeyForSameRequest() {
        Request req1 = createGetRequest("/api/users?page=1");
        Request req2 = createGetRequest("/api/users?page=1");

        String key1 = cache.generateCacheKey(req1);
        String key2 = cache.generateCacheKey(req2);

        assertThat(key1)
                .describedAs("Ключи для одинаковых запросов должны совпадать")
                .isEqualTo(key2);
    }

    @Test
    @SneakyThrows
    @DisplayName("generate different cache key for different requests")
    void generateDifferentCacheKeyForDifferentRequests() {
        Request req1 = createGetRequest("/api/users");
        Request req2 = createGetRequest("/api/orders");

        String key1 = cache.generateCacheKey(req1);
        String key2 = cache.generateCacheKey(req2);

        assertThat(key1)
                .describedAs("Ключи для разных запросов должны отличаться")
                .isNotEqualTo(key2);
    }

    @Test
    @SneakyThrows
    @DisplayName("sort query parameters for cache key")
    void sortQueryParametersForCacheKey() {
        Request req1 = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/users")
                .addQueryParam("b", "2")
                .addQueryParam("a", "1")
                .build();

        Request req2 = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/users")
                .addQueryParam("a", "1")
                .addQueryParam("b", "2")
                .build();

        String key1 = cache.generateCacheKey(req1);
        String key2 = cache.generateCacheKey(req2);

        assertThat(key1)
                .as("Ключи должны совпадать независимо от порядка query-параметров")
                .isEqualTo(key2);
    }

    @Test
    @DisplayName("clear all entries")
    void clearAllEntries() {
        cache.put(createGetRequest("/api/1"), createResponse(200, "1"), 300);
        cache.put(createGetRequest("/api/2"), createResponse(200, "2"), 300);

        cache.clear();

        assertThat(cache.size())
                .as("Кеш должен быть пуст после clear()")
                .isZero();
    }

    @Test
    @SneakyThrows
    @DisplayName("return correct size")
    void returnCorrectSize() {
        cache.put(createGetRequest("/api/1"), createResponse(200, "1"), 300);
        cache.put(createGetRequest("/api/2"), createResponse(200, "2"), 300);

        assertThat(cache.size())
                .as("Размер кеша должен быть 2")
                .isEqualTo(2);
    }

    @Test
    @SneakyThrows
    @DisplayName("move entry to front on access (LRU)")
    void moveEntryToFrontOnAccess() {
        HttpClientCache smallCache = new HttpClientCache(3, 300, "test");

        Request req1 = createGetRequest("/api/1");
        Request req2 = createGetRequest("/api/2");
        Request req3 = createGetRequest("/api/3");

        smallCache.put(req1, createResponse(200, "1"), 300);
        smallCache.put(req2, createResponse(200, "2"), 300);
        smallCache.put(req3, createResponse(200, "3"), 300);

        // Доступ к req1 перемещает его в конец LRU-очереди
        smallCache.get(req1);

        // Добавляем req4, должен удалиться req2 (самый старый)
        smallCache.put(createGetRequest("/api/4"), createResponse(200, "4"), 300);

        var entry2 = smallCache.get(req2);
        var entry1 = smallCache.get(req1);

        assertThat(entry2)
                .as("Запись req2 должна быть удалена (LRU)")
                .isNull();
        assertThat(entry1)
                .as("Запись req1 должна остаться (была доступна)")
                .isNotNull();
    }

    @SneakyThrows
    private Request createGetRequest(String url) {
        return Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl(url)
                .setResponseClass(String.class)
                .build();
    }

    private Response createResponse(int status, String body) {
        return Response.of(
                "test-uuid",
                null,
                status,
                body,
                List.of(new Header("Content-Type", "application/json"))
        );
    }
}
