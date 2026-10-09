package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.HeadTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;

/**
 * Интеграционные тесты для HEAD запросов через mock-сервер.
 */
@DisplayName("Тесты отправки HEAD запросов")
public class HttpClientHeadTests extends BaseHttpTest {

    /**
     * Проверяет HEAD запрос — возвращает только заголовки без тела.
     */
    @Test
    @DisplayName("Тест отправки HEAD запроса без тела")
    @SneakyThrows
    public void headWithoutBodyTest() {
        var rq = Request.builder()
                .setUrl(HEAD_CONTROLLER_PATH + HEAD_SIMPLE_ENDPOINT)
                .setMethod(HttpMethod.HEAD)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // HEAD возвращает только заголовки, тело пустое
        assertThat(rs.getRawBody())
                .describedAs("Тело HEAD запроса должно быть пустым")
                .isEmpty();
    }

    /**
     * Проверяет HEAD запрос с заголовками — сервер возвращает заголовки в ответе.
     */
    @Test
    @DisplayName("Тест отправки HEAD запроса с проверкой заголовков")
    @SneakyThrows
    public void headWithHeadersTest() {
        var rq = Request.builder()
                .setUrl(HEAD_CONTROLLER_PATH + HEAD_WITH_HEADERS_ENDPOINT)
                .setMethod(HttpMethod.HEAD)
                .addHeader("X-Custom-Header", "test-value")
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // Проверяем, что заголовки ответа присутствуют
        assertThat(rs.getHeaders())
                .describedAs("Заголовки ответа не должны быть пустыми")
                .isNotEmpty();
    }
}
