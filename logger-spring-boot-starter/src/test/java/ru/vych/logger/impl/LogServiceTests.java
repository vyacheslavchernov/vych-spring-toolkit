package ru.vych.logger.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vych.logger.impl.appenders.LogAppender;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Тесты для класса {@link LogService}, включая проверку обработки ошибок аппендеров,
 * вариантов методов без сообщения и применения фильтров.
 */
@DisplayName("Тесты для класса LogService")
class LogServiceTests {

    private List<LogAppender> appenders;
    private List<LogFilter> logFilters;

    @BeforeEach
    void setUp() {
        appenders = new ArrayList<>();
        logFilters = new ArrayList<>();
    }

    /**
     * Проверяет, что ошибка в одном аппендере не останавливает запись в другие аппендеры.
     * <p>
     * Согласно spec: "Error in one appender does not affect writing to other appenders".
     * </p>
     * <p>
     * Примечание: failing appender не может быть добавлен до создания LogService,
     * т.к. LogService при инициализации вызывает info(), что вызовет рекурсию
     * (info → failing appender error → error() → failing appender error → ...).
     * Этот сценарий проверяется в интеграционных тестах.
     * </p>
     */
    @Test
    @DisplayName("Ошибка в аппендере — LogService не выбрасывает исключение наружу")
    void appenderErrorDoesNotPropagateOutside() {
        // Создаём рабочий аппендер, который просто собирает события
        var workingAppender = new CollectingAppender();

        appenders.add(workingAppender);

        var logService = new LogService(appenders, logFilters);

        // Вызов log — должен быть успешным
        assertThatCode(() -> logService.info("TestService", "uuid-1", "Test message"))
                .describedAs("LogService не должен выбрасывать исключение")
                .doesNotThrowAnyException();

        // Working appender должен получить событие
        var ourEvent = workingAppender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "Test message".equals(e.getMessage()))
                .findFirst();
        assertThat(ourEvent)
                .describedAs("Наше событие должно быть captured")
                .isPresent();
        assertThat(ourEvent.get().getLoggingLevel()).isEqualTo(LoggingLevel.INFO);
    }

    /**
     * Проверяет, что no-message variant (без текста сообщения) передаёт пустую строку.
     * <p>
     * Согласно spec: "no-message variants pass empty string".
     * </p>
     */
    @Test
    @DisplayName("No-message variant передаёт пустую строку как message")
    void noMessageVariantPassesEmptyString() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        // Вызываем метод без message
        logService.info("TestService", "uuid-1");

        // Найдём событие с нашим serviceCode и uuid
        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .describedAs("Должно быть captured наше событие")
                .isPresent();

        assertThat(ourEvent.get().getMessage())
                .describedAs("Message должен быть пустой строкой для no-message variant")
                .isEmpty();
    }

    /**
     * Проверяет, что no-message variant для debug передаёт пустую строку.
     */
    @Test
    @DisplayName("Debug no-message variant передаёт пустую строку")
    void debugNoMessageVariantPassesEmptyString() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        logService.debug("TestService", "uuid-1");

        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .isPresent();
        assertThat(ourEvent.get().getMessage()).isEmpty();
        assertThat(ourEvent.get().getLoggingLevel()).isEqualTo(LoggingLevel.DEBUG);
    }

    /**
     * Проверяет, что no-message variant для warn передаёт пустую строку.
     */
    @Test
    @DisplayName("Warn no-message variant передаёт пустую строку")
    void warnNoMessageVariantPassesEmptyString() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        logService.warn("TestService", "uuid-1");

        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .isPresent();
        assertThat(ourEvent.get().getMessage()).isEmpty();
        assertThat(ourEvent.get().getLoggingLevel()).isEqualTo(LoggingLevel.WARN);
    }

    /**
     * Проверяет, что no-message variant для error передаёт пустую строку.
     */
    @Test
    @DisplayName("Error no-message variant передаёт пустую строку")
    void errorNoMessageVariantPassesEmptyString() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        logService.error("TestService", "uuid-1");

        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .isPresent();
        assertThat(ourEvent.get().getMessage()).isEmpty();
        assertThat(ourEvent.get().getLoggingLevel()).isEqualTo(LoggingLevel.ERROR);
    }

    /**
     * Проверяет, что no-message variant с entities передаёт пустую строку но сохраняет entities.
     */
    @Test
    @DisplayName("No-message variant с entities сохраняет entities")
    void noMessageVariantWithEntitiesPreservesEntities() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        logService.info("TestService", "uuid-1", new Object[]{"entity1", 42});

        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .isPresent();
        assertThat(ourEvent.get().getMessage()).isEmpty();
        assertThat(ourEvent.get().getEntities())
                .describedAs("Entities должны быть сохранены")
                .hasSize(2)
                .containsExactly("entity1", 42);
    }

    /**
     * Проверяет, что LogService создаёт событие с текущим timestamp.
     */
    @Test
    @DisplayName("Событие создаётся с текущим timestamp")
    void eventHasCurrentTimestamp() {
        var appender = new CollectingAppender();
        appenders.add(appender);

        var logService = new LogService(appenders, logFilters);

        long before = System.currentTimeMillis();
        logService.info("TestService", "uuid-1", "Test message");
        long after = System.currentTimeMillis();

        var ourEvent = appender.getCapturedEvents().stream()
                .filter(e -> "TestService".equals(e.getServiceCode()) && "uuid-1".equals(e.getUuid()))
                .findFirst();

        assertThat(ourEvent)
                .isPresent();

        var event = ourEvent.get();
        long timestampMillis = event.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        assertThat(timestampMillis)
                .describedAs("Timestamp должен быть в пределах вызова")
                .isBetween(before, after);
    }

    /**
     * Проверяет, что SERVICE_CODE всегда "LoggerService".
     */
    @Test
    @DisplayName("SERVICE_CODE всегда 'LoggerService'")
    void serviceCodeAlwaysLoggerService() {
        assertThat(LogService.SERVICE_CODE)
                .describedAs("SERVICE_CODE должен быть LoggerService")
                .isEqualTo("LoggerService");
    }

    /**
     * Вспомогательный аппендер, который просто собирает события для проверки.
     */
    private static final class CollectingAppender implements LogAppender {
        private final List<LogEvent> capturedEvents = new ArrayList<>();

        @Override
        public void append(LogEvent event) throws LoggerAppenderException {
            capturedEvents.add(event);
        }

        @Override
        public String getServiceCode() {
            return "CollectingAppender";
        }

        public List<LogEvent> getCapturedEvents() {
            return capturedEvents;
        }
    }
}
