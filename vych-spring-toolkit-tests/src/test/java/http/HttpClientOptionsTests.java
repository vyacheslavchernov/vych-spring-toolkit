package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.OptionsTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;
import static ru.vych.http.impl.common.MediaType.APPLICATION_JSON;

/**
 * Интеграционные тесты для OPTIONS запросов через mock-сервер.
 */
@DisplayName("Тесты отправки OPTIONS запросов")
public class HttpClientOptionsTests extends BaseHttpTest {

    /**
     * Проверяет OPTIONS запрос без тела — возвращает поддерживаемые методы.
     */
    @Test
    @DisplayName("Тест отправки OPTIONS запроса без тела")
    @SneakyThrows
    public void optionsWithoutBodyTest() {
        var rq = Request.builder()
                .setUrl(OPTIONS_CONTROLLER_PATH + OPTIONS_SIMPLE_ENDPOINT)
                .setMethod(HttpMethod.OPTIONS)
                .setResponseClass(Map.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = rs.getCastedBody();
        step("Проверка allowedMethods", () -> {
            // allowedMethods — это List<String>, bodyContainsEntry не работает с List
            bodyEqualsTo(result.get("allowedMethods"),
                    List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"));
        });
    }

    /**
     * Проверяет OPTIONS запрос с телом.
     */
    @Test
    @DisplayName("Тест отправки OPTIONS запроса с телом")
    @SneakyThrows
    public void optionsWithBodyTest() {
        var payload = Map.of("resource", "users");
        var rq = Request.builder()
                .setUrl(OPTIONS_CONTROLLER_PATH + OPTIONS_WITH_BODY_ENDPOINT)
                .setMethod(HttpMethod.OPTIONS)
                .setPayload(payload)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        // Проверяем, что тело содержит JSON с receivedBody и resourceInfo
        assertThat((String) rs.getBody())
                .describedAs("Тело OPTIONS ответа должно содержать JSON с receivedBody и resourceInfo")
                .contains("\"receivedBody\"")
                .contains("\"resourceInfo\":\"available\"");
    }
}
