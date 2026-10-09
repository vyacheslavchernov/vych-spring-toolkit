package ru.vych.logger.impl.appenders;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.vych.logger.config.FileAppenderProperties;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты для класса {@link FileAppender}, включая проверку записи логов в файл,
 * фильтрации по уровню, сериализации объектов, ротации по дате и потокобезопасности.
 */
@DisplayName("Тесты для класса FileAppender")
class FileAppenderTests {
    @TempDir
    Path tempDir;

    private FileAppenderProperties properties;
    private FileAppender fileAppender;

    @BeforeEach
    void setUp() {
        properties = new FileAppenderProperties();
        properties.setEnabled(true);
        properties.setDir(tempDir.toString());
        properties.setLevel(LoggingLevel.DEBUG);
        properties.setIncludeEntities(true);
        properties.setPrettyEntities(false);
        properties.setEncoding("UTF-8");
        properties.setDatePattern("");
        properties.setFilenamePattern("app.log");
    }

    @Test
    @DisplayName("Инициализация создаёт файл логов")
    @SneakyThrows
    void initCreatesLogFile() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        assertThat(Files.exists(tempDir.resolve("app.log")))
                .describedAs("Файл логов должен быть создан")
                .isTrue();
    }

    @Test
    @DisplayName("Инициализация создаёт директорию, если не существует")
    @SneakyThrows
    void initCreatesDirectoryIfNotExists() {
        var nestedDir = tempDir.resolve("nested").resolve("deep");
        properties.setDir(nestedDir.toString());

        fileAppender = new FileAppender(properties);
        fileAppender.init();

        assertThat(Files.exists(nestedDir.resolve("app.log")))
                .describedAs("Файл должен быть создан в созданной директории")
                .isTrue();
    }

    @Test
    @DisplayName("Запись лога в файл")
    @SneakyThrows
    void appendWritesLogToFile() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message"
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Файл должен содержать сообщение и serviceCode")
                .contains("INFO")
                .contains("TestService")
                .contains("Test message");
    }

    @Test
    @DisplayName("Запись лога с entities")
    @SneakyThrows
    void appendWritesLogWithEntities() {
        properties.setIncludeEntities(true);
        properties.setPrettyEntities(false);
        properties.setLogFormatter("%date %level %message %entity");
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message",
                new Object[]{"entity1", 42}
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Файл должен содержать entities в JSON")
                .contains("entity1")
                .contains("42");
    }

    @Test
    @DisplayName("Фильтрация по уровню — пропускает события ниже минимального")
    @SneakyThrows
    void appendFiltersByLevel() {
        properties.setLevel(LoggingLevel.WARN);
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var debugEvent = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.DEBUG,
                "Debug message"
        );

        var infoEvent = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Info message"
        );

        var warnEvent = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.WARN,
                "Warn message"
        );

        fileAppender.append(debugEvent);
        fileAppender.append(infoEvent);
        fileAppender.append(warnEvent);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Должно быть только WARN сообщение")
                .contains("Warn message")
                .doesNotContain("Debug message")
                .doesNotContain("Info message");
    }

    @Test
    @DisplayName("getServiceCode возвращает правильный код")
    @SneakyThrows
    void getServiceCodeReturnsCorrectCode() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        assertThat(fileAppender.getServiceCode())
                .describedAs("Код аппендера должен быть FileAppender")
                .isEqualTo("FileAppender");
    }

    @Test
    @DisplayName("Pretty entities добавляет отступы в JSON")
    @SneakyThrows
    void appendWithPrettyEntities() {
        properties.setPrettyEntities(true);
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message",
                new Object[]{"entity1"}
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Pretty JSON должен содержать переносы строк")
                .contains("\n");
    }

    @Test
    @DisplayName("Отключение entities — не записывает их в файл")
    @SneakyThrows
    void appendWithoutEntities() {
        properties.setIncludeEntities(false);
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message",
                new Object[]{"myEntity", "myValue"}
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Файл не должен содержать entities")
                .doesNotContain("myEntity")
                .doesNotContain("myValue");
    }

    @Test
    @DisplayName("Multiple append — добавляет несколько записей")
    @SneakyThrows
    void multipleAppends() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event1 = LogEvent.create("S1", "u1", LoggingLevel.INFO, "Message 1");
        var event2 = LogEvent.create("S2", "u2", LoggingLevel.WARN, "Message 2");

        fileAppender.append(event1);
        fileAppender.append(event2);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Должно быть две записи")
                .contains("Message 1")
                .contains("Message 2");
    }

    @Test
    @DisplayName("Формат по умолчанию включает serviceCode")
    @SneakyThrows
    void defaultFormatterIncludesServiceCode() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "MyService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message"
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Формат по умолчанию должен включать serviceCode")
                .contains("MyService")
                .contains("INFO")
                .contains("Test message");
    }

    @Test
    @DisplayName("Кастомный форматтер лога")
    @SneakyThrows
    void customLogFormatter() {
        properties.setLogFormatter("[%date] [%level] %message");
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message"
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Формат строки должен соответствовать кастомному форматтеру")
                .startsWith("[")
                .contains("INFO")
                .contains("Test message");
    }

    @Test
    @DisplayName("Форматтер с entities")
    @SneakyThrows
    void logFormatterWithEntities() {
        properties.setLogFormatter("%date %level %message %entity");
        properties.setIncludeEntities(true);
        properties.setPrettyEntities(false);
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message",
                new Object[]{"entity1"}
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Строка должна содержать entities")
                .contains("entity1");
    }

    @Test
    @DisplayName("Форматтер без %entity — не выбрасывает исключение")
    @SneakyThrows
    void logFormatterWithoutEntityPlaceholder() {
        properties.setLogFormatter("%date %level %message");
        properties.setIncludeEntities(true);
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message",
                new Object[]{"entity1"}
        );

        // Не должно выбросить исключение
        assertThatCode(() -> fileAppender.append(event))
                .describedAs("Запись должна быть успешной даже если в форматтере нет %entity")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Существующий файл с таким же именем — добавляется суффикс _1")
    @SneakyThrows
    void existingFileWithSameNameGetsSuffix() {
        // Создаём существующий файл
        var existingFile = tempDir.resolve("app.log");
        Files.writeString(existingFile, "existing content");

        properties.setDatePattern("");
        properties.setFilenamePattern("app.log");
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        // Должен быть создан файл с суффиксом _1
        var newFile = tempDir.resolve("app_1.log");
        assertThat(Files.exists(newFile))
                .describedAs("Должен быть создан файл с суффиксом _1")
                .isTrue();

        // Существующий файл не должен быть изменён
        var existingContent = Files.readString(existingFile, StandardCharsets.UTF_8);
        assertThat(existingContent)
                .describedAs("Существующий файл не должен быть изменён")
                .isEqualTo("existing content");
    }

    @Test
    @DisplayName("Закрытие аппендера закрывает файл")
    @SneakyThrows
    void closeClosesFile() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create("S1", "u1", LoggingLevel.INFO, "Message");
        fileAppender.append(event);

        fileAppender.close();

        // После закрытия файл должен содержать запись
        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        assertThat(content)
                .describedAs("Файл должен содержать запись после закрытия")
                .contains("Message");
    }

    @Test
    @DisplayName("Имя файла с датой и timestamp")
    @SneakyThrows
    void filenameWithDateAndTimestamp() {
        properties.setDatePattern("yyyy-MM-dd");
        properties.setFilenamePattern("app-{date}_{timestamp}.log");
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        // Проверяем, что файл был создан с правильным именем
        var files = Files.list(tempDir).toList();
        assertThat(files)
                .describedAs("Должен быть создан один файл")
                .hasSize(1);

        var filename = files.get(0).getFileName().toString();
        assertThat(filename)
                .describedAs("Имя файла должно соответствовать паттерну с датой и timestamp")
                .matches("app-\\d{4}-\\d{2}-\\d{2}_\\d+\\.log");
    }

    @Test
    @DisplayName("Неверный форматтер — используется формат по умолчанию")
    @SneakyThrows
    void invalidFormatterUsesDefault() {
        properties.setLogFormatter("%unknown_placeholder");
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var event = LogEvent.create(
                "TestService",
                "uuid-123",
                LoggingLevel.INFO,
                "Test message"
        );

        fileAppender.append(event);

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        // Формат по умолчанию должен быть использован — должны присутствовать все компоненты
        assertThat(content)
                .describedAs("При неверном форматтере должен использоваться формат по умолчанию")
                .contains("INFO")
                .contains("TestService")
                .contains("Test message");
    }

    @Test
    @DisplayName("Потокобезопасность — несколько потоков пишут в один файл")
    @SneakyThrows
    void threadSafety() {
        fileAppender = new FileAppender(properties);
        fileAppender.init();

        var threadCount = 10;
        var messagesPerThread = 100;
        var barriers = new CountDownLatch(threadCount);
        var threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    try {
                        var event = LogEvent.create(
                                "Thread-" + threadId,
                                "uuid-" + threadId + "-" + j,
                                LoggingLevel.INFO,
                                "Message " + j
                        );
                        fileAppender.append(event);
                    } catch (LoggerAppenderException e) {
                        // Ignore
                    }
                }
                barriers.countDown();
            });
        }

        for (var thread : threads) {
            thread.start();
        }

        barriers.await();

        var content = Files.readString(tempDir.resolve("app.log"), StandardCharsets.UTF_8);
        var lineCount = content.lines().count();
        assertThat(lineCount)
                .describedAs("Должно быть записей: " + (threadCount * messagesPerThread))
                .isEqualTo(threadCount * messagesPerThread);
    }
}
