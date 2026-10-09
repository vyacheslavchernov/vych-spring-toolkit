package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.DeleteTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;

/**
 * Интеграционные тесты для DELETE запросов через mock-сервер.
 */
@DisplayName("Тесты отправки DELETE запросов")
public class HttpClientDeleteTests extends BaseHttpTest {

    /**
     * Проверяет DELETE запрос без тела.
     */
    @Test
    @DisplayName("Тест отправки DELETE запроса без тела")
    @SneakyThrows
    public void deleteWithoutBodyTest() {
        var rq = Request.builder()
                .setUrl(DELETE_CONTROLLER_PATH + DELETE_WITHOUT_BODY_ENDPOINT)
                .setMethod(HttpMethod.DELETE)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // Проверяем, что тело содержит JSON с deletedId и success
        assertThat((String) rs.getBody())
                .describedAs("Тело DELETE ответа должно содержать JSON")
                .contains("\"deletedId\"")
                .contains("\"success\":true");
    }
}
