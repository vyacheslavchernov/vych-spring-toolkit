package ru.vych.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.vych.http.impl.checkdata.ResponseBodyCastingCheckData;
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
@DisplayName("Тесты для класса Response")
public class ResponseTests {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Поставщик аргументов для параметризованного теста {@link #bodyCastingTest(ResponseBodyCastingCheckData)}.
     * <p>
     * Возвращает Stream с тестовыми данными для проверки {@link Response#getCastedBody()}:
     * <ul>
     *   <li>{@code String.class} — десериализация строки;</li>
     *   <li>{@code DummyDto.class} — десериализация DTO через Jackson;</li>
     *   <li>{@code List.class} — десериализация списка;</li>
     *   <li>{@code Map.class} — десериализация мапы;</li>
     *   <li>{@code null} — отсутствие класса ответа;</li>
     *   <li>{@code byte.class} — примитивный тип (не поддерживается, возвращает null);</li>
     *   <li>{@code byte[].class} — массив байт (не поддерживается, возвращает null).</li>
     * </ul>
     * </p>
     *
     * @return Stream с тестовыми данными {@link ResponseBodyCastingCheckData}
     * @throws JsonProcessingException если ошибка сериализации тестовых данных
     */
    private static Stream<ResponseBodyCastingCheckData> bodyCastingArgsProvider() throws JsonProcessingException {
        var string = "Hello, world!";
        var dummyDto = new DummyDto("Hello, world!");
        var list = List.of("value1", "value2");
        var map = Map.of("key1", "value1", "key2", "value2");

        return Stream.of(
                new ResponseBodyCastingCheckData()
                        .setResponseClass(String.class)
                        .setBodyBytes(string.getBytes(StandardCharsets.UTF_8))
                        .setBody(string)
                        .setRawBody(string),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(DummyDto.class)
                        .setBodyBytes(OBJECT_MAPPER.writeValueAsBytes(dummyDto))
                        .setBody(dummyDto)
                        .setRawBody(OBJECT_MAPPER.writeValueAsString(dummyDto)),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(List.class)
                        .setBodyBytes(OBJECT_MAPPER.writeValueAsBytes(list))
                        .setBody(list)
                        .setRawBody(OBJECT_MAPPER.writeValueAsString(list)),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(Map.class)
                        .setBodyBytes(OBJECT_MAPPER.writeValueAsBytes(map))
                        .setBody(map)
                        .setRawBody(OBJECT_MAPPER.writeValueAsString(map)),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(null)
                        .setBodyBytes(string.getBytes(StandardCharsets.UTF_8))
                        .setBody(null)
                        .setRawBody(null),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(byte.class)
                        .setBodyBytes(string.getBytes(StandardCharsets.UTF_8))
                        .setBody(null)
                        .setRawBody(null),
                new ResponseBodyCastingCheckData()
                        .setResponseClass(byte[].class)
                        .setBodyBytes(string.getBytes(StandardCharsets.UTF_8))
                        .setBody(null)
                        .setRawBody(null)
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
    public void bodyCastingTest(ResponseBodyCastingCheckData checkData) throws HttpClientInvalidRequestException {
        var response = new Response(
                "uuid",
                Request.builder()
                        .setMethod(HttpMethod.GET)
                        .setResponseClass(checkData.getResponseClass())
                        .build(),
                200,
                checkData.getBodyBytes(),
                checkData.getRawBody(),
                checkData.getBody(),
                new ArrayList<>()
        );

        Assertions.assertThatCode(response::getCastedBody).doesNotThrowAnyException();
        var castedBody = response.getCastedBody();

        if (checkData.getResponseClass() == null
                || checkData.getResponseClass() == byte.class
                || checkData.getResponseClass() == byte[].class
        ) {
            assertThat(castedBody)
                    .describedAs("Проверка каста тела ответа")
                    .isNull();
        } else {
            assertThat(castedBody)
                    .describedAs("Проверка каста тела ответа")
                    .isNotNull()
                    .isInstanceOf(checkData.getResponseClass())
                    .isEqualTo(checkData.getBody());
        }
    }
}
