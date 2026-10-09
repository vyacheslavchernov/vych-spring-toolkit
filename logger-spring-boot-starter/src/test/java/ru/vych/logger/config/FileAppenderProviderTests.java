package ru.vych.logger.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vych.logger.impl.appenders.FileAppender;
import ru.vych.logger.impl.common.LoggingLevel;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты для класса {@link FileAppenderProvider}, включая проверку создания и инициализации аппендера.
 */
@DisplayName("Тесты для класса FileAppenderProvider")
class FileAppenderProviderTests {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("create создаёт и инициализирует FileAppender")
    void createCreatesAndInitializesAppender() {
        var properties = new FileAppenderProperties();
        properties.setEnabled(true);
        properties.setDir(tempDir.toString());
        properties.setDatePattern("");
        properties.setLevel(LoggingLevel.DEBUG);

        var provider = new FileAppenderProvider(properties);

        assertThatCode(provider::create)
                .describedAs("Создание аппендера не должно выбрасывать исключений")
                .doesNotThrowAnyException();

        assertThat(Files.exists(tempDir.resolve("app.log")))
                .describedAs("Файл логов должен быть создан")
                .isTrue();
    }

    @Test
    @DisplayName("create возвращает экземпляр FileAppender")
    @SneakyThrows
    void createReturnsFileAppenderInstance() {
        var properties = new FileAppenderProperties();
        properties.setDir(tempDir.toString());
        properties.setDatePattern("");

        var provider = new FileAppenderProvider(properties);
        var appender = provider.create();

        assertThat(appender)
                .describedAs("Должен вернуть экземпляр FileAppender")
                .isInstanceOf(FileAppender.class);
    }

    @Test
    @DisplayName("create создаёт директорию для файла логов")
    @SneakyThrows
    void createCreatesDirectoryForLogFile() {
        var properties = new FileAppenderProperties();
        var nestedDir = tempDir.resolve("nested").resolve("deep");
        properties.setDir(nestedDir.toString());
        properties.setDatePattern("");

        var provider = new FileAppenderProvider(properties);

        assertThatCode(provider::create)
                .describedAs("Создание не должно выбрасывать исключений при отсутствии директории")
                .doesNotThrowAnyException();

        assertThat(Files.exists(nestedDir.resolve("app.log")))
                .describedAs("Файл должен быть создан в созданной директории")
                .isTrue();
    }
}
