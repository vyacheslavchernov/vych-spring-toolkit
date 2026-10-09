package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.PatchTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;
import static ru.vych.http.impl.common.MediaType.APPLICATION_JSON;

/**
 * Интеграционные тесты для PATCH запросов через mock-сервер.
 */
@DisplayName("Тесты отправки PATCH запросов")
public class HttpClientPatchTests extends BaseHttpTest {

    /**
     * Проверяет PATCH запрос с JSON телом.
     */
    @Test
    @DisplayName("Тест отправки PATCH запроса с JSON телом")
    public void patchWithJsonBodyTest() throws HttpClientException {
        var updates = Map.of("name", "updated", "age", 31);
        var rq = Request.builder()
                .setUrl(PATCH_CONTROLLER_PATH + PATCH_JSON_ENDPOINT)
                .setMethod(HttpMethod.PATCH)
                .setPayload(updates)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // JSON сериализация может менять порядок ключей
        assertThat((String) rs.getBody())
                .describedAs("Тело PATCH ответа должно содержать JSON с name и age")
                .contains("\"name\":\"updated\"")
                .contains("\"age\":31");
    }

    /**
     * Проверяет PATCH запрос с JSON телом Map.
     */
    @Test
    @DisplayName("Тест отправки PATCH запроса с JSON телом Map")
    public void patchWithMapBodyTest() throws HttpClientException {
        var updates = Map.of("field", "newValue");
        var rq = Request.builder()
                .setUrl(PATCH_CONTROLLER_PATH + PATCH_JSON_ENDPOINT)
                .setMethod(HttpMethod.PATCH)
                .setPayload(updates)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // JSON сериализация может менять порядок ключей
        assertThat((String) rs.getBody())
                .describedAs("Тело PATCH ответа должно содержать JSON с field")
                .contains("\"field\":\"newValue\"");
    }
}
