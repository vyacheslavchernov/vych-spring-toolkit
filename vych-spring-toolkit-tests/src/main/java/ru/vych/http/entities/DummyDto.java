package ru.vych.http.entities;

import lombok.*;
import ru.vych.common.RandomUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO для тестовых данных.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class DummyDto {

    /** Тестовое значение. */
    private String value;
    /** Тестовый список значений. */
    private List<String> listValue;
    /** Тестовая карта значений. */
    private Map<String, String> mapValue;

    /**
     * Создает тестовый DummyDto с случайными данными.
     * @return новый DummyDto
     */
    public static DummyDto getDummy() {
        return new DummyDto(
                UUID.randomUUID().toString(),
                RandomUtils.randomList(15),
                RandomUtils.randomMap(15)
        );
    }
}
