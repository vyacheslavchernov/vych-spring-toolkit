package ru.vych.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.common.CookiesPolicies;
import ru.vych.http.impl.entities.CookieEntry;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.logger.impl.LogService;

import java.net.HttpCookie;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;

/**
 * Конфигурация HTTP клиентов с различными политиками обработки cookies.
 */
@Configuration
public class TestServerCookieClientConfiguration {

    /** Название клиента с политикой ACCEPT_ALL. */
    private static final String COOKIE_ACCEPT_ALL_CLIENT = "CookieAcceptAllClient";
    /** Название клиента с политикой ACCEPT_NONE. */
    private static final String COOKIE_ACCEPT_NONE_CLIENT = "CookieAcceptNoneClient";
    /** Название клиента с политикой ACCEPT_ORIGINAL_SERVER. */
    private static final String COOKIE_ORIGINAL_SERVER_CLIENT = "CookieOriginalServerClient";
    /** Название клиента с дефолтными настройками. */
    private static final String COOKIE_WITH_DEFAULTS_CLIENT = "CookieWithDefaultsClient";
    /** Название клиента с ACCEPT_NONE и дефолтными настройками. */
    private static final String COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT = "CookieAcceptNoneWithDefaultsClient";

    /** Сервисный код клиента ACCEPT_ALL. */
    public static final String COOKIE_ACCEPT_ALL_CLIENT_SERVICE_CODE = COOKIE_ACCEPT_ALL_CLIENT;
    /** Сервисный код клиента ACCEPT_NONE. */
    public static final String COOKIE_ACCEPT_NONE_CLIENT_SERVICE_CODE = COOKIE_ACCEPT_NONE_CLIENT;
    /** Сервисный код клиента ACCEPT_ORIGINAL_SERVER. */
    public static final String COOKIE_ORIGINAL_SERVER_CLIENT_SERVICE_CODE = COOKIE_ORIGINAL_SERVER_CLIENT;
    /** Сервисный код клиента с дефолтными настройками. */
    public static final String COOKIE_WITH_DEFAULTS_CLIENT_SERVICE_CODE = COOKIE_WITH_DEFAULTS_CLIENT;
    /** Сервисный код клиента ACCEPT_NONE с дефолтными настройками. */
    public static final String COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT_SERVICE_CODE
            = COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT;

    /**
     * Создает HTTP клиент с политикой ACCEPT_ALL.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = COOKIE_ACCEPT_ALL_CLIENT_SERVICE_CODE)
    public HttpClient cookieAcceptAllClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(COOKIE_ACCEPT_ALL_CLIENT)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setCookiePolicy(CookiesPolicies.ACCEPT_ALL);
        return builder.build(config, logService, List.of(), List.of());
    }

    /**
     * Создает HTTP клиент с политикой ACCEPT_NONE.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = COOKIE_ACCEPT_NONE_CLIENT_SERVICE_CODE)
    public HttpClient cookieAcceptNoneClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(COOKIE_ACCEPT_NONE_CLIENT)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setCookiePolicy(CookiesPolicies.ACCEPT_NONE);
        return builder.build(config, logService, List.of(), List.of());
    }

    /**
     * Создает HTTP клиент с политикой ACCEPT_ORIGINAL_SERVER.
     * @param builder билдер HTTP клиен��а
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = COOKIE_ORIGINAL_SERVER_CLIENT_SERVICE_CODE)
    public HttpClient cookieOriginalServerClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(COOKIE_ORIGINAL_SERVER_CLIENT)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2))
                .setCookiePolicy(CookiesPolicies.ACCEPT_ORIGINAL_SERVER);
        return builder.build(config, logService, List.of(), List.of());
    }

    /**
     * Создает HTTP клиент с дефолтными cookies.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = COOKIE_WITH_DEFAULTS_CLIENT_SERVICE_CODE)
    public HttpClient cookieWithDefaultsClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        try {
            HttpClientConfig config = new HttpClientConfig(COOKIE_WITH_DEFAULTS_CLIENT)
                    .setRoot(TEST_SERVER_URI)
                    .setTimeout(Duration.ofSeconds(2))
                    .setCookiePolicy(CookiesPolicies.ACCEPT_ALL)
                    .setCookies(List.of(
                            new CookieEntry(
                                    new URI(TEST_SERVER_URI),
                                    new HttpCookie("default", "value")
                            )
                    ));
            return builder.build(config, logService, List.of(), List.of());
        } catch (java.net.URISyntaxException e) {
            throw new HttpClientException("Invalid URI for default cookies", e);
        }
    }

    /**
     * Создает HTTP клиент с политикой ACCEPT_NONE и дефолтными cookies.
     * @param builder билдер HTTP клиента
     * @param logService сервис логирования
     * @return настроенный HttpClient
     * @throws HttpClientException при ошибке создания клиента
     */
    @Bean(name = COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT_SERVICE_CODE)
    public HttpClient cookieAcceptNoneWithDefaultsClient(
            HttpClientBuilder builder, LogService logService
    ) throws HttpClientException {
        try {
            HttpClientConfig config = new HttpClientConfig(COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT)
                    .setRoot(TEST_SERVER_URI)
                    .setTimeout(Duration.ofSeconds(2))
                    .setCookiePolicy(CookiesPolicies.ACCEPT_NONE)
                    .setCookies(List.of(
                            new CookieEntry(
                                    new URI(TEST_SERVER_URI),
                                    new HttpCookie("default", "value")
                            )
                    ));
            return builder.build(config, logService, List.of(), List.of());
        } catch (java.net.URISyntaxException e) {
            throw new HttpClientException("Invalid URI for default cookies", e);
        }
    }
}
