package ru.vych.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.DummyDto;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientInvalidRequestException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для класса {@link Response}, включая проверку кастомизации тела ответа
 * через {@link Response#getCastedBody()}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для класса Response")
public class ResponseTests {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Поставщик аргументов для параметризованного теста {@link #bodyCastingTest(Class, byte[], Object, String)}.
     * Содержит комбинации {@code responseClass}, байтов тела, десериализованного объекта и raw-строки
     * для различных сценариев: DTO, List, Map, String, null, byte.class, byte[].class.
     */
    private static Stream<Arguments> bodyCastingArgsProvider() throws JsonProcessingException {
        var dummyDto = new DummyDto("Hello, world!");
        var list = List.of("value1", "value2");
        var map = Map.of("key1", "value1", "key2", "value2");

        return Stream.of(
                Arguments.of(
                        String.class,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8),
                        "Hello, world!",
                        "Hello, world!"
                ),
                Arguments.of(
                        DummyDto.class,
                        OBJECT_MAPPER.writeValueAsBytes(dummyDto),
                        dummyDto,
                        OBJECT_MAPPER.writeValueAsString(dummyDto)
                ),
                Arguments.of(
                        List.class,
                        OBJECT_MAPPER.writeValueAsBytes(list),
                        list,
                        OBJECT_MAPPER.writeValueAsString(list)
                ),
                Arguments.of(
                        Map.class,
                        OBJECT_MAPPER.writeValueAsBytes(map),
                        map,
                        OBJECT_MAPPER.writeValueAsString(map)
                ),
                Arguments.of(
                        null,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8),
                        null,
                        "Hello, world!"
                ),
                Arguments.of(
                        byte.class,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8),
                        null,
                        null
                ),
                Arguments.of(
                        byte[].class,
                        "Hello, world!".getBytes(StandardCharsets.UTF_8),
                        null,
                        null
                )
        );
    }

    /**
     * Проверяет корректность работы {@link Response#getCastedBody()} для различных типов ответа.
     * <p>
     * Тестирует: DTO-объекты, List, Map, String, null, byte.class, byte[].class.
     * Для null, byte.class и byte[].class метод должен возвращать {@code null}.
     * Для остальных типов — корректно приводить тело к указанному классу.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("bodyCastingArgsProvider")
    @DisplayName("Тест каста тела ответа")
    public void bodyCastingTest(
            Class<?> responseClass,
            byte[] bodyBytes,
            Object body,
            String rawBody
    ) throws HttpClientInvalidRequestException {
        var response = new Response(
                "uuid",
                Request.builder()
                        .setMethod(HttpMethod.GET)
                        .setResponseClass(responseClass)
                        .build(),
                200,
                bodyBytes,
                rawBody,
                body,
                new ArrayList<>()
        );

        Assertions.assertThatCode(response::getCastedBody).doesNotThrowAnyException();
        var castedBody = response.getCastedBody();

        if (responseClass == null || responseClass == byte.class || responseClass == byte[].class) {
            assertThat(castedBody)
                    .describedAs("Проверка каста тела ответа")
                    .isNull();
        } else {
            assertThat(castedBody)
                    .describedAs("Проверка каста тела ответа")
                    .isNotNull()
                    .isInstanceOf(responseClass)
                    .isEqualTo(body);
        }
    }
}
