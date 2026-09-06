package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.controllers.GetTestController.GET_CONTROLLER_PATH;
import static ru.vych.http.controllers.GetTestController.GET_HEADERS_ENDPOINT;
import static ru.vych.http.impl.common.HttpStatus.OK;

@DisplayName("Тесты работы с заголовками")
public class HttpClientHeadersTests extends BaseHttpTest {
    private static final String CONFIG_HEADER_NAME = "X-Config-Header";
    private static final String CONFIG_HEADER_VALUE = "config-value";
    private static final String REQUEST_HEADER_NAME = "X-Request-Header";
    private static final String REQUEST_HEADER_VALUE = "request-value";

    /**
     * Проверяет, что заголовки из конфигурации корректно добавляются к запросу.
     */
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка отправки заголовков из конфигурации")
    public void configHeadersAreSent() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_HEADERS_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .setResponseClass(Map.class)
                .build();

        var rs = step("Отправка запроса с заголовками из конфига", () -> sendRequest(rq));

        step("Проверка статуса и заголовка", () -> {
            checkResponseStatus(rs, OK);
            var headers = (Map<String, List<String>>) rs.getCastedBody();
            assertThat(headers)
                    .describedAs("Заголовок из конфига не отправлен")
                    .containsKey(CONFIG_HEADER_NAME.toLowerCase());
            assertThat(headers.get(CONFIG_HEADER_NAME.toLowerCase()))
                    .describedAs("Значение заголовка из конфига не совпадает")
                    .containsExactly(CONFIG_HEADER_VALUE);
        });
    }

    /**
     * Проверяет, что заголовки из запроса добавляются поверх заголовков из конфигурации,
     * если у них разные имена.
     */
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка что заголовки с разными именами не перетирают друг-друга")
    public void requestHeadersWithDifferentNamesMergesWithConfigHeaders() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_HEADERS_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addHeader(REQUEST_HEADER_NAME, REQUEST_HEADER_VALUE)
                .setResponseClass(Map.class)
                .build();

        var rs = step("Отправка запроса с заголовками из конфига и запроса", () -> sendRequest(rq));

        step("Проверка статуса и обоих заголовков", () -> {
            checkResponseStatus(rs, OK);
            var headers = (Map<String, List<String>>) rs.getCastedBody();
            assertThat(headers)
                    .describedAs("Заголовок из конфига не отправлен")
                    .containsKey(CONFIG_HEADER_NAME.toLowerCase());
            assertThat(headers.get(CONFIG_HEADER_NAME.toLowerCase()))
                    .describedAs("Значение заголовка из конфига не совпадает")
                    .containsExactly(CONFIG_HEADER_VALUE);
            assertThat(headers)
                    .describedAs("Заголовок из запроса не отправлен")
                    .containsKey(REQUEST_HEADER_NAME.toLowerCase());
            assertThat(headers.get(REQUEST_HEADER_NAME.toLowerCase()))
                    .describedAs("Значение заголовка из запроса не совпадает")
                    .containsExactly(REQUEST_HEADER_VALUE);
        });
    }

    /**
     * Проверяет, что если в запросе указан заголовок с таким же именем,
     * как и в конфигурации, то значения не перетирают друг-друга,
     * а оба отправляются как значения одного заголовка.
     */
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Проверка что заголовки с одинаковым именем не перетирают друг-друга")
    public void requestHeadersWithSameNameDoesNotOverwriteConfigHeaders() throws HttpClientException {
        var rq = Request.builder()
                .setUrl(GET_CONTROLLER_PATH + GET_HEADERS_ENDPOINT)
                .setMethod(HttpMethod.GET)
                .addHeader(CONFIG_HEADER_NAME, REQUEST_HEADER_VALUE)
                .setResponseClass(Map.class)
                .build();

        var rs = step("Отправка запроса с заголовком с тем же именем, что и в конфиге", () -> sendRequest(rq));

        step("Проверка статуса и обоих значений заголовка", () -> {
            checkResponseStatus(rs, OK);
            var headers = (Map<String, List<String>>) rs.getCastedBody();
            var headerValues = headers.get(CONFIG_HEADER_NAME.toLowerCase());
            assertThat(headerValues)
                    .describedAs("Заголовок с одинаковым именем должен содержать оба значения")
                    .hasSize(2)
                    .contains(CONFIG_HEADER_VALUE, REQUEST_HEADER_VALUE);
        });
    }
}
