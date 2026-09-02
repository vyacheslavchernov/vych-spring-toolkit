package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.common.RandomUtils;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.util.Map;
import java.util.UUID;

import static ru.vych.http.controllers.GetTestController.*;
import static ru.vych.http.impl.common.HttpStatus.OK;

@DisplayName("Тесты отправки GET запросов")
public class HttpClientGetTests extends BaseHttpTest {
    @Test
    @DisplayName("Тест отправки GET запроса без параметров")
    public void getWithoutParamsTest() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_HELLO_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), HELLO_TEXT);
    }

    @Test
    @DisplayName("Тест отправки GET запроса с query параметром")
    public void getWithQueryParamsTest() throws HttpClientException {
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_QUERY_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addQueryParam(UUID_PARAM_KEY, uuid)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), uuid);
    }

    @Test
    @DisplayName("Тест отправки GET запроса с несколькими query параметрами")
    public void getWithManyQueryParamsTest() throws HttpClientException {
        var params = RandomUtils.randomMap(15);
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_MANY_QUERY_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setQueryParams(params)
                .setResponseClass(Map.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyContainsExactlyEntriesOf(rs.getCastedBody(), params);
    }

    @Test
    @DisplayName("Тест отправки GET запроса с некорректными query параметрами")
    public void getWithBrokenQueryParamsTest() throws HttpClientException {
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_MANY_QUERY_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addQueryParam("", "")
                .addQueryParam("a", "")
                .addQueryParam(null, null)
                .addQueryParam("b", null)
                .addQueryParam(UUID_PARAM_KEY, uuid)
                .setResponseClass(Map.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyContainsEntry(rs.getCastedBody(), UUID_PARAM_KEY, uuid);
    }

    @Test
    @DisplayName("Тест отправки GET запроса с path параметром")
    public void getWithPathParamsTest() throws HttpClientException {
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_PATH_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addPathParam(uuid)
                .setResponseClass(String.class)
                .build();

        var rs = sendRequest(rq);
        checkResponseStatus(rs, OK);
        bodyEqualsTo(rs.getBody(), uuid);
    }

    @Test
    @DisplayName("Тест отправки GET запроса с path и query параметрами одновременно")
    public void getWithPathNQueryParamsTest() throws HttpClientException {
        var key = UUID.randomUUID().toString();
        var uuid = UUID.randomUUID().toString();
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_PATH_AND_QUERY_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addPathParam(key)
                .addQueryParam(UUID_PARAM_KEY, uuid)
                .setResponseClass(Map.class)
                .build();

        var rs = sendRequest(rq);

        checkResponseStatus(rs, OK);
        bodyContainsExactlyEntry(rs.getCastedBody(), key, uuid);
    }
}
