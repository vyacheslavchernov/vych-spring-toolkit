package ru.vych.logger.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.logger.impl.common.LoggingLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для класса {@link FileAppenderProperties}, включая проверку генерации путей к файлам,
 * форматирования дат и валидации конфигурации.
 */
@DisplayName("Тесты для класса FileAppenderProperties")
class FileAppenderPropertiesTests {
    @Test
    @DisplayName("Значения по умолчанию")
    void defaultValues() {
        var properties = new FileAppenderProperties();

        assertThat(properties.isEnabled())
                .describedAs("По умолчанию отключён")
                .isFalse();
        assertThat(properties.getDir())
                .describedAs("Директория по умолчанию")
                .isEqualTo("./logs");
        assertThat(properties.getFilenamePattern())
                .describedAs("Паттерн имени файла по умолчанию")
                .isEqualTo("app-{date}_{timestamp}.log");
        assertThat(properties.getDatePattern())
                .describedAs("Паттерн даты по умолчанию")
                .isEqualTo("yyyy-MM-dd");
        assertThat(properties.getEncoding())
                .describedAs("Кодировка по умолчанию")
                .isEqualTo("UTF-8");
        assertThat(properties.getLevel())
                .describedAs("Уровень по умолчанию")
                .isEqualTo(LoggingLevel.INFO);
        assertThat(properties.isIncludeEntities())
                .describedAs("Entities выключены по умолчанию")
                .isFalse();
        assertThat(properties.isPrettyEntities())
                .describedAs("Pretty entities выключены по умолчанию")
                .isFalse();
        assertThat(properties.getBufferSize())
                .describedAs("Размер буфера по умолчанию")
                .isEqualTo(8192);
        assertThat(properties.getLogFormatter())
                .describedAs("Форматтер лога по умолчанию (идентичен консольному аппендеру)")
                .isEqualTo("%date     %level     %serviceCode : %message %entity");
    }

    @Test
    @DisplayName("generateFilename с datePattern и timestamp")
    void generateFilenameWithDateAndTimestamp() {
        var properties = new FileAppenderProperties();
        var filename = properties.generateFilename();

        assertThat(filename)
                .describedAs("Имя файла должно содержать дату и timestamp")
                .matches("app-\\d{4}-\\d{2}-\\d{2}_\\d+\\.log");
    }

    @Test
    @DisplayName("generateFilename с кастомным паттерном")
    void generateFilenameWithCustomPattern() {
        var properties = new FileAppenderProperties();
        properties.setFilenamePattern("myapp-{date}_{timestamp}.log");

        var filename = properties.generateFilename();

        assertThat(filename)
                .describedAs("Имя файла должно использовать кастомный паттерн")
                .matches("myapp-\\d{4}-\\d{2}-\\d{2}_\\d+\\.log");
    }

    @Test
    @DisplayName("generateFilename с кастомным datePattern")
    void generateFilenameWithCustomDatePattern() {
        var properties = new FileAppenderProperties();
        properties.setDatePattern("yyyy-MM");

        var filename = properties.generateFilename();

        assertThat(filename)
                .describedAs("Имя файла должно содержать дату в формате yyyy-MM")
                .matches("app-\\d{4}-\\d{2}_\\d+\\.log");
    }

    @Test
    @DisplayName("getFullLogFilePath без существующего файла")
    void fullLogFilePathWithoutExistingFile() {
        var properties = new FileAppenderProperties();
        properties.setDatePattern("");

        var filePath = properties.getFullLogFilePath();
        assertThat(filePath)
                .describedAs("Путь должен быть без суффикса даты")
                .isEqualTo("./logs/app.log");
    }

    @Test
    @DisplayName("getFullLogFilePath с datePattern")
    void fullLogFilePathWithDatePattern() {
        var properties = new FileAppenderProperties();

        var filePath = properties.getFullLogFilePath();
        assertThat(filePath)
                .describedAs("Путь должен содержать суффикс даты")
                .matches("\\./logs/app-\\d{4}-\\d{2}-\\d{2}_\\d+\\.log");
    }

    @Test
    @DisplayName("getFullLogFilePath с кастомной директорией")
    void fullLogFilePathWithCustomDir() {
        var properties = new FileAppenderProperties();
        properties.setDir("/var/log/myapp");
        properties.setDatePattern("");

        var filePath = properties.getFullLogFilePath();
        assertThat(filePath)
                .describedAs("Путь должен использовать кастомную директорию")
                .isEqualTo("/var/log/myapp/app.log");
    }

    @Test
    @DisplayName("Настройка свойств")
    void propertySetters() {
        var properties = new FileAppenderProperties();
        properties.setEnabled(true);
        properties.setDir("/tmp/logs");
        properties.setFilenamePattern("service-{date}_{timestamp}.log");
        properties.setDatePattern("yyyy-MM-dd");
        properties.setLevel(LoggingLevel.DEBUG);
        properties.setIncludeEntities(true);
        properties.setPrettyEntities(true);
        properties.setBufferSize(4096);
        properties.setLogFormatter("%date [%level] %message %entity");

        assertThat(properties.isEnabled())
                .describedAs("Enabled")
                .isTrue();
        assertThat(properties.getDir())
                .describedAs("Dir")
                .isEqualTo("/tmp/logs");
        assertThat(properties.getFilenamePattern())
                .describedAs("FilenamePattern")
                .isEqualTo("service-{date}_{timestamp}.log");
        assertThat(properties.getLevel())
                .describedAs("Level")
                .isEqualTo(LoggingLevel.DEBUG);
        assertThat(properties.getBufferSize())
                .describedAs("BufferSize")
                .isEqualTo(4096);
        assertThat(properties.getLogFormatter())
                .describedAs("LogFormatter")
                .isEqualTo("%date [%level] %message %entity");
    }

    @Test
    @DisplayName("validate выбрасывает исключение при неверном паттерне даты")
    void validateThrowsOnInvalidDatePattern() {
        var properties = new FileAppenderProperties();
        properties.setDatePattern("invalid[[pattern");

        assertThatThrownBy(properties::validate)
                .describedAs("Должно выбросить IllegalArgumentException")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный паттерн даты");
    }

    @Test
    @DisplayName("validate не выбрасывает исключение при верном паттерне")
    void validateDoesNotThrowOnValidDatePattern() {
        var properties = new FileAppenderProperties();
        properties.setDatePattern("yyyy-MM-dd");

        assertThatCode(properties::validate)
                .describedAs("Валидация не должна выбрасывать исключений")
                .doesNotThrowAnyException();
    }
}
