package ru.vych.http.entities;

import lombok.*;
import ru.vych.common.RandomUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class DummyDto {
    private String value;
    private List<String> listValue;
    private Map<String, String> mapValue;

    public static DummyDto getDummy() {
        return new DummyDto(
                UUID.randomUUID().toString(),
                RandomUtils.randomList(15),
                RandomUtils.randomMap(15)
        );
    }
}
