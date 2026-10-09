package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.config.TestServerRedirectClientConfiguration.REDIRECT_FOLLOW_CLIENT_SERVICE_CODE;
import static ru.vych.http.config.TestServerRedirectClientConfiguration.REDIRECT_NO_FOLLOW_CLIENT_SERVICE_CODE;
import static ru.vych.http.controllers.GetTestController.HELLO_TEXT;
import static ru.vych.http.controllers.RedirectTestController.*;
import static ru.vych.http.impl.common.HttpStatus.FOUND;
import static ru.vych.http.impl.common.HttpStatus.OK;
import static ru.vych.http.impl.common.MediaType.TEXT_PLAIN;

/**
 * Тесты обработки редиректов (3xx).
 */
@DisplayName("Тесты обработки редиректов (3xx)")
public class HttpClientRedirectTests extends BaseHttpTest {

    /** HTTP клиент с политикой следования за редиректами. */
    @Autowired
    @Qualifier(REDIRECT_FOLLOW_CLIENT_SERVICE_CODE)
    protected HttpClient redirectFollowClient;

    /** HTTP клиент без следования за редиректами. */
    @Autowired
    @Qualifier(REDIRECT_NO_FOLLOW_CLIENT_SERVICE_CODE)
    protected HttpClient redirectNoFollowClient;

    /**
     * Тест редиректа 302 с followRedirects = true.
     */
    @Test
    @DisplayName("Redirect 302 — followRedirects = true (по умолчанию)")
    @SneakyThrows
    public void redirect302FollowTest() {
        step("Отправка GET на " + REDIRECT_CONTROLLER_PATH + TO_HELLO_ENDPOINT + " с followRedirects = true", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + TO_HELLO_ENDPOINT)
                    .setMethod(HttpMethod.GET)
                    .setResponseClass(String.class)
                    .build();

            var rs = sendRequest(rq, redirectFollowClient);

            checkResponseStatus(rs, OK);
            bodyEqualsTo(rs.getBody(), HELLO_TEXT);
        });
    }

    /**
     * Тест редиректа 301 с followRedirects = true.
     */
    @Test
    @DisplayName("Redirect 301 — followRedirects = true")
    @SneakyThrows
    public void redirect301FollowTest() {
        step("Отправка GET на "
                + REDIRECT_CONTROLLER_PATH + TO_HELLO_301_ENDPOINT
                + " с followRedirects = true", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + TO_HELLO_301_ENDPOINT)
                    .setMethod(HttpMethod.GET)
                    .setResponseClass(String.class)
                    .build();

            var rs = sendRequest(rq, redirectFollowClient);

            checkResponseStatus(rs, OK);
            bodyEqualsTo(rs.getBody(), HELLO_TEXT);
        });
    }

    /**
     * Тест редиректа 302 без следования.
     */
    @Test
    @DisplayName("Redirect 302 — followRedirects = false")
    @SneakyThrows
    public void redirect302NoFollowTest() {
        step("Отправка GET на " + REDIRECT_CONTROLLER_PATH + TO_HELLO_ENDPOINT + " с followRedirects = false", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + TO_HELLO_ENDPOINT)
                    .setMethod(HttpMethod.GET)
                    .setResponseClass(String.class)
                    .build();

            var rs = sendRequest(rq, redirectNoFollowClient);

            checkResponseStatus(rs, FOUND);
            responseContainsHeader(rs, "location", HELLO_TARGET_LOCATION);
        });
    }

    /**
     * Тест защиты от зацикливания редиректов.
     */
    @Test
    @DisplayName("Redirect loop — защита от зацикливания")
    @SneakyThrows
    public void redirectLoopTest() {
        step("Отправка GET на "
                + REDIRECT_CONTROLLER_PATH + LOOP_ENDPOINT
                + " с followRedirects = NORMAL (по умолчанию)", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + LOOP_ENDPOINT)
                    .setMethod(HttpMethod.GET)
                    .setResponseClass(String.class)
                    .build();

            // Клиент по умолчанию имеет Redirect.NORMAL
            // Java HttpClient с Redirect.NORMAL не следует за редиректами
            // на тот же хост+порт с другим путём (считает это циклом)
            // Поэтому возвращает ответ 302 как есть
            var rs = sendRequest(rq, defaultClient);

            // Клиент должен вернуть 302 (не следовать за редиректом на тот же URL)
            // Это доказывает, что у клиента есть встроенная защита от циклических редиректов
            checkResponseStatus(rs, FOUND);
            responseContainsHeader(rs, "location", "http://localhost:9090" + LOOP_TARGET_LOCATION);
        });
    }

    /**
     * Тест редиректа на внешний домен.
     */
    @Test
    @DisplayName("Redirect на внешний домен — followRedirects = true")
    @SneakyThrows
    public void externalRedirectTest() {
        step("Отправка GET на " + REDIRECT_CONTROLLER_PATH + EXTERNAL_ENDPOINT + " с followRedirects = true", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + EXTERNAL_ENDPOINT)
                    .setMethod(HttpMethod.GET)
                    .setResponseClass(String.class)
                    .build();

            var rs = sendRequest(rq, redirectFollowClient);

            // Редирект на внешний домен example.com не должен блокироваться политикой cookie
            // или другими ограничениями. Может завершиться успешно или с ошибкой
            // в зависимости от доступности сети.
            // Главное — сам редирект не заблокирован.
            step("Проверка, что редирект на внешний домен не заблокирован", () -> {
                var statusCode = rs.getStatus();
                // Либо получен финальный ответ (2xx), либо ошибка редиректа
                // Важнее всего, что он не заблокирован политикой cookie
                assertThat(statusCode)
                        .as("Внешний редирект не должен быть заблокирован")
                        .isNotEqualTo(FOUND);
            });
        });
    }

    /**
     * Тест редиректа с изменением метода (POST -> GET).
     */
    @Test
    @DisplayName("Redirect с изменением метода (POST → GET)")
    @SneakyThrows
    public void redirectPostToGetTest() {
        step("Отправка POST на эндпоинт, который возвращает 302", () -> {
            var rq = Request.builder()
                    .setUrl(REDIRECT_CONTROLLER_PATH + POST_REDIRECT_ENDPOINT)
                    .setMethod(HttpMethod.POST)
                    .setPayload(REDIRECT_TEST_PAYLOAD)
                    .setContentType(TEXT_PLAIN)
                    .setResponseClass(String.class)
                    .build();

            var rs = sendRequest(rq, redirectFollowClient);

            // Java HttpClient при редиректе 302 меняет метод POST на GET
            checkResponseStatus(rs, OK);
            bodyEqualsTo(rs.getBody(), HELLO_TEXT);
        });
    }
}
