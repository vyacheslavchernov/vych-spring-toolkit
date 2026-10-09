package ru.vych.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.logger.impl.LogService;

import java.time.Duration;
import java.util.List;

import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;

/**
 * Конфигурация HTTP клиентов для тестирования редиректов.
 */
@Configuration
public class TestServerRedirectClientConfiguration {

    /** Сервисный код клиента с followRedirects = true. */
    public static final String REDIRECT_FOLLOW_CLIENT_SERVICE_CODE = "RedirectFollowClient";
    /** Сервисный код клиента с followRedirects = false. */
    public static final String REDIRECT_NO_FOLLOW_CLIENT_SERVICE_CODE = "RedirectNoFollowClient";

    /**
     * Создает HTTP клиент с политикой следования за редиректами.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = REDIRECT_FOLLOW_CLIENT_SERVICE_CODE)
    public HttpClient redirectFollowClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(REDIRECT_FOLLOW_CLIENT_SERVICE_CODE)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setRedirectPolicy(java.net.http.HttpClient.Redirect.NORMAL);
        return builder.build(config, logService, List.of(), List.of());
    }

    /**
     * Создает HTTP клиент без следования за редиректами.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = REDIRECT_NO_FOLLOW_CLIENT_SERVICE_CODE)
    public HttpClient redirectNoFollowClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(REDIRECT_NO_FOLLOW_CLIENT_SERVICE_CODE)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setRedirectPolicy(java.net.http.HttpClient.Redirect.NEVER);
        return builder.build(config, logService, List.of(), List.of());
    }
}
