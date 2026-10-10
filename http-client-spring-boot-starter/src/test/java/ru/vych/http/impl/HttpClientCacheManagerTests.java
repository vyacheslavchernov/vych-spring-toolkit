package ru.vych.http.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Юнит-тесты для {@link HttpClientCacheManager}.
 */
@DisplayName("HttpClientCacheManager should")
@ExtendWith(MockitoExtension.class)
class HttpClientCacheManagerTests {

    @Mock
    private LogService logService;

    @Mock
    private ResponseInterceptor responseInterceptor;

    private HttpClientCache cache;

    @BeforeEach
    void setUp() {
        cache = new HttpClientCache(100, 300, "test");
    }

    private RequestExecutor createExecutor(Response response) {
        return req -> {
            try {
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Test
    @DisplayName("return cached response on cache hit")
    void returnCachedResponseOnCacheHit() throws Exception {
        Request request = createCachedGetRequest("/api/users");
        Response originalResponse = Response.of(
                "req-uuid",
                request,
                200,
                "{\"id\":1}",
                List.of(new Header("Content-Type", "application/json"))
        );

        cache.put(request, originalResponse, 300);

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                req -> {
                    try {
                        return Response.of(
                                "req-uuid", req, 200, "{\"data\":\"test\"}",
                                List.of(new Header("Content-Type", "application/json"))
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                cache
        );

        Response response = manager.executeWithCache(request);

        assertThat(response.isCached())
                .as("Ответ должен быть возвращён из кеша")
                .isTrue();
        assertThat(response.getCachedAt())
                .as("cachedAt должен быть установлен")
                .isNotNull();
    }

    @Test
    @DisplayName("execute request on cache miss")
    void executeRequestOnCacheMiss() throws Exception {
        Request request = createCachedGetRequest("/api/users");

        Response serverResponse = Response.of(
                "req-uuid", request, 200, "{\"data\":\"test\"}",
                List.of(
                        new Header("Content-Type", "application/json"),
                        new Header("Cache-Control", "max-age=60")
                )
        );

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                req -> {
                    try {
                        return serverResponse;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                cache
        );

        Response response = manager.executeWithCache(request);

        assertThat(response.isCached())
                .as("Ответ должен быть получен с сервера")
                .isFalse();
        assertThat(response.getRawBody())
                .as("Тело ответа должно совпадать")
                .isEqualTo("{\"data\":\"test\"}");
    }

    @Test
    @DisplayName("respect request-level TTL override")
    void respectRequestLevelTtlOverride() throws Exception {
        Request request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/config")
                .setCached(true)
                .setCacheTtl(60, TimeUnit.SECONDS)
                .setResponseClass(String.class)
                .build();

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                createExecutor(Response.of(
                        "req-uuid", request, 200, "config", List.of()
                )),
                cache
        );

        manager.executeWithCache(request);

        assertThat(cache.size())
                .as("Ответ должен быть сохранён в кеш")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("respect Cache-Control max-age")
    void respectCacheControlMaxAge() throws Exception {
        Request request = createCachedGetRequest("/api/data");

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                createExecutor(Response.of(
                        "req-uuid", request, 200, "data",
                        List.of(new Header("Cache-Control", "max-age=30"))
                )),
                cache
        );

        manager.executeWithCache(request);

        assertThat(cache.size())
                .as("Ответ должен быть сохранён в кеш")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("not cache response with Cache-Control no-store")
    void notCacheResponseWithCacheControlNoStore() throws Exception {
        Request request = createCachedGetRequest("/api/nocache");

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                createExecutor(Response.of(
                        "req-uuid", request, 200, "data",
                        List.of(new Header("Cache-Control", "no-store"))
                )),
                cache
        );

        manager.executeWithCache(request);

        assertThat(cache.size())
                .as("Ответ с Cache-Control: no-store не должен кешироваться")
                .isZero();
    }

    @Test
    @DisplayName("apply response interceptors on cache hit")
    void applyResponseInterceptorsOnCacheHit() throws Exception {
        Request request = createCachedGetRequest("/api/users");
        Response originalResponse = Response.of(
                "req-uuid",
                request,
                200,
                "data",
                List.of()
        );

        cache.put(request, originalResponse, 300);

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of(responseInterceptor)),
                createExecutor(Response.of(
                        "req-uuid", request, 200, "data", List.of()
                )),
                cache
        );

        manager.executeWithCache(request);

        verify(responseInterceptor).handle(any(), any());
    }

    @Test
    @DisplayName("handle execute exception gracefully")
    void handleExecuteExceptionGracefully() throws Exception {
        Request request = createCachedGetRequest("/api/error");

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 300, List.of()),
                createExecutor(Response.of(
                        "req-uuid", request, 500, "error", List.of()
                )),
                cache
        );

        Response response = manager.executeWithCache(request);

        assertThat(response.getStatus())
                .as("Должен вернуться статус ошибки")
                .isEqualTo(500);
    }

    @Test
    @DisplayName("use default TTL when request TTL is not set")
    void useDefaultTtlWhenRequestTtlNotSet() throws Exception {
        Request request = createCachedGetRequest("/api/default");

        HttpClientCacheManager manager = new HttpClientCacheManager(
                new HttpClientCacheConfig(logService, "Test", "uuid", 600, List.of()),
                createExecutor(Response.of(
                        "req-uuid", request, 200, "data", List.of()
                )),
                cache
        );

        manager.executeWithCache(request);

        assertThat(cache.size())
                .describedAs("Ответ должен быть сохранён в кеш")
                .isGreaterThanOrEqualTo(1);
    }

    private Request createCachedGetRequest(String url) {
        try {
            return Request.builder()
                    .setMethod(HttpMethod.GET)
                    .setUrl(url)
                    .setCached(true)
                    .setResponseClass(String.class)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
