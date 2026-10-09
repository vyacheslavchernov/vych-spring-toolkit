package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.entities.DummyDto;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.util.Map;
import java.util.UUID;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.PutTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;
import static ru.vych.http.impl.common.MediaType.APPLICATION_JSON;

/**
 * Интеграционные тесты для PUT запросов через mock-сервер.
 */
@DisplayName("Тесты отправки PUT запросов")
public class HttpClientPutTests extends BaseHttpTest {

    /**
     * Проверяет PUT запрос с телом в виде строки.
     */
    @Test
    @DisplayName("Тест отправки PUT запроса с телом в виде строки")
    public void putWithStringBodyTest() throws HttpClientException {
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(PUT_CONTROLLER_PATH + PUT_STRING_ENDPOINT)
                .setMethod(HttpMethod.PUT)
                .setPayload(uuid)
                .setContentType("text/plain")
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), uuid);
    }

    /**
     * Проверяет PUT запрос с JSON телом.
     */
    @Test
    @DisplayName("Тест отправки PUT запроса с JSON телом")
    public void putWithJsonBodyTest() throws HttpClientException {
        var dummy = DummyDto.getDummy();
        var rq = Request.builder()
                .setUrl(PUT_CONTROLLER_PATH + PUT_JSON_ENDPOINT)
                .setMethod(HttpMethod.PUT)
                .setPayload(dummy)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(DummyDto.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getCastedBody(), dummy);
    }

    /**
     * Проверяет PUT запрос с JSON телом DTO.
     */
    @Test
    @DisplayName("Тест отправки PUT запроса с JSON телом DTO")
    public void putWithDtoBodyTest() throws HttpClientException {
        var dummy = DummyDto.getDummy();
        var rq = Request.builder()
                .setUrl(PUT_CONTROLLER_PATH + PUT_JSON_ENDPOINT)
                .setMethod(HttpMethod.PUT)
                .setPayload(dummy)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(DummyDto.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getCastedBody(), dummy);
    }
}
