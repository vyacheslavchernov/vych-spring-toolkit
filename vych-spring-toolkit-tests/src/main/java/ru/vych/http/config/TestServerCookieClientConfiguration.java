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

@Configuration
public class TestServerCookieClientConfiguration {

    private static final String COOKIE_ACCEPT_ALL_CLIENT = "CookieAcceptAllClient";
    private static final String COOKIE_ACCEPT_NONE_CLIENT = "CookieAcceptNoneClient";
    private static final String COOKIE_ORIGINAL_SERVER_CLIENT = "CookieOriginalServerClient";
    private static final String COOKIE_WITH_DEFAULTS_CLIENT = "CookieWithDefaultsClient";
    private static final String COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT = "CookieAcceptNoneWithDefaultsClient";

    public static final String COOKIE_ACCEPT_ALL_CLIENT_SERVICE_CODE = COOKIE_ACCEPT_ALL_CLIENT;
    public static final String COOKIE_ACCEPT_NONE_CLIENT_SERVICE_CODE = COOKIE_ACCEPT_NONE_CLIENT;
    public static final String COOKIE_ORIGINAL_SERVER_CLIENT_SERVICE_CODE = COOKIE_ORIGINAL_SERVER_CLIENT;
    public static final String COOKIE_WITH_DEFAULTS_CLIENT_SERVICE_CODE = COOKIE_WITH_DEFAULTS_CLIENT;
    public static final String COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT_SERVICE_CODE = COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT;

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
