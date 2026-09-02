package ru.vych.logger.impl.common;

import lombok.Getter;

@Getter
public enum LoggingLevel {
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4);

    private final int value;

    LoggingLevel(int value) {
        this.value = value;
    }
}
