package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.ErrorTestController.*;

@DisplayName("Тесты обработки non-2xx ответов сервера")
public class HttpClientNon2xxResponseTests extends BaseHttpTest {

    @Test
    @DisplayName("Тест GET → 400 Bad Request с текстовым телом")
    public void get400BadRequestTest() throws HttpClientException {
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

    @Test
    @DisplayName("Тест GET → 404 Not Found с текстовым телом")
    public void get404NotFoundTest() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_404_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(404);
        bodyEqualsTo(rs.getBody(), NOT_FOUND_TEXT);
    }

    @Test
    @DisplayName("Тест GET → 500 Internal Server Error с текстовым телом")
    public void get500InternalErrorTest() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(ERROR_CONTROLLER_PATH + ERROR_500_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);

        assertThat(rs.getStatus()).isEqualTo(500);
        bodyEqualsTo(rs.getBody(), INTERNAL_SERVER_ERROR_TEXT);
    }

    @Test
    @DisplayName("Тест GET → 403 Forbidden с JSON-телом")
    public void get403ForbiddenJsonTest() throws HttpClientException {
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

    @Test
    @DisplayName("Тест GET → 401 Unauthorized")
    public void get401UnauthorizedTest() throws HttpClientException {
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
