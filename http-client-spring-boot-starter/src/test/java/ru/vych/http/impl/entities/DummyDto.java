package ru.vych.http.impl.entities;

import lombok.*;

/**
 * Моковый DTO-объект для тестирования маппинга тела ответа через Jackson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DummyDto {
    private String field;
}