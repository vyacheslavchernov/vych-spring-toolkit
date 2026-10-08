package ru.vych.logger.impl.appenders;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import ru.vych.logger.config.FileAppenderProperties;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.common.ObjectMapperUtils;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Аппендер для записи лог-сообщений в файлы с поддержкой ежедневной ротации,
 * настраиваемого форматирования и буферизации.
 *
 * <p>Поддерживает:
 * <ul>
 *   <li>Фильтрацию по уровню логирования</li>
 *   <li>Ротацию файлов по дате (ежедневную)</li>
 *   <li>Настраиваемый формат строки лога через плейсхолдеры</li>
 *   <li>Сериализацию дополнительных объектов в JSON</li>
 *   <li>Буферизацию записей</li>
 *   <li>Потокобезопасную запись</li>
 * </ul>
 *
 * <p>Формат имени файла по умолчанию: {@code app-2026-10-08_1728391822.log}
 *
 * <p>Формат строки лога по умолчанию идентичен консольному аппендеру:
 * {@code <timestamp>     <LEVEL>     [<serviceCode>] : <message> <entities>}
 *
 * @see LogAppender
 * @see FileAppenderProperties
 * @see LoggingLevel
 */
@RequiredArgsConstructor
public class FileAppender implements LogAppender {
    /**
     * Код этого аппендера, используется при логировании внутренних сообщений.
     */
    private final static String SERVICE_CODE = "FileAppender";

    /**
     * Конфигурационные свойства файлового аппендера.
     */
    private final FileAppenderProperties properties;

    /**
     * Путь к текущему лог-файлу.
     */
    private transient Path logFilePath;

    /**
     * Текущая дата в имени файла для отслеживания необходимости ротации.
     */
    private transient LocalDate currentDateInFilename;

    /**
     * Буферизированный писатель для записи в файл.
     */
    private transient BufferedWriter writer;

    /**
     * Монитор для потокобезопасной записи.
     */
    private final Object writeMonitor = new Object();

    /**
     * Внутреннее логирование аппендера.
     * Диагностические сообщения (инициализация, ротация, закрытие) → System.out.
     * Ошибки → System.err.
     *
     * @param message сообщение для вывода
     */
    private void internalLog(String message) {
        System.out.println("[" + SERVICE_CODE + "] " + message);
    }

    /**
     * Внутреннее логирование ошибок аппендера.
     *
     * @param message сообщение об ошибке
     */
    private void internalLogError(String message) {
        System.err.println("[" + SERVICE_CODE + "] " + message);
    }

    /**
     * Инициализирует аппендер: создаёт директорию (если нужно) и открывает поток записи.
     *
     * @throws LoggerAppenderException в случае ошибки инициализации файла
     */
    public void init() throws LoggerAppenderException {
        try {
            // Валидация паттерна даты
            properties.validate();

            // Создание директории
            var parent = Paths.get(properties.getDir());
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Открытие файла
            openNewFile();

            internalLog("Инициализирован. Файл логов: " + logFilePath);
        } catch (IOException e) {
            throw new LoggerAppenderException("Не удалось инициализировать файл логов: " + properties.getDir(), e);
        }
    }

    /**
     * Открывает новый файл для записи.
     *
     * @throws IOException в случае ошибки файловой системы
     */
    private void openNewFile() throws IOException {
        var filePath = properties.getFullLogFilePath();
        logFilePath = Paths.get(filePath);

        var parent = logFilePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        // Create file if it doesn't exist
        if (!Files.exists(logFilePath)) {
            Files.createFile(logFilePath);
        }

        var charset = Charset.forName(properties.getEncoding());
        writer = new BufferedWriter(
                Files.newBufferedWriter(logFilePath, charset, StandardOpenOption.APPEND),
                properties.getBufferSize()
        );

        // Запоминаем текущую дату из имени файла (если есть дата в имени)
        var filename = logFilePath.getFileName().toString();
        currentDateInFilename = extractDateFromFilename(filename);
    }

