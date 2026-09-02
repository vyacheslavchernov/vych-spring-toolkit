package ru.vych.logger.impl.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperUtils {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String toJson(Object target) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(target);
    }

    public static String toPrettyJson(Object target) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(target);
    }
}
