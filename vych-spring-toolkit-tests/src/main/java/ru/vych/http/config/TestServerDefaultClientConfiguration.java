package ru.vych.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;

/**
 * Конфигурация HTTP клиента по умолчанию.
 */
@Configuration
public class TestServerDefaultClientConfiguration {

    /** Сервисный код клиента по умолчанию. */
    public static final String DEFAULT_CLIENT_SERVICE_CODE = "TestServerHttpClient";

    /**
     * Создает HTTP клиент по умолчанию.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @param requestInterceptors список перехватчиков запросов
     * @param responseInterceptors список перехватчиков ответов
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Primary
    @Bean(name = DEFAULT_CLIENT_SERVICE_CODE)
    public HttpClient defaultClient(
            HttpClientBuilder builder, LogService logService,
            List<RequestInterceptor> requestInterceptors, List<ResponseInterceptor> responseInterceptors
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(DEFAULT_CLIENT_SERVICE_CODE)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setHeaders(Map.of("X-Config-Header", "config-value"));
        return builder.build(config, logService, requestInterceptors, responseInterceptors);
    }
}
