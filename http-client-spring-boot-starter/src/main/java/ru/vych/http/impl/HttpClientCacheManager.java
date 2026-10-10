package ru.vych.http.impl;

import ru.vych.http.impl.common.UrlUtils;
import ru.vych.http.impl.entities.CachedEntry;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.exceptions.HttpClientExecuteRequestException;
import ru.vych.http.impl.interceptors.ResponseInterceptor;

import java.util.List;

import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.EXECUTE_ERROR_UNKNOWN;

/**
 * Менеджер кэширования HTTP-запросов.
 * <p>
 * Инкапсузирует логику работы с кешем: hit/miss, инвалидацию,
 * парсинг Cache-Control и логирование.
 * </p>
 *
 * @see HttpClientCache
 * @see CachedEntry
 */
public class HttpClientCacheManager {

    /**
     * Конфигурация менеджера кеша.
     */
    private final HttpClientCacheConfig config;

    /**
     * Функция для выполнения HTTP-запроса без кеша.
     */
    private final RequestExecutor requestExecutor;

    /**
     * Внутренний кеш.
     */
    private final HttpClientCache cache;

    /**
     * Создаёт новый менеджер кеша.
     *
     * @param config       конфигурация менеджера кеша
     * @param requestExecutor функция для выполнения HTTP-запроса без кеша
     * @param cache        внутренний кеш
     */
    public HttpClientCacheManager(
            HttpClientCacheConfig config,
            RequestExecutor requestExecutor,
            HttpClientCache cache
    ) {
        this.config = config;
        this.requestExecutor = requestExecutor;
        this.cache = cache;
    }

    /**
     * Выполняет запрос с поддержкой кеша.
     * <p>
     * Если запись найдена в кеше — возвращает кешированный ответ.
     * Иначе выполняет запрос через {@link RequestExecutor}, кеширует результат
     * и возвращает ответ с сервера.
     * </p>
     *
     * @param request запрос с флагом {@code cached=true}
     * @return результат выполнения запроса
     * @throws Exception если произошла ошибка при выполнении
     * @throws ru.vych.http.impl.exceptions.HttpClientExecuteRequestException если ошибка выполнения
     */
    public Response executeWithCache(Request request) throws Exception {
        CachedEntry cachedEntry = cache.get(request);

        if (cachedEntry != null) {
            Response cachedResponse = buildCachedResponse(request, cachedEntry);
            applyResponseInterceptors(cachedResponse);
            return cachedResponse;
        }

        Response response;
        try {
            response = requestExecutor.execute(request);
        } catch (Exception e) {
            if (e instanceof HttpClientException ie) {
                throw ie;
            }
            throw new HttpClientExecuteRequestException(EXECUTE_ERROR_UNKNOWN, e);
        }

        long ttl = determineTtl(request, response);
        if (shouldCacheResponse(response, ttl)) {
            cache.put(request, response, ttl);
        }

        return response;
    }

    /**
     * Инвалидирует кеш для WRITE-запроса.
     * <p>
     * Удаляет все записи, ключ которых начинается с точного URL
     * или родительского URL запроса.
     * </p>
     *
     * @param request WRITE-запрос
     */
    public void invalidateForWrite(Request request) {
        String url = request.getUrl();
        String exactPattern = UrlUtils.extractUrlPath(url);
        String parentPattern = UrlUtils.extractParentUrlPath(url);

        int exactRemoved = cache.invalidateByUrlPrefix(exactPattern);
        int parentRemoved = cache.invalidateByUrlPrefix(parentPattern);
        int totalRemoved = exactRemoved + parentRemoved;

        if (totalRemoved > 0) {
            logCacheInvalidate(exactPattern, totalRemoved);
        }
    }

    /**
     * Определяет TTL для кеширования ответа.
     *
     * @param request исходный запрос
     * @param response ответ от сервера
     * @return TTL в секундах
     */
    private long determineTtl(Request request, Response response) {
        long ttl = request.getCacheTtl() != null
                ? request.getCacheTtl()
                : config.getDefaultCacheTtlSeconds();

        long maxAge = parseMaxAgeFromCacheControl(response.getHeaders());
        if (maxAge > 0) {
            ttl = Math.min(ttl, maxAge);
        }

        return ttl;
    }

    /**
     * Проверяет, следует ли кешировать ответ.
     *
     * @param response ответ от сервера
     * @param ttl TTL в секундах
     * @return {@code true} если ответ следует кешировать
     */
    private boolean shouldCacheResponse(Response response, long ttl) {
        if (ttl <= 0) {
            return false;
        }

        List<Header> headers = response.getHeaders();
        if (headers == null) {
            return true;
        }

        for (Header header : headers) {
            if ("Cache-Control".equalsIgnoreCase(header.name())) {
                String value = header.value().toLowerCase();
                if (value.contains("no-store")
                        || value.contains("max-age=0")
                        || value.contains("no-cache")) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Парсит значение {@code max-age} из {@code Cache-Control} заголовков.
     *
     * @param headers список заголовков ответа
     * @return значение max-age в секундах, или 0 если не указано
     */
    private long parseMaxAgeFromCacheControl(List<Header> headers) {
        if (headers == null) {
            return 0;
        }

        for (Header header : headers) {
            if ("Cache-Control".equalsIgnoreCase(header.name())) {
                String value = header.value();
                int maxAgeIndex = value.indexOf("max-age=");
                if (maxAgeIndex >= 0) {
                    try {
                        String maxAgeStr = value.substring(maxAgeIndex + 8).trim();
                        int commaIndex = maxAgeStr.indexOf(',');
                        if (commaIndex >= 0) {
                            maxAgeStr = maxAgeStr.substring(0, commaIndex);
                        }
                        return Long.parseLong(maxAgeStr);
                    } catch (NumberFormatException e) {
                        // Игнорируем невалидные значения
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Формирует ответ из кешированной записи.
     *
     * @param request исходный запрос
     * @param cachedEntry кешированная запись с ответом и временем истечения
     * @return новый {@link Response} с флагом {@code isCached=true}
     */
    private Response buildCachedResponse(Request request, CachedEntry cachedEntry) {
        return Response.cached(
                cachedEntry.getResponse(),
                request.getUuid(),
                request,
                cachedEntry.getCachedAt()
        );
    }

    /**
     * Логирует инвалидацию кеша.
     *
     * @param pattern паттерн URL
     * @param count количество удалённых записей
     */
    private void logCacheInvalidate(String pattern, int count) {
        config.getLogService().debug(
                config.getServiceCode(), config.getClientUuid(),
                "Cache invalidate: pattern=" + pattern + " count=" + count,
                pattern
        );
    }

    /**
     * Применяет response-интерсепторы к ответу.
     *
     * @param response ответ для обработки
     */
    private void applyResponseInterceptors(Response response) {
        for (ResponseInterceptor interceptor : config.getResponseInterceptors()) {
            interceptor.handle(null, response);
        }
    }
}
