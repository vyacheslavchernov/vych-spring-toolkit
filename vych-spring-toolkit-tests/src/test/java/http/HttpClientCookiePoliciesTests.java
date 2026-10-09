package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.config.TestServerCookieClientConfiguration.*;
import static ru.vych.http.controllers.CookieTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;

/**
 * Тесты cookie-политик.
 */
@DisplayName("Тесты cookie-политик")
public class HttpClientCookiePoliciesTests extends BaseHttpTest {

    /** HTTP клиент с политикой ACCEPT_ALL. */
    @Autowired
    @Qualifier(COOKIE_ACCEPT_ALL_CLIENT_SERVICE_CODE)
    private HttpClient cookieAcceptAllClient;

    /** HTTP клиент с политикой ACCEPT_NONE. */
    @Autowired
    @Qualifier(COOKIE_ACCEPT_NONE_CLIENT_SERVICE_CODE)
    private HttpClient cookieAcceptNoneClient;

    /** HTTP клиент с политикой ACCEPT_ORIGINAL_SERVER. */
    @Autowired
    @Qualifier(COOKIE_ORIGINAL_SERVER_CLIENT_SERVICE_CODE)
    private HttpClient cookieOriginalServerClient;

    /** HTTP клиент с дефолтными cookies. */
    @Autowired
    @Qualifier(COOKIE_WITH_DEFAULTS_CLIENT_SERVICE_CODE)
    private HttpClient cookieWithDefaultsClient;

    /** HTTP клиент с ACCEPT_NONE и дефолтными cookies. */
    @Autowired
    @Qualifier(COOKIE_ACCEPT_NONE_WITH_DEFAULTS_CLIENT_SERVICE_CODE)
    private HttpClient cookieAcceptNoneWithDefaultsClient;

    /**
     * Тест получения cookie через политику ACCEPT_ALL.
     */
    @Test
    @DisplayName("CookiePolicy ACCEPT_ALL — получение cookie от сервера")
    @SneakyThrows
    public void acceptAllGetCookieTest() {
        // Отправляем GET на /cookieTest/set
        var setRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_SET_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var setResponse = sendRequest(setRequest, cookieAcceptAllClient);
        checkResponseStatus(setResponse, OK);

        // Отправляем GET на /cookieTest/check
        var checkRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_CHECK_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(Map.class)
                .build();

        var checkResponse = sendRequest(checkRequest, cookieAcceptAllClient);
        checkResponseStatus(checkResponse, OK);
        bodyContainsEntry(checkResponse.getCastedBody(), "hasCookie", true);
    }

    /**
     * Тест отсутствия cookies через политику ACCEPT_NONE.
     */
    @Test
    @DisplayName("CookiePolicy ACCEPT_NONE — не принимать cookies")
    @SneakyThrows
    public void acceptNoneDontStoreCookieTest() {
        // Отправляем GET на /cookieTest/set
        var setRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_SET_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var setResponse = sendRequest(setRequest, cookieAcceptNoneClient);
        checkResponseStatus(setResponse, OK);

        // Отправляем GET на /cookieTest/check
        var checkRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_CHECK_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(Map.class)
                .build();

        var checkResponse = sendRequest(checkRequest, cookieAcceptNoneClient);
        checkResponseStatus(checkResponse, OK);
        bodyContainsEntry(checkResponse.getCastedBody(), "hasCookie", false);
    }

    /**
     * Тест получения cookie через политику ACCEPT_ORIGINAL_SERVER.
     */
    @Test
    @DisplayName("CookiePolicy ACCEPT_ORIGINAL_SERVER — принимать только от оригинала")
    @SneakyThrows
    public void acceptOriginalServerGetCookieTest() {
        // Отправляем GET на /cookieTest/set (прямой запрос к тестовому серверу)
        var setRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_SET_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var setResponse = sendRequest(setRequest, cookieOriginalServerClient);
        checkResponseStatus(setResponse, OK);

        // Отправляем GET на /cookieTest/check
        var checkRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_CHECK_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(Map.class)
                .build();

        var checkResponse = sendRequest(checkRequest, cookieOriginalServerClient);
        checkResponseStatus(checkResponse, OK);
        bodyContainsEntry(checkResponse.getCastedBody(), "hasCookie", true);
    }

    /**
     * Тест отправки дефолтных cookies при запросе.
     */
    @Test
    @DisplayName("Дефолтные cookies — отправка при запросе")
    @SneakyThrows
    public void defaultCookiesSentTest() {
        // Отправляем GET на /cookieTest/echo
        var echoRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_ECHO_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var echoResponse = sendRequest(echoRequest, cookieWithDefaultsClient);
        checkResponseStatus(echoResponse, OK);

        // Проверяем, что сервер получил дефолтный cookie
        String bodyString = new String(echoResponse.getRawBytes());
        assertThat(bodyString)
                .describedAs("Сервер должен получить дефолтный cookie default=value")
                .contains("default");
    }

    /**
     * Тест отправки дефолтных cookies при политике ACCEPT_NONE.
     */
    @Test
    @DisplayName("CookiePolicy ACCEPT_NONE — дефолтные cookies отправляются")
    @SneakyThrows
    public void acceptNoneWithDefaultsDontSendTest() {
        // Отправляем GET на /cookieTest/echo
        var echoRequest = Request.builder()
                .setUrl(COOKIE_TEST_PATH + COOKIE_TEST_ECHO_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var echoResponse = sendRequest(echoRequest, cookieAcceptNoneWithDefaultsClient);
        checkResponseStatus(echoResponse, OK);

        // Проверяем, что сервер получил дефолтные cookies
        // CookiePolicy.ACCEPT_NONE влияет только на приём cookies от сервера,
        // но не на отправку дефолтных cookies из CookieStore
        String bodyString = new String(echoResponse.getRawBytes());
        assertThat(bodyString)
                .describedAs("Сервер должен получить дефолтные cookies")
                .contains("default");
    }
}
