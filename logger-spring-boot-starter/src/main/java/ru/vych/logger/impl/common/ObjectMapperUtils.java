package ru.vych.logger.impl.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Утилитарный класс для сериализации объектов в JSON с помощью Jackson.
 *
 * <p>Использует настроенный {@code ObjectMapper} с поддержкой Java 8 дат
 * и выводом дат в ISO-формате (не timestamp).
 *
 * @see ru.vych.logger.impl.entities.LogEvent
 */
public final class ObjectMapperUtils {
    /**
     * Общий экземпляр ObjectMapper с предустановленными настройками.
     */
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Сериализует объект в JSON-строку.
     *
     * @param target объект для сериализации
     * @return JSON-строка
     * @throws JsonProcessingException в случае ошибки сериализации
     */
    public static String toJson(Object target) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(target);
    }

    /**
     * Сериализует объект в отформатированную (pretty-print) JSON-строку.
     *
     * @param target объект для сериализации
     * @return отформатированная JSON-строка
     * @throws JsonProcessingException в случае ошибки сериализации
     */
    public static String toPrettyJson(Object target) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(target);
    }
}
