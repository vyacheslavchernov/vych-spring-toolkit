package ru.vych.http.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.entities.CookieEntry;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.logger.impl.LogService;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты persistent cookie storage в HttpClientImpl.
 * Проверяют сохранение и загрузку cookies между "созданием клиента".
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты persistent cookie storage HTTP-клиента")
class HttpClientPersistentCookieTests {

    @TempDir
    Path tempDir;

    @Mock
    private LogService logService;

    /**
     * Проверяет, что default cookies загружаются в хранилище при создании.
     */
    @Test
    @DisplayName("Default cookies загружаются при инициализации")
    void defaultCookiesLoadedOnInit() throws HttpClientException, URISyntaxException {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://default.com"), new HttpCookie("default_cookie", "value1"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookies(cookieEntries)
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        assertThat(client.getCookies("default.com"))
                .describedAs("Default cookie должен быть в хранилище")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("default_cookie");
    }

    /**
     * Проверяет, что getAllCookies возвращает immutable map.
     */
    @Test
    @DisplayName("getAllCookies возвращает immutable map")
    void getAllCookiesReturnsImmutableMap() throws HttpClientException, URISyntaxException {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookies(cookieEntries)
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        Map<String, List<HttpCookie>> allCookies = client.getAllCookies();

        assertThatCode(() -> allCookies.put("other.com", List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Проверяет, что clearCookies(host) не удаляет cookies других хостов.
     */
    @Test
    @DisplayName("clearCookies удаляет только cookies указанного хоста")
    void clearCookiesOnlyRemovesSpecifiedHost() throws HttpClientException, URISyntaxException {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://host1.com"), new HttpCookie("cookie1", "v1")),
                new CookieEntry(new URI("https://host2.com"), new HttpCookie("cookie2", "v2"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookies(cookieEntries)
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        client.clearCookies("host1.com");

        assertThat(client.getCookies("host1.com"))
                .isEmpty();
        assertThat(client.getCookies("host2.com"))
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("cookie2");
    }

    /**
     * Проверяет, что NullPointerException бросается при host == null.
     */
    @Test
    @DisplayName("getCookies бросает NPE при host == null")
    void getCookiesThrowsNpeOnNullHost() throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        assertThatCode(() -> client.getCookies(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Host cannot be null in getCookies()");
    }

    /**
     * Проверяет, что clearCookies бросает NPE при host == null.
     */
    @Test
    @DisplayName("clearCookies бросает NPE при host == null")
    void clearCookiesThrowsNpeOnNullHost() throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        assertThatCode(() -> client.clearCookies(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Host cannot be null in clearCookies()");
    }

    /**
     * Проверяет, что getCookies возвращает копию списка.
     */
    @Test
    @DisplayName("getCookies возвращает копию списка")
    void getCookiesReturnsCopy() throws HttpClientException, URISyntaxException {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookies(cookieEntries)
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        List<HttpCookie> cookies = client.getCookies("example.com");

        // Модификация возвращённого списка не должна влиять на хранилище
        assertThatCode(() -> cookies.add(new HttpCookie("injected", "bad")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Проверяет, что getCookies фильтрует cookies по TTL.
     */
    @Test
    @DisplayName("getCookies фильтрует cookies по TTL")
    void getCookiesFiltersByTtl() throws HttpClientException, URISyntaxException {
        // Cookie с maxAge > 0 будет сохранён с timestamp
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("persistent", "value"))
        );
        cookieEntries.get(0).getCookie().setMaxAge(3600L);

        HttpClientConfig config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setCookies(cookieEntries)
                .setCookieStorageDir(tempDir);

        var client = new HttpClientImpl(config, logService, null, null);

        // Cookie с валидным TTL должен быть возвращён
        assertThat(client.getCookies("example.com"))
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("persistent");
    }
}
