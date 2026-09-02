package ru.vych.logger.impl;

import org.springframework.stereotype.Service;
import ru.vych.logger.impl.appenders.LogAppender;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.exceptions.LoggerException;

import java.util.List;
import java.util.UUID;

/**
 * Сервис логирования
 */
@Service
public class LogService {
    public final static String SERVICE_CODE = "LoggerService";
    private final String uuid = UUID.randomUUID().toString();

    private final List<LogAppender> appenders;
    private final List<LogFilter> logFilters;

    public LogService(List<LogAppender> appenders, List<LogFilter> logFilters) {
        this.appenders = appenders;
        this.logFilters = logFilters;
        info(
                SERVICE_CODE, uuid,
                "Инициализирован сервис логирования",
                appenders.stream()
                        .map(Object::getClass)
                        .map(Class::getSimpleName)
                        .toList()
        );
    }

    public void debug(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.DEBUG, message, entities);
    }

    public void debug(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.DEBUG, "", entities);
    }

    public void info(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.INFO, message, entities);
    }

    public void info(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.INFO, "", entities);
    }

    public void warn(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.WARN, message, entities);
    }

    public void warn(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.WARN, "", entities);
    }

    public void error(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.ERROR, message, entities);
    }

    public void error(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.ERROR, "", entities);
    }

    public void log(String serviceCode, String uuid, LoggingLevel loggingLevel, String message, Object... entities) {
        appenders.forEach(
                appender -> {
                    var event = LogEvent.create(
                            serviceCode, uuid, loggingLevel,
                            message, entities
                    );

                    for (var filter : logFilters) {
                        if (!filter.filter(event)) {
                            return;
                        }
                    }

                    try {
                        appender.append(event);
                    } catch (LoggerException e) {
                        error(
                                SERVICE_CODE, uuid, "Не удалось отправить лог.",
                                appender.getServiceCode(), event.toString()
                        );
                    }
                }
        );
    }
}
