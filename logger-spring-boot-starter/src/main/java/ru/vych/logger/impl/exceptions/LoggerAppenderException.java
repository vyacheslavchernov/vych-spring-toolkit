package ru.vych.logger.impl.exceptions;

import ru.vych.logger.impl.entities.LogEvent;

/**
 * Исключение, выбрасываемое при ошибке во время работы {@link ru.vych.logger.impl.appenders.LogAppender}.
 *
 * <p>Является подклассом {@link LoggerException} и используется для специализации
 * ошибок, связанных именно с аппендерами.
 *
 * @see LoggerException
 * @see ru.vych.logger.impl.appenders.LogAppender#append(LogEvent) 
 */
public class LoggerAppenderException extends LoggerException {
    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public LoggerAppenderException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанным сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина (исключение-корень)
     */
    public LoggerAppenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
