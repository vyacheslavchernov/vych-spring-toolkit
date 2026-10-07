package ru.vych.http.impl.checkdata;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Данные для тестирования метода {@code buildResponse} класса {@link ru.vych.http.impl.HttpClientImpl}.
 * Хранит информацию о классе ответа, ожидаемом теле, raw-теле и байтах ответа.
 */
@Getter
@Setter
@Accessors(chain = true)
public class HttpClientImplBuildResponseCheckData {
    /**
     * Ожидаемый класс десериализованного тела ответа.
     */
    private Class<?> responseClass = null;

    /**
     * Ожидаемое десериализованное тело ответа.
     */
    private Object expectedBody = null;

    /**
     * Ожидаемое строковое представление тела ответа.
     */
    private Object expectedRawBody = null;

    /**
     * Ожидаемый тип десериализованного тела.
     */
    private Class<?> expectedBodyType = null;

    /**
     * Ожидаемый тип строкового представления тела.
     */
    private Class<?> expectedRawBodyType = null;

    /**
     * Исходные байты тела ответа.
     */
    private byte[] responseByte = null;
}
