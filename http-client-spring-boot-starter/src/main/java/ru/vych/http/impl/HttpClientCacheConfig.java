package ru.vych.http.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.util.List;

/**
 * Конфигурация для менеджера кеширования HTTP-клиента.
 *
 * @see HttpClientCacheManager
 */
@Getter
@RequiredArgsConstructor
public class HttpClientCacheConfig {

    /**
     * Сервис логирования.
     */
    private final LogService logService;

    /**
     * Код сервиса для логирования.
     */
    private final String serviceCode;

    /**
     * UUID клиента для логирования.
     */
    private final String clientUuid;

    /**
     * Глобальный TTL кеширования по умолчанию.
     */
    private final long defaultCacheTtlSeconds;

    /**
     * Список response-интерсепторов.
     */
    private final List<ResponseInterceptor> responseInterceptors;
}
