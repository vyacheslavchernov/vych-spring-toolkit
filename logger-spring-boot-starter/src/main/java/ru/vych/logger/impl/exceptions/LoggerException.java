package ru.vych.logger.impl.exceptions;

/**
 * Корневое исключение лоррега.
 * Любые другие исключения всегда оборачиваются в него.
 */
public class LoggerException extends Exception {
    public LoggerException(String message) {
        super(message);
    }
  public LoggerException(String message, Throwable e) {
    super(message, e);
  }
}
