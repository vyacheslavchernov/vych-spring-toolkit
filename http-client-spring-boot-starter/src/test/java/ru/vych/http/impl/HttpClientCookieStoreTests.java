package ru.vych.http.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.entities.CookieEntry;
import ru.vych.logger.impl.LogService;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты внутреннего хранилища cookies HTTP-клиента.
 * Проверяют изоляцию, добавление дефолтных cookies, очистку и получение всего хранилища.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты cookie store HTTP-клиента")
class HttpClientCookieStoreTests {
    @Mock
    private LogService logService;

    private HttpClientConfig config;

    @BeforeEach
    void setUp() {
        config = new HttpClientConfig("TestClient")
                .setRoot("http://localhost:8080");
    }

    /**
     * Проверяет, что дефолтные cookies корректно добавляются во внутреннее хранилище.
     */
    @Test
    @DisplayName("Проверка работы cookie store с дефолтными cookies")
    @SneakyThrows
    public void cookieStoreWithDefaultCookies() {
        List<CookieEntry> cookieEntries = List.of(
                new CookieEntry(new URI("https://example1.com"), new HttpCookie("test1", "test1")),
                new CookieEntry(new URI("https://example2.com"), new HttpCookie("test2", "test2")),
                new CookieEntry(new URI("https://example3.com"), new HttpCookie("test3", "test3"))
        );

        var client = new HttpClientImpl(
                new HttpClientConfig("TestClient").setCookies(cookieEntries),
                logService, null, null
        );

        // Проверяем cookies для example1.com
        assertThat(client.getCookies("example1.com"))
                .describedAs("CookieStore не содержит ожидаемых cookies для example1.com")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("test1");

        // Проверяем cookies для example2.com
        assertThat(client.getCookies("example2.com"))
                .describedAs("CookieStore не содержит ожидаемых cookies для example2.com")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("test2");

        // Проверяем cookies для example3.com
        assertThat(client.getCookies("example3.com"))
                .describedAs("CookieStore не содержит ожидаемых cookies для example3.com")
                .hasSize(1)
                .extracting(HttpCookie::getName)
                .containsExactly("test3");
    }

    /**
     * Проверяет, что cookies изолированы между разными клиентами.
     */
    @Test
    @DisplayName("Проверка изоляции cookies между клиентами")
    @SneakyThrows
    public void cookieIsolationBetweenClients() {
        List<CookieEntry> cookies1 = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("client", "1"))
        );
        List<CookieEntry> cookies2 = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("client", "2"))
        );

        var client1 = new HttpClientImpl(
                new HttpClientConfig("Client1").setCookies(cookies1),
                logService, null, null
        );
        var client2 = new HttpClientImpl(
                new HttpClientConfig("Client2").setCookies(cookies2),
                logService, null, null
        );

        // Клиент 1 имеет свой cookie
        assertThat(client1.getCookies("example.com"))
                .extracting(HttpCookie::getValue)
                .containsExactly("1");

        // Клиент 2 имеет свой cookie
        assertThat(client2.getCookies("example.com"))
                .extracting(HttpCookie::getValue)
                .containsExactly("2");
    }

    /**
     * Проверяет метод очистки cookies.
     */
    @Test
    @DisplayName("Проверка очистки cookies")
    @SneakyThrows
    public void clearCookies() {
        List<CookieEntry> cookies = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        var client = new HttpClientImpl(
                new HttpClientConfig("TestClient").setCookies(cookies),
                logService, null, null
        );

        assertThat(client.getCookies("example.com"))
                .hasSize(1);

        client.clearCookies("example.com");

        assertThat(client.getCookies("example.com"))
                .isEmpty();
    }

    /**
     * Проверяет метод получения всего хранилища cookies.
     */
    @Test
    @DisplayName("Проверка получения всего хранилища cookies")
    @SneakyThrows
    public void getAllCookies() {
        List<CookieEntry> cookies = List.of(
                new CookieEntry(new URI("https://example1.com"), new HttpCookie("a", "1")),
                new CookieEntry(new URI("https://example2.com"), new HttpCookie("b", "2"))
        );

        var client = new HttpClientImpl(
                new HttpClientConfig("TestClient").setCookies(cookies),
                logService, null, null
        );

        Map<String, List<HttpCookie>> allCookies = client.getAllCookies();

        assertThat(allCookies)
                .describedAs("Все cookies должны содержать 2 записи")
                .hasSize(2)
                .containsKeys("example1.com", "example2.com");

        assertThat(allCookies.get("example1.com"))
                .extracting(HttpCookie::getValue)
                .containsExactly("1");

        assertThat(allCookies.get("example2.com"))
                .extracting(HttpCookie::getValue)
                .containsExactly("2");
    }

    /**
     * Проверяет, что getAllCookies возвращает неизменяемую карту.
     */
    @Test
    @DisplayName("getAllCookies возвращает неизменяемую карту")
    @SneakyThrows
    public void getAllCookiesIsImmutable() {
        List<CookieEntry> cookies = List.of(
                new CookieEntry(new URI("https://example.com"), new HttpCookie("test", "value"))
        );

        var client = new HttpClientImpl(
                new HttpClientConfig("TestClient").setCookies(cookies),
                logService, null, null
        );

        Map<String, List<HttpCookie>> allCookies = client.getAllCookies();

        assertThatThrownBy(() -> allCookies.put("other.com", List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Проверяет, что getAllCookies пуст для клиента без дефолтных cookies.
     */
    @Test
    @DisplayName("getAllCookies пуст для клиента без cookies")
    @SneakyThrows
    public void getAllCookiesEmptyWhenNoCookies() {
        var client = new HttpClientImpl(
                new HttpClientConfig("TestClient").setCookies(List.of()),
                logService, null, null
        );

        assertThat(client.getAllCookies())
                .isEmpty();
    }
}
