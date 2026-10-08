package ru.vych.logger.impl.appenders;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import ru.vych.logger.impl.common.AnsiColor;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.common.ObjectMapperUtils;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
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
 *   <li>Кастомное форматирование строки лога через плейсхолдеры (%date, %level, %serviceCode, %message, %entity)</li>
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
     * Формат по умолчанию для строки лога.
     */
    private static final String DEFAULT_FORMAT_PATTERN = "%date     %level     %serviceCode : %message %entity";

    /**
     * Форматер для timestamp в кастомном форматтере.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Минимальный уровень логирования для вывода.
     */
    private final LoggingLevel loggingLevel;

    /**
     * Включать ли дополнительные объекты в вывод.
     */
    private final boolean includeEntities;

    /**
     * Форматировать ли JSON объектов с отступами (pretty-print).
     */
    private final boolean prettyEntities;

    /**
     * Использовать ли ANSI-цвета.
     */
    private final boolean enableColors;

    /**
     * Делать ли вывод объектов менее ярким (ANSI dim).
     */
    private final boolean dimEntities;

    /**
     * Кастомный формат строки лога через плейсхолдеры.
     * Если {@code null}, используется формат по умолчанию.
     */
    private final String formatPattern;

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
        String logLine = formatLogLine(event);

        if (enableColors) {
            logLine = applyColorToLevel(logLine, event.getLoggingLevel());
        }

        stream.println(logLine);
    }

    /**
     * Применяет ANSI-цвет к первому вхождению уровня логирования в строке.
     *
     * @param line    отформатированная строка лога
     * @param level   уровень логирования
     * @return строка с ANSI-цветом вокруг значения уровня
     */
    private String applyColorToLevel(String line, LoggingLevel level) {
        String levelName = level.name();
        String color = getColorByLevel(level);
        int index = line.indexOf(levelName);
        if (index >= 0) {
            return line.substring(0, index)
                    + color + levelName + AnsiColor.RESET
                    + line.substring(index + levelName.length());
        }
        return line;
    }

    /**
     * Форматирует событие логирования в строку.
     *
     * <p>Если {@code formatPattern} задан — используется кастомный форматтер с подстановкой плейсхолдеров.
     * Если {@code formatPattern} не задан — используется формат по умолчанию.
     *
     * @param event событие логирования
     * @return отформатированная строка лога
     * @throws LoggerAppenderException в случае ошибки сериализации объектов
     */
    private String formatLogLine(LogEvent event) throws LoggerAppenderException {
        String formatter = formatPattern;
        if (formatter == null || formatter.isBlank()) {
            formatter = DEFAULT_FORMAT_PATTERN;
        }

        // Проверяем, является ли форматтер некорректным
        // Неверный — если не содержит ни одного поддерживаемого плейсхолдера
        var isValid = formatter.contains("%date")
                || formatter.contains("%level")
                || formatter.contains("%serviceCode")
                || formatter.contains("%message")
                || formatter.contains("%entity");

        if (!isValid) {
            formatter = DEFAULT_FORMAT_PATTERN;
            System.err.println("[ConsoleAppender] Неверный формат форматтера строки лога. Используется формат по умолчанию.");
        }

        // Форматируем timestamp
        var timestamp = event.getTimestamp().format(TIMESTAMP_FORMATTER);
        var level = event.getLoggingLevel().name();
        var serviceCode = "[" + event.getServiceCode() + "]";
        var message = event.getMessage();

        // Подставляем плейсхолдеры
        var line = formatter
                .replace("%date", timestamp)
                .replace("%message", message);

        // Применяем форматирование с фиксированной шириной для level и serviceCode
        // Только к первому вхождению каждого плейсхолдера
        if (line.contains("%level")) {
            int idx = line.indexOf("%level");
            line = line.substring(0, idx)
                    + String.format("%-5s", level)
                    + line.substring(idx + "%level".length());
            // Заменяем оставшиеся вхождения (если %level был несколько раз)
            line = line.replace("%level", level);
        }
        if (line.contains("%serviceCode")) {
            int idx = line.indexOf("%serviceCode");
            line = line.substring(0, idx)
                    + String.format("%30s", serviceCode)
                    + line.substring(idx + "%serviceCode".length());
            // Заменяем оставшиеся вхождения (если %serviceCode был несколько раз)
            line = line.replace("%serviceCode", serviceCode);
        }

        // Entities -> JSON
        if (includeEntities && !event.getEntities().isEmpty()) {
            try {
                var entitiesJson = prettyEntities
                        ? ObjectMapperUtils.toPrettyJson(event.getEntities())
                        : ObjectMapperUtils.toJson(event.getEntities());
                line = line.replace("%entity", entitiesJson);
            } catch (JsonProcessingException e) {
                throw new LoggerAppenderException("Не получилось сериализовать LogEvent entities", e);
            }
        } else {
            line = line.replace("%entity", "");
        }

        // Применяем dim-эффект к entities, если нужно
        if (dimEntities && includeEntities && !event.getEntities().isEmpty()) {
            line = applyDimToEntities(line);
        }

        return line;
    }

    /**
     * Применяет ANSI dim-эффект к JSON-строке entities.
     *
     * <p>Оборачивает JSON-строку entities в белый цвет + reset.
     *
     * @param line отформатированная строка лога
     * @return строка с dim-эффектом для entities
     */
    private String applyDimToEntities(String line) {
        // Ищем JSON-объект/массив (начинается с [ или {)
        int jsonStart = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '[' || c == '{') {
                jsonStart = i;
                break;
            }
        }
        if (jsonStart >= 0) {
            // Ищем конец JSON
            int jsonEnd = findJsonEnd(line, jsonStart);
            if (jsonEnd >= 0) {
                return line.substring(0, jsonStart)
                        + AnsiColor.WHITE + line.substring(jsonStart, jsonEnd) + AnsiColor.RESET
                        + line.substring(jsonEnd);
            }
        }
        return line;
    }

    /**
     * Находит конец JSON-объекта/массива.
     *
     * @param line    строка
     * @param start   позиция начала JSON
     * @return позиция конца JSON или -1
     */
    private int findJsonEnd(String line, int start) {
        char open = line.charAt(start);
        char close = (open == '[') ? ']' : '}';
        int depth = 1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start + 1; i < line.length() && depth > 0; i++) {
            char c = line.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '[' || c == '{') {
                    depth++;
                } else if (c == ']' || c == '}') {
                    depth--;
                    if (depth == 0) {
                        return i + 1;
                    }
                }
            }
        }
        return -1;
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
}
