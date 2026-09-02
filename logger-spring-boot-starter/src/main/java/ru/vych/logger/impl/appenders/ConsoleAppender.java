package ru.vych.logger.impl.appenders;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import ru.vych.logger.impl.common.AnsiColor;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.common.ObjectMapperUtils;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.io.PrintStream;
import java.util.List;

@RequiredArgsConstructor
public class ConsoleAppender implements LogAppender {
    private final static String SERVICE_CODE = "ConsoleAppender";

    private final LoggingLevel loggingLevel;
    private final boolean includeEntities;
    private final boolean prettyEntities;
    private final boolean enableColors;
    private final boolean dimEntities;

    @Override
    public void append(LogEvent event) throws LoggerAppenderException {
        if (event.getLoggingLevel().getValue() < loggingLevel.getValue()) {
            return;
        }

        PrintStream stream = (event.getLoggingLevel() == LoggingLevel.ERROR ? System.err : System.out);
        stream.printf(
                "%-30s %-5s %-30s : %s %s\n",
                event.getTimestamp(),
                enableColors
                        ? getColorByLevel(event.getLoggingLevel()) + event.getLoggingLevel().name() + AnsiColor.RESET
                        : event.getLoggingLevel().name(),
                "[" + event.getServiceCode() + "]",
                event.getMessage(),
                writeEntities(event.getEntities())
        );
    }

    @Override
    public String getServiceCode() {
        return SERVICE_CODE;
    }

    private String getColorByLevel(LoggingLevel level) {
        return switch (level) {
            case DEBUG -> AnsiColor.WHITE;
            case INFO -> AnsiColor.BLUE;
            case WARN -> AnsiColor.YELLOW;
            case ERROR -> AnsiColor.RED;
        };
    }

    private String writeEntities(List<Object> entities) throws LoggerAppenderException {
        var entitiesString = "";
        if (includeEntities) {
            try {
                entitiesString = prettyEntities
                        ? ": " + ObjectMapperUtils.toPrettyJson(entities)
                        : ": " + ObjectMapperUtils.toJson(entities);
            } catch (JsonProcessingException e) {
                throw new LoggerAppenderException("Не получилось сериализовать LogEvent entities", e);
            }
        }

        if (includeEntities && dimEntities) {
            entitiesString = AnsiColor.WHITE + entitiesString + AnsiColor.RESET;
        }
        return entitiesString;
    }
}
