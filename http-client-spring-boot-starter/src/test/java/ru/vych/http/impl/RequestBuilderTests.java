package ru.vych.http.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientInvalidRequestException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.REQUEST_ERROR_INVALID_CONTENT_TYPE;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.REQUEST_ERROR_INVALID_METHOD;

/**
 * Тесты для билдера {@link Request.Builder}, включая валидацию обязательных параметров,
 * обработку query/path-параметров, заголовков и контента-типа.
 */
@DisplayName("Тесты для билдера Request.Builder")
public class RequestBuilderTests {

    /**
     * Проверяет, что {@code build()} без указания HTTP-метода
     * выбрасывает {@link HttpClientInvalidRequestException}.
     */
    @Test
    @DisplayName("buildWithoutMethodThrows")
    public void buildWithoutMethodThrows() {
        var builder = Request.builder().setUrl("/api/test");

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при отсутствии метода")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_METHOD);
    }

    /**
     * Проверяет, что POST-запрос с телом (payload) и без Content-Type
     * вызывает {@link HttpClientInvalidRequestException}.
     */
    @Test
    @DisplayName("buildPostWithPayloadWithoutContentTypeThrows")
    public void buildPostWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.POST)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"));

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при POST + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }

    /**
     * Проверяет, что POST-запрос с телом и установленным Content-Type
     * успешно создаёт {@link Request}.
     */
    @Test
    @DisplayName("buildPostWithPayloadWithContentTypeSucceeds")
    public void buildPostWithPayloadWithContentTypeSucceeds() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.POST)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"))
                .setContentType("application/json")
                .build();

        assertThat(request)
                .describedAs("Запрос должен быть создан успешно")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.POST);
    }

    /**
     * Проверяет создание минимально валидного GET-запроса (только метод + URL).
     */
    @Test
    @DisplayName("buildMinimalValidRequest")
    public void buildMinimalValidRequest() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .build();

        assertThat(request)
                .describedAs("Минимальный запрос должен быть создан")
                .isNotNull()
                .extracting(Request::getMethod, Request::getUrl)
                .containsExactly(HttpMethod.GET, "/api/test");
    }

    /**
     * Проверяет, что {@code setQueryParams} удаляет записи с ключом {@code null}.
     */
    @Test
    @DisplayName("queryParamsRemovesNullKeys")
    public void queryParamsRemovesNullKeys() throws HttpClientInvalidRequestException {
        var params = new HashMap<String, String>();
        params.put("validKey", "validValue");
        params.put(null, "nullValue");

        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .setQueryParams(params)
                .build();

        assertThat(request.getQueryParams().keySet())
                .describedAs("QueryParams не должны содержать null-ключи")
                .doesNotContain((String) null)
                .hasSize(1)
                .contains("validKey");
    }

    /**
     * Проверяет, что {@code addQueryParam} с ключом {@code null} ничего не добавляет.
     */
    @Test
    @DisplayName("addQueryParamWithNullKeyDoesNothing")
    public void addQueryParamWithNullKeyDoesNothing() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .addQueryParam("realKey", "realValue")
                .addQueryParam(null, "nullValue")
                .build();

        assertThat(request.getQueryParams())
                .describedAs("QueryParams не должны содержать параметр с null-ключом")
                .hasSize(1)
                .containsEntry("realKey", "realValue");
    }

    /**
     * Проверяет, что {@code addPathParam} добавляет path-параметр в список.
     */
    @Test
    @DisplayName("addPathParamAppendsToList")
    public void addPathParamAppendsToList() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/users")
                .addPathParam("42")
                .addPathParam("profile")
                .build();

        assertThat(request.getPathParams())
                .describedAs("PathParams не соответствуют ожидаемым")
                .hasSize(2)
                .containsExactly("42", "profile");
    }

    /**
     * Проверяет, что {@code addHeader} добавляет заголовок в список.
     */
    @Test
    @DisplayName("addHeaderAddsHeaderToList")
    public void addHeaderAddsHeaderToList() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .addHeader("X-Custom-Header", "custom-value")
                .addHeader("X-Another-Header", "another-value")
                .build();

        assertThat(request.getHeaders())
                .describedAs("Headers не соответствуют ожидаемым")
                .hasSize(2)
                .containsExactly(
                        new Header("X-Custom-Header", "custom-value"),
                        new Header("X-Another-Header", "another-value")
                );
    }

    /**
     * Проверяет, что {@code build()} с установленным {@code contentType}
     * автоматически добавляет заголовок {@code Content-Type}.
     */
    @Test
    @DisplayName("buildWithContentTypeAddsHeader")
    public void buildWithContentTypeAddsHeader() throws HttpClientInvalidRequestException {
        var request = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .setContentType("application/json")
                .build();

        var contentTypeHeader = request.getHeaders().stream()
                .filter(h -> "Content-Type".equalsIgnoreCase(h.name()))
                .findFirst();

        assertThat(contentTypeHeader)
                .describedAs("Content-Type заголовок должен быть добавлен автоматически")
                .isPresent()
                .hasValueSatisfying(h -> assertThat(h.value()).isEqualTo("application/json"));
    }
}