    /**
     * Извлекает дату из имени файла.
     *
     * @param filename имя файла
     * @return LocalDate если дата найдена, null иначе
     */
    private LocalDate extractDateFromFilename(String filename) {
        var datePattern = properties.getDatePattern();
        if (datePattern == null || datePattern.isBlank()) {
            return null;
        }

        var formatter = DateTimeFormatter.ofPattern(datePattern);

        // Формат: app-2026-10-08_1728391822.log
        // Или: app-2026-10-08_1728391822_1.log
        var fileNameWithoutExt = filename.replace(".log", "");

        // Убираем суффикс _1, _2 и т.д.
        var lastUnderscore = fileNameWithoutExt.lastIndexOf('_');
        var possibleSuffix = fileNameWithoutExt.substring(lastUnderscore + 1);

        // Проверяем, является ли суффикс числовым (timestamp или _N)
        String nameWithoutSuffix;
        try {
            Long.parseLong(possibleSuffix);
            nameWithoutSuffix = fileNameWithoutExt.substring(0, lastUnderscore);
        } catch (NumberFormatException e) {
            nameWithoutSuffix = fileNameWithoutExt;
        }

        // Извлекаем дату из оставшейся части (app-2026-10-08)
        var firstDash = nameWithoutSuffix.indexOf('-');
        if (firstDash < 0) {
            return null;
        }

        var dateStr = nameWithoutSuffix.substring(firstDash + 1);

        // Обрезаем до ожидаемой длины
        var expectedLength = switch (datePattern) {
            case "yyyy-MM-dd" -> 10;
            case "yyyy-MM" -> 7;
            case "yyyy" -> 4;
            default -> datePattern.length();
        };

        if (dateStr.length() < expectedLength) {
            return null;
        }

        dateStr = dateStr.substring(0, expectedLength);

        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Добавляет событие логирования в файл.
     *
     * <p>Если уровень события ниже настроенного минимального — событие пропускается.
     * Если дата в имени текущего файла отличается от текущей даты — выполняется ротация.
     *
     * @param event событие логирования
     * @throws LoggerAppenderException в случае ошибки записи в файл или сериализации объектов
     */
    @Override
    public void append(LogEvent event) throws LoggerAppenderException {
        if (event.getLoggingLevel().getValue() < properties.getLevel().getValue()) {
            return;
        }

        synchronized (writeMonitor) {
            try {
                // Проверяем необходимость ротации по дате
                checkDateRotation();

                var logLine = formatLogLine(event);
                writer.write(logLine);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new LoggerAppenderException("Не удалось записать лог в файл: " + logFilePath, e);
            }
        }
    }

    /**
     * Проверяет необходимость ротации файла по дате.
     *
     * @throws IOException в случае ошибки при открытии нового файла
     */
    private void checkDateRotation() throws IOException {
        if (currentDateInFilename == null) {
            return;
        }

        var today = LocalDate.now();
        if (!currentDateInFilename.isEqual(today)) {
            // Закрываем текущий файл и открываем новый
            closeWriter();
            openNewFile();

            internalLog("Выполнена ротация файла. Новый файл: " + logFilePath);
        }
    }

    /**
     * Закрывает буферизированный писатель.
     */
    private void closeWriter() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                internalLogError("Не удалось закрыть файл логов: " + logFilePath + ": " + e.getMessage());
            }
        }
    }

    /**
     * Закрывает аппендер при уничтожении bean.
     * Буфер flushится и файл закрывается.
     */
    public void close() {
        synchronized (writeMonitor) {
            closeWriter();
            internalLog("Закрыт.");
        }
    }

    /**
     * Форматирует событие логирования в строку.
     *
     * @param event событие логирования
     * @return отформатированная строка лога
     * @throws LoggerAppenderException в случае ошибки сериализации объектов
     */
    private String formatLogLine(LogEvent event) throws LoggerAppenderException {
        var formatter = properties.getLogFormatter();
        var timestamp = event.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        var level = event.getLoggingLevel().name();
        var serviceCode = "[" + event.getServiceCode() + "]";
        var message = event.getMessage();

        // Проверяем, является ли форматтер некорректным
        // Неверный — если не содержит ни одного поддерживаемого плейсхолдера
        var isValid = formatter.contains("%date")
                || formatter.contains("%level")
                || formatter.contains("%serviceCode")
                || formatter.contains("%message")
                || formatter.contains("%entity");

        if (!isValid) {
            // Используем формат по умолчанию
            formatter = "%date     %level     %serviceCode : %message %entity";
            internalLog("Неверный формат форматтера строки лога. Используется формат по умолчанию.");
        }

        var line = formatter
                .replace("%date", timestamp)
                .replace("%level", level)
                .replace("%serviceCode", serviceCode)
                .replace("%message", message);

        // Применяем форматирование с фиксированной шириной для level и serviceCode
        // Ищем паттерн %level и заменяем на отформатированный
        if (line.contains("%level")) {
            line = line.replace("%level", String.format("%-5s", level));
        }
        if (line.contains("%serviceCode")) {
            line = line.replace("%serviceCode", String.format("%30s", serviceCode));
        }

        if (properties.isIncludeEntities() && !event.getEntities().isEmpty()) {
            try {
                var entitiesJson = properties.isPrettyEntities()
                        ? ObjectMapperUtils.toPrettyJson(event.getEntities())
                        : ObjectMapperUtils.toJson(event.getEntities());
                line = line.replace("%entity", entitiesJson);
            } catch (JsonProcessingException e) {
                throw new LoggerAppenderException("Не получилось сериализовать LogEvent entities", e);
            }
        } else {
            line = line.replace("%entity", "");
        }

        return line;
    }

    /**
     * Возвращает код этого аппендера.
     *
     * @return {@code "FileAppender"}
     */
    @Override
    public String getServiceCode() {
        return SERVICE_CODE;
    }
}
