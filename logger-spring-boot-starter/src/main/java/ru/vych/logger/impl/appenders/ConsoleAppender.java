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

/**
 * Аппендер для записи лог-сообщений в консоль (System.out / System.err).
 *
 * <p>Поддерживает:
 * <ul>
 *   <li>Фильтрацию по уровню логирования</li>
 *   <li>ANSI-цветирование вывода</li>
 *   <li>Сериализацию дополнительных объектов в JSON</li>
 *   <li>Вывод ошибок в stderr, остальных сообщений — в stdout</li>
 * </ul>
 *
 * @see LogAppender
 * @see LoggingLevel
 * @see AnsiColor
 */
@RequiredArgsConstructor
public class ConsoleAppender implements LogAppender {
    /**
     * Код этого аппендера, используется при логировании внутренних сообщений.
     */
    private final static String SERVICE_CODE = "ConsoleAppender";

    /**
     * Минимальный уровень логирования для вывода.
     */
    private final LoggingLevel loggingLevel;

    /**
     * Включать ли дополнительные объекты в вывод.
     */
    private final boolean includeEntities;

    /**
     * Форматировать ли JSON объектов с отступами.
     */
    private final boolean prettyEntities;

    /**
     * Использовать ли ANSI-цвета.
     */
    private final boolean enableColors;

    /**
     * Делать ли вывод объектов менее ярким.
     */
    private final boolean dimEntities;

    /**
     * Добавляет событие логирования в консоль.
     *
     * <p>Если уровень события ниже настроенного минимального — событие пропускается.
     * Сообщения уровня ERROR выводятся в {@code System.err}, остальные — в {@code System.out}.
     *
     * @param event событие логирования
     * @throws LoggerAppenderException в случае ошибки сериализации объектов
     */
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

    /**
     * Возвращает код этого аппендера.
     *
     * @return {@code "ConsoleAppender"}
     */
    @Override
    public String getServiceCode() {
        return SERVICE_CODE;
    }

    /**
     * Возвращает ANSI-код цвета для указанного уровня логирования.
     *
     * @param level уровень логирования
     * @return ANSI-код цвета
     */
    private String getColorByLevel(LoggingLevel level) {
        return switch (level) {
            case DEBUG -> AnsiColor.WHITE;
            case INFO -> AnsiColor.BLUE;
            case WARN -> AnsiColor.YELLOW;
            case ERROR -> AnsiColor.RED;
        };
    }

    /**
     * Сериализует дополнительные объекты в строковое представление.
     *
     * @param entities список объектов для сериализации
     * @return строка с JSON-представлением объектов (префикс ": " + JSON)
     * @throws LoggerAppenderException в случае ошибки сериализации
     */
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
