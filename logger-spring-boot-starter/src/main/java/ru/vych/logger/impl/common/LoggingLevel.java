package ru.vych.logger.impl.common;

import lombok.Getter;

/**
 * Уровни логирования с числовыми значениями для сравнения.
 *
 * <p>Чем выше значение, тем «строже» уровень:
 * {@code DEBUG < INFO < WARN < ERROR}.
 * Используется для фильтрации сообщений в аппендерах.
 */
@Getter
public enum LoggingLevel {
    /**
     * Отладочные сообщения.
     */
    DEBUG(1),

    /**
     * Информационные сообщения.
     */
    INFO(2),

    /**
     * Предупреждения.
     */
    WARN(3),

    /**
     * Ошибки.
     */
    ERROR(4);

    /**
     * Числовое значение уровня для сравнения.
     */
    private final int value;

    LoggingLevel(int value) {
        this.value = value;
    }
}
