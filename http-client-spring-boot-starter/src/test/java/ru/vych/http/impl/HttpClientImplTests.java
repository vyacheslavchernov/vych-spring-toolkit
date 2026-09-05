package ru.vych.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientConfigurationException;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.exceptions.HttpClientHandleResponseException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpClientImpl")
class HttpClientImplTests {
    private final static String SERVICE_CODE = "test-client";

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private LogService logService;

    private HttpClientConfig config;

    /**
     * Поставщик аргументов для параметризованного теста конструктора:
     * null-аргументы, пустые списки и списки с интерцепторами.
     */
    private static Stream<Arguments> interceptorArgsProvider() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(new ArrayList<RequestInterceptor>(), new ArrayList<ResponseInterceptor>()),
                Arguments.of(
                        List.of(new TestRequestInterceptor(), new TestRequestInterceptor()),
                        List.of(new TestResponseInterceptor(), new TestResponseInterceptor())
                )
        );
    }

    /**
     * Поставщик аргументов для параметризованного теста невалидной конфигурации:
     * {@code null} конфигурация, а также конфигурации с {@code null} или отрицательными
     * значениями полей {@code root}, {@code timeout}, {@code version}, {@code cookieHandlerClass}.
     */
    private static Stream<Arguments> invalidConfigArgsProvider() {
        return Stream.of(
                Arguments.of(
                        null,
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_NULL
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setRoot(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT_ROOT_CANT_BE_NULL
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setTimeout(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setTimeout(Duration.ZERO),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setVersion(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setStoreCookies(true).setCookieHandlerClass(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIE_HANDLER_CANT_BE_NULL
                )
        );
    }

    /**
     * Поставщик аргументов для параметризованного теста {@code buildResponseBody}:
     * тела ответа в виде {@code String}, кастомного DTO (маппинг через Jackson)
     * и {@code byte[]}.
     */
    private static Stream<Arguments> buildResponseArgsProvider() throws JsonProcessingException {
        return Stream.of(
                Arguments.of(
                        String.class,
                        "Hello, world!",
                        "Hello, world!",
                        String.class,
                        String.class,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8)
                ),
                Arguments.of(
                        DummyDto.class,
                        new DummyDto("test"),
                        OBJECT_MAPPER.writeValueAsString(new DummyDto("test")),
                        DummyDto.class,
                        String.class,
                        OBJECT_MAPPER.writeValueAsBytes(new DummyDto("test"))
                ),
                Arguments.of(
                        byte[].class,
                        null,
                        null,
                        null,
                        null,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8)
                )
        );
    }

    /**
     * Поставщик аргументов для параметризованного теста {@code buildResponseHeaders}:
     * карта заголовков с несколькими значениями и пустая карта.
     */
    private static Stream<Arguments> buildResponseHeadersArgsProvider() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "header1", List.of("value1", "value2"),
                                "header2", List.of("value1")
                        ),
                        List.of(
                                new Header("header1", "value1"),
                                new Header("header1", "value2"),
                                new Header("header2", "value1")
                        )
                ),
                Arguments.of(
                        new HashMap<String, List<String>>(),
                        new ArrayList<Header>()
                )
        );
    }

    /**
     * Создаёт конфигурацию с базовым URL, тайм-аутом и включённым логированием запросов.
     */
    @BeforeEach
    void setUp() {
        config = new HttpClientConfig(SERVICE_CODE)
                .setRoot("http://localhost:8080")
                .setLogRequests(true);
    }

    /**
     * Проверяет, что конструктор корректно инициализирует все поля
     * при передаче null, пустых списков или списков с интерцепторами.
     */
    @ParameterizedTest
    @MethodSource("interceptorArgsProvider")
    @DisplayName("Проверка конструктора с валидными аргументами")
    public void constructorValidArgs(
            List<RequestInterceptor> requestInterceptors,
            List<ResponseInterceptor> responseInterceptors
    ) throws HttpClientException {
        var httpClient = new HttpClientImpl(config, logService, requestInterceptors, responseInterceptors);
        validateHttpClient(httpClient, requestInterceptors, responseInterceptors);
    }

    /**
     * Проверяет, что конструктор выбрасывает {@code HttpClientConfigurationException}
     * с корректным сообщением при невалидных значениях конфигурации:
     * {@code null} конфигурация, а также {@code null} или отрицательные значения
     * полей {@code root}, {@code timeout}, {@code version}, {@code cookieHandlerClass}.
     */
    @ParameterizedTest
    @MethodSource("invalidConfigArgsProvider")
    @DisplayName("Проверка конструктора с неправильными значениями конфигурации")
    public void constructorInvalidConfig(
            HttpClientConfig invalidConfig,
            Class<Exception> expectedException,
            String expectedMessage
    ) {
        assertThrows(expectedException, () ->
                        new HttpClientImpl(invalidConfig, logService, null, null),
                expectedMessage
        );
    }

    /**
     * Проверяет, что конструктор выбрасывает {@code HttpClientException}
     * с сообщением {@code CREATION_ERROR_LOG_SERVICE_IS_NULL}
     * при передаче {@code null} в качестве логгера.
     */
    @Test
    @DisplayName("Проверка конструктора с logService null")
    public void constructorLogServiceNull() {
        assertThrows(HttpClientConfigurationException.class, () ->
                        new HttpClientImpl(config, null, null, null),
                CREATION_ERROR_LOG_SERVICE_IS_NULL
        );
    }

    /**
     * Проверяет корректность разбора тела ответа и маппинга в нужный тип:
     * {@code String}, {@code byte[]} и кастомный DTO через Jackson.
     * Также проверяет, что {@code Response} корректно сохраняет raw-байты,
     * разобранное тело, UUID и исходный запрос.
     */
    @ParameterizedTest
    @MethodSource("buildResponseArgsProvider")
    @DisplayName("Проверка корректного возврата тела ответа для разных типов")
    public void buildResponseBody(
            Class<?> responseClass,
            Object expectedBody,
            Object expectedRawBody,
            Class<?> expectedBodyType,
            Class<?> expectedRawBodyType,
            byte[] responseBytes
    ) throws HttpClientException {
        var request = Request.builder()
                .setUrl("")
                .setMethod(HttpMethod.GET)
                .setResponseClass(responseClass)
                .build();

        var dummyResponse = new DummyResponse()
                .setBody(responseBytes);

        var response = getValidClient().buildResponse(dummyResponse, request);

        assertThat(response)
                .satisfies(
                        rs -> assertThat(rs.getRawBytes())
                                .describedAs("Тело ответа не соответствует ожидаемому")
                                .isNotNull()
                                .isNotEmpty()
                                .containsExactly(responseBytes),

                        rs -> {
                            if (expectedBody == null) {
                                assertThat(rs.getBody())
                                        .describedAs("Тело ответа должно быть null")
                                        .isNull();
                            } else {
                                assertThat(rs.getBody())
                                        .describedAs("Тело ответа не соответствует ожидаемому")
                                        .isNotNull()
                                        .isInstanceOf(expectedBodyType)
                                        .isEqualTo(expectedBody);
                            }
                        },

                        rs -> {
                            if (expectedRawBody == null) {
                                assertThat(rs.getRawBody())
                                        .describedAs("RawBody должно быть null")
                                        .isNull();
                            } else {
                                assertThat(rs.getRawBody())
                                        .describedAs("RawBody не соответствует ожидаемому")
                                        .isNotNull()
                                        .isNotEmpty()
                                        .isInstanceOf(expectedRawBodyType)
                                        .isEqualTo(expectedRawBody);
                            }
                        },

                        rs -> assertThat(rs.getUuid())
                                .describedAs("UUID не соответствует ожидаемому")
                                .isNotNull()
                                .isNotEmpty()
                                .isEqualTo(request.getUuid()),

                        rs -> assertThat(rs.getRequest())
                                .describedAs("Запрос не соответствует ожидаемому")
                                .isEqualTo(request)
                );
    }

    /**
     * Проверяет, что при некорректном JSON в теле ответа конструктор выбрасывает
     * {@code HttpClientHandleResponseException} с сообщением {@code RESPONSE_ERROR_CANT_DESERIALIZE_BODY}.
     */
    @Test
    @DisplayName("Проверка обработки некорректного JSON в теле ответа")
    public void buildResponseInvalidJson() throws HttpClientException {
        var request = Request.builder()
                .setUrl("")
                .setMethod(HttpMethod.GET)
                .setResponseClass(DummyDto.class)
                .build();

        var dummyResponse = new DummyResponse()
                .setBody("{'invalidJson':'invalidJson'}".getBytes(StandardCharsets.UTF_8));

        assertThrows(
                HttpClientHandleResponseException.class,
                () -> getValidClient().buildResponse(dummyResponse, request),
                RESPONSE_ERROR_CANT_DESERIALIZE_BODY
        );
    }

    /**
     * Проверяет корректность маппинга HTTP-заголовков ответа из {@code HttpHeaders}
     * в список {@code Header}.
     */
    @ParameterizedTest
    @MethodSource("buildResponseHeadersArgsProvider")
    @DisplayName("Проверка корректного маппинга заголовков ответа")
    public void buildResponseHeaders(Map<String, List<String>> headers, List<Header> expectedHeaders) throws HttpClientException {
        var client = getValidClient();
        var request = Request.builder()
                .setUrl("")
                .setMethod(HttpMethod.GET)
                .setResponseClass(String.class)
                .build();

        var dummyResponse = new DummyResponse()
                .setHeaders(HttpHeaders.of(headers, (s1, s2) -> true));

        var response = client.buildResponse(dummyResponse, request);

        assertThat(response.getHeaders())
                .describedAs("Заголовки не соответствуют ожидаемым")
                .containsExactlyElementsOf(expectedHeaders);
    }

    /**
     * Создаёт валидный экземпляр {@code HttpClientImpl}.
     */
    private HttpClientImpl getValidClient() throws HttpClientException {
        return new HttpClientImpl(config, logService, null, null);
    }

    /**
     * Валидирует, что все поля {@code HttpClientImpl} корректно инициализированы:
     * config, UUID, logService, списки request/response интерцепторов, внутренний http-клиент.
     */
    private void validateHttpClient(
            HttpClientImpl httpClient,
            List<RequestInterceptor> requestInterceptors,
            List<ResponseInterceptor> responseInterceptors
    ) {
        var requestInterceptorsList = requestInterceptors == null
                ? new ArrayList<RequestInterceptor>()
                : new ArrayList<>(requestInterceptors);

        var responseInterceptorsList = responseInterceptors == null
                ? new ArrayList<ResponseInterceptor>()
                : new ArrayList<>(responseInterceptors);

        assertThat(httpClient)
                .describedAs("Клиент не прошёл валидацию")
                .isNotNull()
                .satisfies(
                        client -> assertThat(client)
                                .describedAs("Конфигурация не совпадает с переданной")
                                .extracting("config")
                                .isEqualTo(config),

                        client -> assertThat(client.getClientUuid())
                                .describedAs("UUID не должен быть null или пустой строкой")
                                .isNotNull()
                                .isNotEmpty(),

                        client -> assertThat(client)
                                .describedAs("Логгер не совпадает с переданным")
                                .extracting("httpClientLogger")
                                .isNotNull()
                                .extracting("logService")
                                .isEqualTo(logService),

                        client -> assertThat(client)
                                .describedAs("Список request interceptors не совпадает с переданным")
                                .extracting("requestInterceptors")
                                .asInstanceOf(LIST)
                                .containsAll(requestInterceptorsList),

                        client -> assertThat(client)
                                .describedAs("Список response interceptors не совпадает с переданным")
                                .extracting("responseInterceptors")
                                .asInstanceOf(LIST)
                                .containsAll(responseInterceptorsList),

                        client -> assertThat(client)
                                .describedAs("Внутренний HTTP-клиент не должен быть null")
                                .extracting("client")
                                .isNotNull()
                                .isInstanceOf(HttpClient.class)
                                .asInstanceOf(new InstanceOfAssertFactory<>(HttpClient.class, Assertions::assertThat))
                                .satisfies(
                                        innerClient -> assertThat(innerClient.version())
                                                .describedAs("Версия HTTP-клиента не совпадает с переданной")
                                                .isEqualTo(config.getVersion()),

                                        innerClient -> assertThat(innerClient.followRedirects())
                                                .describedAs("Перенаправления не совпадают с переданными")
                                                .isEqualTo(config.getRedirectPolicy()),

                                        innerClient -> assertThat(innerClient.connectTimeout().orElse(null))
                                                .describedAs("Таймаут соединения не совпадает с переданным")
                                                .isNotNull()
                                                .isEqualTo(config.getTimeout())
                                )
                );
    }

    /**
     * Моковый {@link HttpResponse}, используемый для тестирования
     * методов {@code HttpClientImpl} с заданными статус-кодом, заголовками и телом.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private static class DummyResponse implements HttpResponse {
        private int statusCode = 200;
        private HttpRequest request = null;
        private Optional<HttpResponse> previousResponse = Optional.empty();
        private HttpHeaders headers = HttpHeaders.of(new HashMap<>(), (a, b) -> true);
        private Object body = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        private Optional<SSLSession> sslSession = Optional.empty();
        private URI uri = null;
        private HttpClient.Version version = HttpClient.Version.HTTP_1_1;

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse> previousResponse() {
            return previousResponse;
        }

        @Override
        public HttpHeaders headers() {
            return headers;
        }

        @Override
        public Object body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return sslSession;
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public HttpClient.Version version() {
            return version;
        }
    }

    /**
     * Моковый DTO-объект для тестирования маппинга тела ответа через Jackson.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class DummyDto {
        private String field;
    }

    /**
     * Тестовый {@link RequestInterceptor} с пустой реализацией.
     */
    private static class TestRequestInterceptor implements RequestInterceptor {
        @Override
        public void handle(ru.vych.http.impl.HttpClient client, Request request) {

        }
    }

    /**
     * Тестовый {@link ResponseInterceptor} с пустой реализацией.
     */
    private static class TestResponseInterceptor implements ResponseInterceptor {
        @Override
        public void handle(ru.vych.http.impl.HttpClient client, Response response) {

        }
    }
}
