package ru.vych.logger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конфигурационные свойства файлового аппендера.
 *
 * <p>Привязывается к свойствам с префиксом {@code logger.file} из application.yaml / application.properties.
 *
 * @see LogProperties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "logger.file")
public class FileAppenderProperties {
    /**
     * Включён ли файловый аппендер. По умолчанию — {@code false}.
     */
    private boolean enabled = false;

    /**
     * Директория для файлов логов. По умолчанию — {@code ./logs}.
     */
    private String dir = "./logs";

    /**
     * Паттерн имени файла. Поддерживает переменные {@code {date}} и {@code {timestamp}}.
     * По умолчанию — {@code app-{date}_{timestamp}.log}.
     */
    private String filenamePattern = "app-{date}_{timestamp}.log";

    /**
     * Формат даты в имени файла (DateTimeFormatter pattern).
     * По умолчанию — {@code yyyy-MM-dd}.
     */
    private String datePattern = "yyyy-MM-dd";

    /**
     * Кодировка файла логов. По умолчанию — {@code UTF-8}.
     */
    private String encoding = "UTF-8";

    /**
     * Минимальный уровень логирования для файла.
     * По умолчанию — {@link ru.vych.logger.impl.common.LoggingLevel#INFO}.
     */
    private ru.vych.logger.impl.common.LoggingLevel level = ru.vych.logger.impl.common.LoggingLevel.INFO;

    /**
     * Включать ли дополнительные объекты (entities) в вывод.
     * По умолчанию — {@code false}.
     */
    private boolean includeEntities = false;

    /**
     * Форматировать ли JSON объектов с отступами (pretty-print).
     * По умолчанию — {@code false}.
     */
    private boolean prettyEntities = false;

    /**
     * Размер буфера в байтах. По умолчанию — {@code 8192}.
     */
    private int bufferSize = 8192;

    /**
     * Форматтер строки лога. Поддерживает плейсхолдеры: {@code %date}, {@code %level},
     * {@code %serviceCode}, {@code %message}, {@code %entity}.
     * По умолчанию — формат идентичен консольному аппендеру:
     * {@code "%date     %level     %serviceCode : %message %entity"}.
     */
    private String logFormatter = "%date     %level     %serviceCode : %message %entity";

    /**
     * Форматирует временную метку в строку согласно {@link #datePattern}.
     *
     * @return отформатированная строка с датой/временем
     */
    public String formatDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(datePattern));
    }

    /**
     * Генерирует UNIX timestamp (секунды) для уникальности имени файла.
     *
     * @return UNIX timestamp в секундах
     */
    public String generateTimestamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }

    /**
     * Генерирует полное имя файла по паттерну с подстановкой даты и timestamp.
     *
     * @return полное имя файла
     */
    public String generateFilename() {
        var date = formatDate();
        var timestamp = generateTimestamp();
        var filename = filenamePattern
                .replace("{date}", date)
                .replace("{timestamp}", timestamp);
        return filename;
    }

    /**
     * Генерирует уникальный путь к файлу, добавляя суффикс при существовании файла с таким же именем.
     *
     * @return уникальный путь к файлу
     */
    public String getFullLogFilePath() {
        var pathValue = Paths.get(dir);
        var basePath = pathValue.getParent() != null
                ? pathValue.toString()
                : ".";

        String filename;
        if (datePattern != null && !datePattern.isBlank()) {
            filename = generateFilename();
        } else {
            // Если datePattern пустой, используем дефолтное имя файла
            filename = "app.log";
        }

        var filePath = basePath + "/" + filename;

        // Добавляем суффикс _1, _2, и т.д. если файл уже существует
        var file = Paths.get(filePath);
        var counter = 1;
        while (Files.exists(file)) {
            var nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            var extension = filename.substring(filename.lastIndexOf('.'));
            var newFilename = nameWithoutExt + "_" + counter + extension;
            filePath = basePath + "/" + newFilename;
            file = Paths.get(filePath);
            counter++;
        }

        return filePath;
    }

    /**
     * Валидирует паттерн даты.
     *
     * @throws IllegalArgumentException если паттерн невалидный
     */
    public void validate() {
        try {
            DateTimeFormatter.ofPattern(datePattern);
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный паттерн даты: " + datePattern, e);
        }
    }
}
