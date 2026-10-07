package ru.vych.http.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.exceptions.HttpClientConfigurationException;
import ru.vych.logger.impl.LogService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.CREATION_ERROR_CONFIGURATION_IS_NULL;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.CREATION_ERROR_LOG_SERVICE_IS_NULL;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpClientLogger")
class HttpClientLoggerTests {

    private static final String SERVICE_CODE = "test-service";
    private static final String REQUEST_ID = "request-1";
    private static final String MESSAGE = "Test message";

    @Mock
    private LogService logService;

    private HttpClientConfig config;

    /**
     * Поставщик аргументов для параметризованных тестов {@code info()} и {@code debug()} с флагом {@code forced}:
     * комбинации значений {@code logRequests} (true/false) и {@code forced} (true/false)
     * с ожидаемым результатом (вызов или отсутствие вызова logService).
     */
    private static Stream<Arguments> logWithForcedArgsProvider() {
        return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, true),
                Arguments.of(false, true, true),
                Arguments.of(false, false, false)
        );
    }

    /**
     * Создаёт конфигурацию с включённым логированием запросов.
     */
    @BeforeEach
    void setUp() {
        config = new HttpClientConfig(SERVICE_CODE)
                .setLogRequests(true);
    }

    /**
     * Проверяет, что {@code info()} логирует когда {@code logRequests=true}.
     */
    @Test
    @DisplayName("info() логирует когда logRequests=true")
    public void infoWithLogRequestsEnabledShouldCallLogService() throws HttpClientConfigurationException {
        config.setLogRequests(true);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        verify(logService).info(any(), any(), eq(MESSAGE), any());
    }

    /**
     * Проверяет, что {@code info()} не логирует когда {@code logRequests=false}.
     */
    @Test
    @DisplayName("info() не логирует когда logRequests=false")
    public void infoWithLogRequestsDisabledShouldNotCallLogService() throws HttpClientConfigurationException {
        config.setLogRequests(false);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        verify(logService, never()).info(any(), any(), any(), any());
    }

    /**
     * Проверяет поведение {@code info()} с флагом {@code forced} для различных
     * комбинаций {@code logRequests} и {@code forced}:
     * логирование происходит когда {@code logRequests=true} или {@code forced=true}.
     */
    @ParameterizedTest
    @MethodSource("logWithForcedArgsProvider")
    @DisplayName("info() с флагом forced")
    public void infoWithForced(boolean logRequests, boolean forced, boolean shouldCallLogService) throws HttpClientConfigurationException {
        config.setLogRequests(logRequests);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(forced, SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        if (shouldCallLogService) {
            verify(logService).info(any(), any(), eq(MESSAGE), any());
        } else {
            verify(logService, never()).info(any(), any(), any(), any());
        }
    }

    /**
     * Проверяет, что {@code debug()} логирует когда {@code logRequests=true}.
     */
    @Test
    @DisplayName("debug() логирует когда logRequests=true")
    public void debugWithLogRequestsEnabledShouldCallLogService() throws HttpClientConfigurationException {
        config.setLogRequests(true);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        verify(logService).debug(any(), any(), eq(MESSAGE), any());
    }

    /**
     * Проверяет, что {@code debug()} не логирует когда {@code logRequests=false}.
     */
    @Test
    @DisplayName("debug() не логирует когда logRequests=false")
    public void debugWithLogRequestsDisabledShouldNotCallLogService() throws HttpClientConfigurationException {
        config.setLogRequests(false);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        verify(logService, never()).debug(any(), any(), any(), any());
    }

    /**
     * Проверяет поведение {@code debug()} с флагом {@code forced} для различных
     * комбинаций {@code logRequests} и {@code forced}:
     * логирование происходит когда {@code logRequests=true} или {@code forced=true}.
     */
    @ParameterizedTest
    @MethodSource("logWithForcedArgsProvider")
    @DisplayName("debug() с флагом forced")
    public void debugWithForced(boolean logRequests, boolean forced, boolean shouldCallLogService) throws HttpClientConfigurationException {
        config.setLogRequests(logRequests);
        var httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug(forced, SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        if (shouldCallLogService) {
            verify(logService).debug(any(), any(), eq(MESSAGE), any());
        } else {
            verify(logService, never()).debug(any(), any(), any(), any());
        }
    }

    /**
     * Проверяет, что {@code error()} всегда логирует независимо от {@code logRequests}.
     */
    @Test
    @DisplayName("error() всегда логирует независимо от logRequests")
    public void errorAlwaysLogsRegardlessOfConfig() throws HttpClientConfigurationException {
        var configWithLogging = new HttpClientConfig(SERVICE_CODE).setLogRequests(true);
        var configWithoutLogging = new HttpClientConfig(SERVICE_CODE).setLogRequests(false);

        var loggerWithLogging = new HttpClientLogger(configWithLogging, logService);
        var loggerWithoutLogging = new HttpClientLogger(configWithoutLogging, logService);

        loggerWithLogging.error(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);
        loggerWithoutLogging.error(SERVICE_CODE, REQUEST_ID, MESSAGE, (Object[]) null);

        verify(logService, times(2)).error(any(), any(), eq(MESSAGE), any());
    }

    /**
     * Проверяет, что конструктор выбрасывает {@code HttpClientConfigurationException}
     * при передаче {@code null} в качестве конфигурации.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null config")
    public void constructorWithNullConfigShouldThrow() {
        assertThrows(HttpClientConfigurationException.class, () ->
                        new HttpClientLogger(null, logService),
                CREATION_ERROR_CONFIGURATION_IS_NULL
        );
    }

    /**
     * Проверяет, что конструктор выбрасывает {@code HttpClientConfigurationException}
     * при передаче {@code null} в качестве {@code LogService}.
     */
    @Test
    @DisplayName("Конструктор выбрасывает при null logService")
    public void constructorWithNullLogServiceShouldThrow() {
        assertThrows(HttpClientConfigurationException.class, () ->
                        new HttpClientLogger(config, null),
                CREATION_ERROR_LOG_SERVICE_IS_NULL
        );
    }
}
