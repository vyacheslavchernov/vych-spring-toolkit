package ru.vych.logger.impl.exceptions;

/**
 * Исключение выбрасываемое при ошибке во время работы {@link ru.vych.logger.impl.appenders.LogAppender}
 */
public class LoggerAppenderException extends LoggerException {
    public LoggerAppenderException(String message) {
        super(message);
    }
  public LoggerAppenderException(String message, Throwable e) {
    super(message, e);
  }
}
