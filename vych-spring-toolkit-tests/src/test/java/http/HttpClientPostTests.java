package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.common.RandomUtils;
import ru.vych.http.entities.DummyDto;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qameta.allure.Allure.step;
import static ru.vych.http.controllers.PostTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;
import static ru.vych.http.impl.common.MediaType.*;

/**
 * Тесты отправки POST запросов.
 */
@DisplayName("Тесты отправки POST запросов")
public class HttpClientPostTests extends BaseHttpTest {

    /**
     * Тест отправки POST запроса с пустым телом.
     */
    @Test
    @DisplayName("Тест отправки POST запроса с пустым телом")
    @SneakyThrows
    public void emptyPostTest() {
        var rq = Request.builder()
                .setUrl(POST_CONTROLLER_PATH + EMPTY_POST_ENDPOINT)
                .setMethod(HttpMethod.POST)
                .build();
        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), null);
    }

    /**
     * Тест отправки POST запроса с телом в виде строки.
     */
    @Test
    @DisplayName("Тест отправки POST запроса с телом в виде строки")
    @SneakyThrows
    public void stringPostTest() {
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(POST_CONTROLLER_PATH + STRING_POST_ENDPOINT)
                .setMethod(HttpMethod.POST)
                .setPayload(uuid)
                .setContentType(TEXT_PLAIN)
                .setResponseClass(String.class)
                .build();
        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), uuid);
    }

    /**
     * Тест отправки POST запроса с телом в виде массива байт.
     */
    @Test
    @DisplayName("Тест отправки POST запроса с телом в виде массива байт")
    @SneakyThrows
    public void bytesPostTest() {
        var bytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        var rq = Request.builder()
                .setUrl(POST_CONTROLLER_PATH + BYTES_POST_ENDPOINT)
                .setMethod(HttpMethod.POST)
                .setPayload(bytes)
                .setContentType(APPLICATION_OCTET_STREAM)
                .setResponseClass(byte[].class)
                .build();
        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyContainsExactlyBytes(rs.getRawBytes(), bytes);
    }

    /**
     * Тест отправки POST запроса с телом в виде коллекций.
     */
    @Test
    @DisplayName("Тест отправки POST запроса с телом в виде коллекций")
    public void collectionPostTest() {
        var listPayload = RandomUtils.randomList(15);
        var mapPayload = RandomUtils.randomMap(15);

        step("Проверка List", () -> {
            var rqBuilder = Request.builder()
                    .setUrl(POST_CONTROLLER_PATH + JSON_POST_ENDPOINT)
                    .setMethod(HttpMethod.POST)
                    .setPayload(listPayload).setResponseClass(List.class)
                    .setContentType(APPLICATION_JSON);
            var rs = sendRequest(rqBuilder.build());
            checkResponseStatus(rs, OK);
            bodyContainsExactlyElementsOf(rs.getCastedBody(), listPayload);
        });

        step("Проверка Map", () -> {
            var rqBuilder = Request.builder()
                    .setUrl(POST_CONTROLLER_PATH + JSON_POST_ENDPOINT)
                    .setMethod(HttpMethod.POST)
                    .setContentType(APPLICATION_JSON)
                    .setPayload(mapPayload).setResponseClass(Map.class);
            var rs = sendRequest(rqBuilder.build());
            checkResponseStatus(rs, OK);
            bodyContainsExactlyEntriesOf(rs.getCastedBody(), mapPayload);
        });
    }

    /**
     * Тест отправки POST запроса с телом в DTO.
     */
    @Test
    @DisplayName("Тест отправки POST запроса с телом в DTO")
    @SneakyThrows
    public void dtoPostTest() {
        var dummy = DummyDto.getDummy();
        var rq = Request.builder()
                .setUrl(POST_CONTROLLER_PATH + JSON_POST_ENDPOINT)
                .setMethod(HttpMethod.POST)
                .setPayload(dummy)
                .setContentType(APPLICATION_JSON)
                .setResponseClass(DummyDto.class)
                .build();
        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getCastedBody(), dummy);
    }
}
