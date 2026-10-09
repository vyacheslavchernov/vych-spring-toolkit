package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.ErrorTestController.*;

/**
 * Тесты обработки non-2xx ответов сервера.
 */
@DisplayName("Тесты обработки non-2xx ответов сервера")
public class HttpClientNon2xxResponseTests extends BaseHttpTest {

    /**
     * Тест GET запроса с ошибкой 400 Bad Request.
     */
    @Test
    @DisplayName("Тест GET → 400 Bad Request с текстовым телом")
    @SneakyThrows
    public void get400BadRequestTest() {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_400_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(400);
        bodyEqualsTo(rs.getBody(), BAD_REQUEST_TEXT);
        bodyContainsExactlyBytes(rs.getRawBytes(), BAD_REQUEST_TEXT.getBytes());
    }

    /**
     * Тест GET запроса с ошибкой 404 Not Found.
     */
    @Test
    @DisplayName("Тест GET → 404 Not Found с текстовым телом")
    @SneakyThrows
    public void get404NotFoundTest() {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_404_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(404);
        bodyEqualsTo(rs.getBody(), NOT_FOUND_TEXT);
    }

    /**
     * Тест GET запроса с ошибкой 500 Internal Server Error.
     */
    @Test
    @DisplayName("Тест GET → 500 Internal Server Error с текстовым телом")
    @SneakyThrows
    public void get500InternalErrorTest() {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_500_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(500);
        bodyEqualsTo(rs.getBody(), INTERNAL_SERVER_ERROR_TEXT);
    }

    /**
     * Тест GET запроса с ошибкой 403 Forbidden.
     */
    @Test
    @DisplayName("Тест GET → 403 Forbidden с JSON-телом")
    @SneakyThrows
    public void get403ForbiddenJsonTest() {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_403_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(Map.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(403);
        var castedBody = rs.getCastedBody();
        assertThat(castedBody).isNotNull();
        @SuppressWarnings("unchecked")
        var map = (Map<String, Object>) castedBody;
        assertThat(map).containsEntry(FORBIDDEN_ERROR_KEY, FORBIDDEN_ERROR_VALUE);
        assertThat(map).containsEntry(FORBIDDEN_CODE_KEY, FORBIDDEN_CODE_VALUE);
    }

    /**
     * Тест GET запроса с ошибкой 401 Unauthorized.
     */
    @Test
    @DisplayName("Тест GET → 401 Unauthorized")
    @SneakyThrows
    public void get401UnauthorizedTest() {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_401_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(401);
        bodyEqualsTo(rs.getBody(), UNAUTHORIZED_TEXT);
    }
}
