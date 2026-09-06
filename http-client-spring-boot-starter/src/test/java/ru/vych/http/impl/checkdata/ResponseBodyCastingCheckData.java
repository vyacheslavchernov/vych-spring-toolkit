package ru.vych.http.impl.checkdata;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Данные для тестирования маппинга тела ответа HTTP-клиента.
 * Хранит исходный класс-тип, байты тела, десериализованное тело и строковое представление.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResponseBodyCastingCheckData {
    /**
     * Ожидаемый тип десериализованного тела ответа.
     */
    private Class<?> responseClass = null;

    /**
     * Сырые байты тела ответа.
     */
    private byte[] bodyBytes = null;

    /**
     * Ожидаемое десериализованное тело ответа.
     */
    private Object body = null;

    /**
     * Ожидаемое строковое представление тела ответа.
     */
    private String rawBody = null;
}
