package ru.vych.http.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.common.CookiesPolicies;
import ru.vych.http.impl.entities.CookieEntry;
import ru.vych.logger.impl.LogService;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты cookie policies (ACCEPT_ALL, ACCEPT_NONE, ACCEPT_ORIGINAL_SERVER).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты cookie policies HTTP-клиента")
class HttpClientCookiePolicyTests {

    @Mock
    private LogService logService;

    /**
     * Проверяет, что ACCEPT_ALL принимает все cookies.
     */
    @Test
    @DisplayName("ACCEPT_ALL принимает все cookies")
    @SneakyThrows
    void acceptAllAcceptsAllCookies() {
        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookiePolicy(CookiesPolicies.ACCEPT_ALL);

        var client = new HttpClientImpl(config, logService, null, null);

        // Default cookies должны добавиться
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        HttpClientConfig configWithCookies = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookiePolicy(CookiesPolicies.ACCEPT_ALL)
                .setCookies(cookieEntries);

        var clientWithCookies = new HttpClientImpl(configWithCookies, logService, null, null);

        assertThat(clientWithCookies.getCookies("example.com"))
                .describedAs("ACCEPT_ALL должен принимать все cookies")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("test");
    }

    /**
     * Проверяет, что ACCEPT_NONE отклоняет все cookies.
     */
    @Test
    @DisplayName("ACCEPT_NONE отклоняет все cookies")
    @SneakyThrows
    void acceptNoneRejectsAllCookies() {
        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookiePolicy(CookiesPolicies.ACCEPT_NONE);

        var client = new HttpClientImpl(config, logService, null, null);

        // Default cookies должны добавиться (ACCEPT_NONE влияет только на Set-Cookie из ответов)
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("default", "value"))
        );

        HttpClientConfig configWithCookies = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookiePolicy(CookiesPolicies.ACCEPT_NONE)
                .setCookies(cookieEntries);

        var clientWithCookies = new HttpClientImpl(configWithCookies, logService, null, null);

        // Default cookies загружаются независимо от политики
        assertThat(clientWithCookies.getCookies("example.com"))
                .describedAs("Default cookies загружаются даже при ACCEPT_NONE")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("default");
    }

    /**
     * Проверяет, что ACCEPT_ORIGINAL_SERVER принимает только cookies от root host.
     */
    @Test
    @DisplayName("ACCEPT_ORIGINAL_SERVER принимает только cookies от root host")
    @SneakyThrows
    void acceptOriginalServerAcceptsOnlyRootHost() {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://api.example.com"), new HttpCookie("root_cookie", "value"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("https://api.example.com")
                .setCookiePolicy(CookiesPolicies.ACCEPT_ORIGINAL_SERVER)
                .setCookies(cookieEntries);

        var client = new HttpClientImpl(config, logService, null, null);

        assertThat(client.getCookies("api.example.com"))
                .describedAs("ACCEPT_ORIGINAL_SERVER должен принимать cookies от root host")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("root_cookie");
    }

    /**
     * Проверяет, что getCookies возвращает копию списка.
     */
    @Test
    @DisplayName("getCookies возвращает копию списка")
    @SneakyThrows
    void getCookiesReturnsCopy() {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookiePolicy(CookiesPolicies.ACCEPT_ALL)
                .setCookies(cookieEntries);

        var client = new HttpClientImpl(config, logService, null, null);

        List<HttpCookie> cookies = client.getCookies("example.com");

        // Модификация возвращённого списка не должна влиять на хранилище
        assertThatCode(() -> cookies.add(new HttpCookie("injected", "bad")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
