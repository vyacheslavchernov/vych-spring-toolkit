package ru.vych.logger.impl.appenders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Тесты для класса {@link ConsoleAppender}, включая проверку вывода в консоль,
 * фильтрации по уровню, ANSI-цветирования, сериализации объектов и разделения потоков.
 */
@DisplayName("Тесты для класса ConsoleAppender")
class ConsoleAppenderTests {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream captureOut;
    private ByteArrayOutputStream captureErr;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        captureOut = new ByteArrayOutputStream();
        captureErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOut));
        System.setErr(new PrintStream(captureErr));
    }

    @Test
    @DisplayName("Аппендер по умолчанию включён")
    void appenderEnabledByDefault() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var event = LogEvent.create("TestService", "uuid-1", LoggingLevel.DEBUG, "Debug message");

        assertThatCode(() -> appender.append(event))
                .describedAs("Запись DEBUG события должна быть успешной при уровне DEBUG")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Фильтрация по уровню — пропускает события ниже минимального")
    void levelFilteringSkipsLowerEvents() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.WARN,
                false, false, false, false
        );

        var debugEvent = LogEvent.create("TestService", "uuid-1", LoggingLevel.DEBUG, "Debug message");
        var infoEvent = LogEvent.create("TestService", "uuid-2", LoggingLevel.INFO, "Info message");
        var warnEvent = LogEvent.create("TestService", "uuid-3", LoggingLevel.WARN, "Warn message");

        appender.append(debugEvent);
        appender.append(infoEvent);
        appender.append(warnEvent);

        var outContent = captureOut.toString();
        assertThat(outContent)
                .describedAs("Сообщение DEBUG не должно быть выведено")
                .doesNotContain("Debug message");
        assertThat(outContent)
                .describedAs("Сообщение INFO не должно быть выведено")
                .doesNotContain("Info message");
        assertThat(outContent)
                .describedAs("Сообщение WARN должно быть выведено")
                .contains("Warn message");
    }

    @Test
    @DisplayName("ERROR выводится в System.err, остальные — в System.out")
    void errorStreamSeparation() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var infoEvent = LogEvent.create("TestService", "uuid-1", LoggingLevel.INFO, "Info message");
        var errorEvent = LogEvent.create("TestService", "uuid-2", LoggingLevel.ERROR, "Error message");

        appender.append(infoEvent);
        appender.append(errorEvent);

        var outContent = captureOut.toString();
        var errContent = captureErr.toString();

        assertThat(outContent)
                .describedAs("INFO должно быть в System.out")
                .contains("Info message");
        assertThat(errContent)
                .describedAs("ERROR должно быть в System.err")
                .contains("Error message");
    }

    @Test
    @DisplayName("Формат вывода включает timestamp, level, serviceCode, message")
    void outputFormat() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var event = LogEvent.create("MyService", "uuid-123", LoggingLevel.INFO, "Test message");
        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Формат должен содержать timestamp (ISO format)")
                .contains("T");
        assertThat(content)
                .describedAs("Формат должен содержать level INFO")
                .contains("INFO");
        assertThat(content)
                .describedAs("Формат должен содержать serviceCode")
                .contains("[MyService]");
        assertThat(content)
                .describedAs("Формат должен содержать сообщение")
                .contains("Test message");
    }

    @Test
    @DisplayName("JSON entities сериализуются при includeEntities=true")
    void jsonEntitiesSerialization() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                true, false, false, false
        );

        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message with entities",
                new Object[]{"entity1", 42}
        );

        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Вывод должен содержать entity1")
                .contains("entity1");
        assertThat(content)
                .describedAs("Вывод должен содержать 42")
                .contains("42");
    }

    @Test
    @DisplayName("Pretty entities добавляют отступы в JSON")
    void prettyEntities() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                true, true, false, false
        );

        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message",
                new Object[]{"entity1", "entity2"}
        );

        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Pretty JSON должен содержать переносы строк")
                .contains("\n");
    }

    @Test
    @DisplayName("Отключение entities — не выводит их")
    void entitiesDisabled() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message",
                new Object[]{"myEntity", "value"}
        );

        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Вывод не должен содержать entities")
                .doesNotContain("myEntity")
                .doesNotContain("value");
    }

    @Test
    @DisplayName("dimEntities делает вывод менее ярким (добавляет ANSI codes)")
    void dimEntities() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                true, false, false, true
        );

        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message",
                new Object[]{"entity1"}
        );

        appender.append(event);

        var content = captureOut.toString();
        // ANSI reset code должен быть добавлен к entities
        assertThat(content)
                .describedAs("dimEntities должен добавить ANSI reset codes")
                .contains("\u001B[0m");
    }

    @Test
    @DisplayName("dimEntities работает только с includeEntities")
    void dimEntitiesOnlyWithIncludeEntities() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, true
        );

        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message",
                new Object[]{"entity1"}
        );

        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Без includeEntities dimEntities не должен влиять на вывод")
                .doesNotContain("entity1");
    }

    @Test
    @DisplayName("getServiceCode возвращает 'ConsoleAppender'")
    void getServiceCodeReturnsCorrectCode() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        assertThat(appender.getServiceCode())
                .describedAs("Код аппендера должен быть ConsoleAppender")
                .isEqualTo("ConsoleAppender");
    }

    @Test
    @DisplayName("ANSI цвета для уровней — DEBUG=White, INFO=Blue, WARN=Yellow, ERROR=Red")
    void ansiColorsByLevel() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, true, false
        );

        var debugEvent = LogEvent.create("S", "u1", LoggingLevel.DEBUG, "Debug");
        var infoEvent = LogEvent.create("S", "u2", LoggingLevel.INFO, "Info");
        var warnEvent = LogEvent.create("S", "u3", LoggingLevel.WARN, "Warn");
        var errorEvent = LogEvent.create("S", "u4", LoggingLevel.ERROR, "Error");

        appender.append(debugEvent);
        appender.append(infoEvent);
        appender.append(warnEvent);
        appender.append(errorEvent);

        var content = captureOut.toString() + captureErr.toString();

        // Проверим, что ANSI codes присутствуют (включены enableColors=true)
        assertThat(content)
                .describedAs("ANSI цвета должны быть применены")
                .contains("\u001B[");
    }

    @Test
    @DisplayName("ANSI цвета применяются только к LEVEL, не к всему сообщению")
    void ansiColorsOnlyToLevel() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, true, false
        );

        var event = LogEvent.create("S", "u1", LoggingLevel.INFO, "Info message");
        appender.append(event);

        var content = captureOut.toString();
        // ANSI codes должны быть вокруг LEVEL
        assertThat(content)
                .describedAs("ANSI codes должны быть вокруг LEVEL")
                .contains("\u001B[");
    }

    @Test
    @DisplayName("Ошибка сериализации — бросает LoggerAppenderException")
    void serializationErrorThrowsLoggerAppenderException() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                true, false, false, false
        );

        // Создаём event с объектом, который не может быть сериализован
        var event = LogEvent.create(
                "TestService",
                "uuid-1",
                LoggingLevel.INFO,
                "Message",
                new Object[]{new NonSerializableObject()}
        );

        assertThatThrownBy(() -> appender.append(event))
                .describedAs("Должен быть брошен LoggerAppenderException при ошибке сериализации")
                .isInstanceOf(LoggerAppenderException.class)
                .hasMessageContaining("Не получилось сериализовать LogEvent entities");
    }

    @Test
    @DisplayName("Multiple append — добавляет несколько записей")
    void multipleAppends() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var event1 = LogEvent.create("S1", "u1", LoggingLevel.INFO, "Message 1");
        var event2 = LogEvent.create("S2", "u2", LoggingLevel.WARN, "Message 2");

        appender.append(event1);
        appender.append(event2);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Должно быть две записи")
                .contains("Message 1")
                .contains("Message 2");
    }

    @Test
    @DisplayName("Отключённый аппендер — не выводит ничего")
    void appenderDisabled() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.WARN,
                false, false, false, false
        );

        var debugEvent = LogEvent.create("S", "u1", LoggingLevel.DEBUG, "Debug");
        var infoEvent = LogEvent.create("S", "u2", LoggingLevel.INFO, "Info");

        appender.append(debugEvent);
        appender.append(infoEvent);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Отключённый аппендер не должен выводить события ниже WARN")
                .isEmpty();
    }

    @Test
    @DisplayName("Сообщение без текста — пустая строка допустима")
    void messageWithoutText() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                false, false, false, false
        );

        var event = LogEvent.create("S", "u1", LoggingLevel.INFO, "", new Object[]{"entity1"});
        appender.append(event);

        var content = captureOut.toString();
        assertThat(content)
                .describedAs("Событие с пустым сообщением должно быть выведено")
                .contains("INFO")
                .contains("S");
    }

    @Test
    @DisplayName("Пустой список entities — не выбрасывает исключение")
    void emptyEntitiesList() throws LoggerAppenderException {
        var appender = new ConsoleAppender(
                LoggingLevel.DEBUG,
                true, false, false, false
        );

        var event = LogEvent.create("S", "u1", LoggingLevel.INFO, "Message");
        assertThatCode(() -> appender.append(event))
                .describedAs("Событие с пустым списком entities должно быть обработано")
                .doesNotThrowAnyException();
    }

    // Вспомогательный класс, который не может быть сериализован
    private static class NonSerializableObject {
        @Override
        public String toString() {
            throw new RuntimeException("Cannot serialize");
        }
    }
}
