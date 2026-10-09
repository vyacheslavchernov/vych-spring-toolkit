package http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.interceptors.CustomRequestInterceptor;
import ru.vych.http.interceptors.CustomResponseInterceptor;

import static ru.vych.http.controllers.GetTestController.GET_CONTROLLER_PATH;
import static ru.vych.http.controllers.GetTestController.GET_HELLO_ENDPOINT;
import static ru.vych.http.impl.common.HttpStatus.OK;

/**
 * Тесты перехватчиков запросов Http-клиента.
 */
@DisplayName("Тесты перехватчиков запросов Http-клиента")
public class HttpClientInterceptorsTests extends BaseHttpTest {

    /**
     * Отключает перехватчики после каждого теста.
     */
    @AfterEach
    public void afterEach() {
        CustomRequestInterceptor.disable();
        CustomResponseInterceptor.disable();
    }

    /**
     * Тест перехвата запроса/ответа с помощью перехватчиков.
     */
    @Test
    @DisplayName("Тест перехвата запроса/ответа с помощью перехватчиков")
    @SneakyThrows
    public void interceptTest() {
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_HELLO_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        CustomRequestInterceptor.enable(rq.getUuid());
        CustomResponseInterceptor.enable(rq.getUuid());

        var rs = sendRequest(rq);

        checkResponseStatus(rs, OK);
        requestContainsHeader(
                rs.getRequest(),
                CustomRequestInterceptor.HEADER_NAME,
                CustomRequestInterceptor.HEADER_VALUE
        );
        responseContainsHeader(
                rs,
                CustomResponseInterceptor.HEADER_NAME,
                CustomResponseInterceptor.HEADER_VALUE
        );
    }
}
