package ru.vych.http.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientInvalidRequestException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.REQUEST_ERROR_INVALID_CONTENT_TYPE;

/**
 * Тесты валидации Content-Type для всех HTTP-методов, поддерживающих payload.
 * <p>
 * По спецификации: запрос с payload без Content-Type должен бросать
 * {@link HttpClientInvalidRequestException} для всех методов, кроме GET и HEAD.
 * </p>
 *
 * @see HttpClientImpl
 * @see Request.Builder
 */
@DisplayName("Тесты валидации Content-Type для HTTP-методов")
public class HttpClientMethodValidationTests {

    /**
     * Проверяет, что PUT-запрос с payload и без Content-Type бросает исключение.
     */
    @Test
    @DisplayName("buildPutWithPayloadWithoutContentTypeThrows")
    public void buildPutWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.PUT)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"));

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при PUT + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }

    /**
     * Проверяет, что PUT-запрос с payload и Content-Type успешно создаётся.
     */
    @Test
    @DisplayName("buildPutWithPayloadWithContentTypeSucceeds")
    @SneakyThrows
    public void buildPutWithPayloadWithContentTypeSucceeds() {
        var request = Request.builder()
                .setMethod(HttpMethod.PUT)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"))
                .setContentType("application/json")
                .build();

        assertThat(request)
                .describedAs("Запрос PUT с Content-Type должен быть создан успешно")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.PUT);
    }

    /**
     * Проверяет, что DELETE-запрос с payload и без Content-Type бросает исключение.
     */
    @Test
    @DisplayName("buildDeleteWithPayloadWithoutContentTypeThrows")
    public void buildDeleteWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.DELETE)
                .setUrl("/api/test")
                .setPayload(Map.of("id", "42"));

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при DELETE + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }

    /**
     * Проверяет, что DELETE-запрос с payload и Content-Type успешно создаётся.
     */
    @Test
    @DisplayName("buildDeleteWithPayloadWithContentTypeSucceeds")
    @SneakyThrows
    public void buildDeleteWithPayloadWithContentTypeSucceeds() {
        var request = Request.builder()
                .setMethod(HttpMethod.DELETE)
                .setUrl("/api/test")
                .setPayload(Map.of("id", "42"))
                .setContentType("application/json")
                .build();

        assertThat(request)
                .describedAs("Запрос DELETE с Content-Type должен быть создан успешно")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.DELETE);
    }

    /**
     * Проверяет, что PATCH-запрос с payload и без Content-Type бросает исключение.
     */
    @Test
    @DisplayName("buildPatchWithPayloadWithoutContentTypeThrows")
    public void buildPatchWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.PATCH)
                .setUrl("/api/test")
                .setPayload(Map.of("field", "value"));

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при PATCH + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }

    /**
     * Проверяет, что PATCH-запрос с payload и Content-Type успешно создаётся.
     */
    @Test
    @DisplayName("buildPatchWithPayloadWithContentTypeSucceeds")
    @SneakyThrows
    public void buildPatchWithPayloadWithContentTypeSucceeds() {
        var request = Request.builder()
                .setMethod(HttpMethod.PATCH)
                .setUrl("/api/test")
                .setPayload(Map.of("field", "value"))
                .setContentType("application/json")
                .build();

        assertThat(request)
                .describedAs("Запрос PATCH с Content-Type должен быть создан успешно")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.PATCH);
    }

    /**
     * Проверяет, что HEAD-запрос с payload успешно создаётся (HEAD игнорирует тело).
     * <p>
     * HEAD игнорирует тело запроса согласно HTTP-протоколу, поэтому Content-Type не требуется.
     * </p>
     */
    @Test
    @DisplayName("buildHeadWithPayloadSucceedsBecauseHeadIgnoresBody")
    @SneakyThrows
    public void buildHeadWithPayloadSucceedsBecauseHeadIgnoresBody() {
        var request = Request.builder()
                .setMethod(HttpMethod.HEAD)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"))
                .build();

        assertThat(request)
                .describedAs("HEAD-запрос должен создаваться даже с payload (игнорирует тело)")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.HEAD);
    }

    /**
     * Проверяет, что OPTIONS-запрос с payload и без Content-Type бросает исключение.
     */
    @Test
    @DisplayName("buildOptionsWithPayloadWithoutContentTypeThrows")
    public void buildOptionsWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.OPTIONS)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"));

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при OPTIONS + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }

    /**
     * Проверяет, что OPTIONS-запрос с payload и Content-Type успешно создаётся.
     */
    @Test
    @DisplayName("buildOptionsWithPayloadWithContentTypeSucceeds")
    @SneakyThrows
    public void buildOptionsWithPayloadWithContentTypeSucceeds() {
        var request = Request.builder()
                .setMethod(HttpMethod.OPTIONS)
                .setUrl("/api/test")
                .setPayload(Map.of("key", "value"))
                .setContentType("application/json")
                .build();

        assertThat(request)
                .describedAs("Запрос OPTIONS с Content-Type должен быть создан успешно")
                .isNotNull()
                .extracting(Request::getMethod)
                .isEqualTo(HttpMethod.OPTIONS);
    }

    /**
     * Проверяет, что GET-запрос с payload (если пользователь его установил) без Content-Type бросает исключение.
     * <p>
     * GET формально не поддерживает payload, но если пользователь его установил, валидация всё равно сработает.
     * </p>
     */
    @Test
    @DisplayName("buildGetWithPayloadWithoutContentTypeThrows")
    public void buildGetWithPayloadWithoutContentTypeThrows() {
        var builder = Request.builder()
                .setMethod(HttpMethod.GET)
                .setUrl("/api/test")
                .setPayload("some body");

        assertThatThrownBy(builder::build)
                .describedAs("Ожидалась HttpClientInvalidRequestException при GET + payload без Content-Type")
                .isInstanceOf(HttpClientInvalidRequestException.class)
                .hasMessage(REQUEST_ERROR_INVALID_CONTENT_TYPE);
    }
}
