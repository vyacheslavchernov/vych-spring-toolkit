package ru.vych.http.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.checkdata.HttpClientImplBuildResponseCheckData;
import ru.vych.http.impl.checkdata.HttpClientImplBuildUriCheckData;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.DummyDto;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders.SERVICE_CODE;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.CREATION_ERROR_LOG_SERVICE_IS_NULL;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.RESPONSE_ERROR_CANT_DESERIALIZE_BODY;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты класса HttpClientImpl")
class HttpClientImplTests {
    @Mock
    private LogService logService;

    private HttpClientConfig config;

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
    @MethodSource("ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders#interceptorArgsProvider")
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
    @MethodSource("ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders#invalidConfigArgsProvider")
    @DisplayName("Проверка конструктора с неправильными значениями конфигурации")
    public void constructorInvalidConfig(
            HttpClientConfig invalidConfig,
            Class<Exception> expectedException,
            String expectedMessage
    ) {
        assertThatThrownBy(() ->
                new HttpClientImpl(invalidConfig, logService, null, null))
                .describedAs("Ошибка не соответствует ожидаемой")
                .isInstanceOf(expectedException)
                .hasMessage(expectedMessage);
    }

    /**
     * Проверяет, что конструктор выбрасывает {@code HttpClientException}
     * с сообщением {@code CREATION_ERROR_LOG_SERVICE_IS_NULL}
     * при передаче {@code null} в качестве логгера.
     */
    @Test
    @DisplayName("Проверка конструктора с logService null")
    public void constructorLogServiceNull() {
        assertThatThrownBy(() ->
                new HttpClientImpl(config, null, null, null))
                .describedAs("Ошибка не соответствует ожидаемой")
                .isInstanceOf(HttpClientConfigurationException.class)
                .hasMessage(CREATION_ERROR_LOG_SERVICE_IS_NULL);
    }

    /**
     * Проверяет корректность разбора тела ответа и маппинга в нужный тип:
     * {@code String}, {@code byte[]} и кастомный DTO через Jackson.
     * Также проверяет, что {@code Response} корректно сохраняет raw-байты,
     * разобранное тело, UUID и исходный запрос.
     */
    @ParameterizedTest
    @MethodSource("ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders#buildResponseArgsProvider")
    @DisplayName("Проверка корректного возврата тела ответа для разных типов")
    public void buildResponseBody(HttpClientImplBuildResponseCheckData checkData) throws HttpClientException {
        var request = Request.builder()
                .setUrl("")
                .setMethod(HttpMethod.GET)
                .setResponseClass(checkData.getResponseClass())
                .build();

        var dummyResponse = new DummyResponse()
                .setBody(checkData.getResponseByte());

        var response = getValidClient().buildResponse(dummyResponse, request);

        assertThat(response)
                .satisfies(
                        rs -> assertThat(rs.getRawBytes())
                                .describedAs("Тело ответа не соответствует ожидаемому")
                                .isNotNull()
                                .isNotEmpty()
                                .containsExactly(checkData.getResponseByte()),

                        rs -> {
                            if (checkData.getExpectedBody() == null) {
                                assertThat(rs.getBody())
                                        .describedAs("Тело ответа должно быть null")
                                        .isNull();
                            } else {
                                assertThat(rs.getBody())
                                        .describedAs("Тело ответа не соответствует ожидаемому")
                                        .isNotNull()
                                        .isInstanceOf(checkData.getExpectedBodyType())
                                        .isEqualTo(checkData.getExpectedBody());
                            }
                        },

                        rs -> {
                            if (checkData.getExpectedRawBody() == null) {
                                assertThat(rs.getRawBody())
                                        .describedAs("RawBody должно быть null")
                                        .isNull();
                            } else {
                                assertThat(rs.getRawBody())
                                        .describedAs("RawBody не соответствует ожидаемому")
                                        .isNotNull()
                                        .isNotEmpty()
                                        .isInstanceOf(checkData.getExpectedRawBodyType())
                                        .isEqualTo(checkData.getExpectedRawBody());
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

        assertThatThrownBy(() -> getValidClient().buildResponse(dummyResponse, request))
                .describedAs("Ошибка не соответствует ожидаемой")
                .isInstanceOf(HttpClientHandleResponseException.class)
                .hasMessage(RESPONSE_ERROR_CANT_DESERIALIZE_BODY);
    }

    /**
     * Проверяет корректность маппинга HTTP-заголовков ответа из {@code HttpHeaders}
     * в список {@code Header}.
     */
    @ParameterizedTest
    @MethodSource("ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders#buildResponseHeadersArgsProvider")
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
     * Проверяет корректность построения URI из {@link Request}:
     * объединение корневого URL из конфигурации с путём, path-параметрами и query-параметрами.
     */
    @ParameterizedTest
    @MethodSource("ru.vych.http.impl.checkdata.providers.HttpClientImplTestsDataProviders#httpClientImplBuildUriArgsProvider")
    @DisplayName("Проверка корректного построения URI")
    public void buildUri(HttpClientImplBuildUriCheckData checkData) throws HttpClientException {
        var client = new HttpClientImpl(checkData.getConfig(), logService, null, null);

        assertThatCode(() -> client.buildUri(checkData.getRequest()))
                .describedAs("При построении URI произошла ошибка")
                .doesNotThrowAnyException();
        var actualUri = client.buildUri(checkData.getRequest());

        assertThat(actualUri)
                .describedAs("URI не соответствует ожидаемому")
                .isNotNull()
                .isEqualTo(checkData.getExpectedURI());
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
}
