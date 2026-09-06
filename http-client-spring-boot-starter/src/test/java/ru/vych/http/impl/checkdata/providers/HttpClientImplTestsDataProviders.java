package ru.vych.http.impl.checkdata.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.provider.Arguments;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.checkdata.HttpClientImplBuildResponseCheckData;
import ru.vych.http.impl.entities.DummyDto;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.TestRequestInterceptor;
import ru.vych.http.impl.entities.TestResponseInterceptor;
import ru.vych.http.impl.exceptions.HttpClientConfigurationException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.*;

/**
 * Поставщики тестовых данных для параметризованных тестов класса {@link ru.vych.http.impl.HttpClientImpl}.
 * Содержит методы-провайдеры для проверки конструктора, разбора тела и заголовков ответа.
 */
public class HttpClientImplTestsDataProviders {
    /**
     * Код сервиса, используемый в тестовых конфигурациях.
     */
    public final static String SERVICE_CODE = "test-client";
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Поставщик аргументов для параметризованного теста конструктора:
     * null-аргументы, пустые списки и списки с интерцепторами.
     */
    public static Stream<Arguments> interceptorArgsProvider() {
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
    public static Stream<Arguments> invalidConfigArgsProvider() {
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
                        new HttpClientConfig(SERVICE_CODE).setCookiePolicy(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIE_POLICY_CANT_BE_NULL
                ),
                Arguments.of(
                        new HttpClientConfig(SERVICE_CODE).setCookies(null),
                        HttpClientConfigurationException.class,
                        CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIES_CANT_BE_NULL
                )
        );
    }

    /**
     * Поставщик аргументов для параметризованного теста {@code buildResponseBody}:
     * тела ответа в виде {@code String}, кастомного DTO (маппинг через Jackson)
     * и {@code byte[]}.
     */
    public static Stream<HttpClientImplBuildResponseCheckData> buildResponseArgsProvider() throws JsonProcessingException {
        var string = "Hello, world!";
        var dummyDto = new DummyDto("test");

        return Stream.of(
                new HttpClientImplBuildResponseCheckData()
                        .setResponseClass(String.class)
                        .setExpectedBody("Hello, world!")
                        .setExpectedRawBody("Hello, world!")
                        .setExpectedBodyType(String.class)
                        .setExpectedRawBodyType(String.class)
                        .setResponseByte(string.getBytes(StandardCharsets.UTF_8)),
                new HttpClientImplBuildResponseCheckData()
                        .setResponseClass(DummyDto.class)
                        .setExpectedBody(dummyDto)
                        .setExpectedRawBody(OBJECT_MAPPER.writeValueAsString(dummyDto))
                        .setExpectedBodyType(DummyDto.class)
                        .setExpectedRawBodyType(String.class)
                        .setResponseByte(OBJECT_MAPPER.writeValueAsBytes(dummyDto)),
                new HttpClientImplBuildResponseCheckData()
                        .setResponseClass(byte[].class)
                        .setExpectedBody(null)
                        .setExpectedRawBody(null)
                        .setExpectedBodyType(null)
                        .setExpectedRawBodyType(null)
                        .setResponseByte(string.getBytes(StandardCharsets.UTF_8))
        );
    }

    /**
     * Поставщик аргументов для параметризованного теста {@code buildResponseHeaders}:
     * карта заголовков с несколькими значениями и пустая карта.
     */
    public static Stream<Arguments> buildResponseHeadersArgsProvider() {
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
}
