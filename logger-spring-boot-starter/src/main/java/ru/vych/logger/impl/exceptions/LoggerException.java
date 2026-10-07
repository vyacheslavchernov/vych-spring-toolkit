package ru.vych.logger.impl.exceptions;

import ru.vych.logger.impl.entities.LogEvent;

/**
 * Корневое проверяемое исключение логгера.
 *
 * <p>Любые другие исключения в модуле логирования всегда оборачиваются в него.
 * Используется для унификации обработки ошибок в аппендерах и фильтрах.
 *
 * @see ru.vych.logger.impl.appenders.LogAppender#append(LogEvent) 
 */
public class LoggerException extends Exception {
    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public LoggerException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанным сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина (исключение-корень)
     */
    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
