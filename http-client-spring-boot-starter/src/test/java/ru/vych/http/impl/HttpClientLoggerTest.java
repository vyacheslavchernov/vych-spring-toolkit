package ru.vych.http.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.logger.impl.LogService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpClientLogger")
class HttpClientLoggerTest {

    @Mock
    private LogService logService;

    @Captor
    private ArgumentCaptor<String> serviceCodeCaptor;

    @Captor
    private ArgumentCaptor<String> requestIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private HttpClientLogger httpClientLogger;

    @BeforeEach
    void setUp() {
        reset(logService);
    }

    @Test
    @DisplayName("info() — логирует когда logRequests=true")
    void infoWithLogRequestsEnabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info("test-service", "request-1", "Test message", "entity1");

        verify(logService).info(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("test-service", serviceCodeCaptor.getValue());
        assertEqual("request-1", requestIdCaptor.getValue());
        assertEqual("Test message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("info() — не логирует когда logRequests=false")
    void infoWithLogRequestsDisabledShouldNotCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info("test-service", "request-1", "Test message", "entity1");

        verify(logService, never()).info(any(), any(), any(), any());
    }

    @Test
    @DisplayName("info(forced=true) — логирует даже при logRequests=false")
    void infoWithForcedTrueAndLogRequestsDisabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(true, "test-service", "request-1", "Forced message", "entity1");

        verify(logService).info(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("Forced message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("info(forced=false) — не логирует при logRequests=false")
    void infoWithForcedFalseAndLogRequestsDisabledShouldNotCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(false, "test-service", "request-1", "Test message", "entity1");

        verify(logService, never()).info(any(), any(), any(), any());
    }

    @Test
    @DisplayName("info(forced=true, logRequests=true) — логирует")
    void infoWithForcedTrueAndLogRequestsEnabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.info(true, "test-service", "request-1", "Forced message", "entity1");

        verify(logService).info(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("Forced message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("debug() — логирует когда logRequests=true")
    void debugWithLogRequestsEnabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug("test-service", "request-1", "Debug message", "entity1");

        verify(logService).debug(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("test-service", serviceCodeCaptor.getValue());
        assertEqual("request-1", requestIdCaptor.getValue());
        assertEqual("Debug message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("debug() — не логирует когда logRequests=false")
    void debugWithLogRequestsDisabledShouldNotCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug("test-service", "request-1", "Debug message", "entity1");

        verify(logService, never()).debug(any(), any(), any(), any());
    }

    @Test
    @DisplayName("debug(forced=true) — логирует даже при logRequests=false")
    void debugWithForcedTrueAndLogRequestsDisabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug(true, "test-service", "request-1", "Forced debug", "entity1");

        verify(logService).debug(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("Forced debug", messageCaptor.getValue());
    }

    @Test
    @DisplayName("debug(forced=false) — не логирует при logRequests=false")
    void debugWithForcedFalseAndLogRequestsDisabledShouldNotCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.debug(false, "test-service", "request-1", "Test message", "entity1");

        verify(logService, never()).debug(any(), any(), any(), any());
    }

    @Test
    @DisplayName("error() — логирует когда logRequests=true")
    void errorWithLogRequestsEnabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.error("test-service", "request-1", "Error message", "entity1");

        verify(logService).error(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("test-service", serviceCodeCaptor.getValue());
        assertEqual("request-1", requestIdCaptor.getValue());
        assertEqual("Error message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("error() — логирует когда logRequests=false")
    void errorWithLogRequestsDisabledShouldCallLogService() {
        HttpClientConfig config = new HttpClientConfig("test-service")
                .setLogRequests(false);
        httpClientLogger = new HttpClientLogger(config, logService);

        httpClientLogger.error("test-service", "request-1", "Error message", "entity1");

        // error() always logs, regardless of logRequests flag
        verify(logService).error(serviceCodeCaptor.capture(), requestIdCaptor.capture(),
                messageCaptor.capture(), any());
        assertEqual("Error message", messageCaptor.getValue());
    }

    @Test
    @DisplayName("error() — всегда логирует независимо от logRequests")
    void errorAlwaysLogsRegardlessOfConfig() {
        HttpClientConfig configWithLogging = new HttpClientConfig("test-service")
                .setLogRequests(true);
        HttpClientLogger loggerWithLogging = new HttpClientLogger(configWithLogging, logService);

        HttpClientConfig configWithoutLogging = new HttpClientConfig("test-service")
                .setLogRequests(false);
        HttpClientLogger loggerWithoutLogging = new HttpClientLogger(configWithoutLogging, logService);

        loggerWithLogging.error("test-service", "req-1", "Error");
        loggerWithoutLogging.error("test-service", "req-2", "Error");

        // Both should call logService.error
        verify(logService, times(2)).error(any(), any(), eq("Error"));
    }

    @Test
    @DisplayName("constructor — выбрасывает NPE при null config")
    void constructorWithNullConfigShouldThrowNullPointerException() {
        assertDoesNotThrow(() -> {
            try {
                new HttpClientLogger(null, logService);
            } catch (NullPointerException e) {
                throw e;
            }
        });
    }

    @Test
    @DisplayName("constructor — выбрасывает NPE при null logService")
    void constructorWithNullLogServiceShouldThrowNullPointerException() {
        HttpClientConfig config = new HttpClientConfig("test-service");
        assertDoesNotThrow(() -> {
            try {
                new HttpClientLogger(config, null);
            } catch (NullPointerException e) {
                throw e;
            }
        });
    }

    @Test
    @DisplayName("info() — не выбрасывает при null message")
    void infoWithNullMessageShouldNotThrow() {
        HttpClientConfig config = new HttpClientConfig("test-service");
        config.setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        assertDoesNotThrow(() ->
                httpClientLogger.info("test-service", "request-1", null, (Object[]) null)
        );
    }

    @Test
    @DisplayName("debug() — не выбрасывает при null message")
    void debugWithNullMessageShouldNotThrow() {
        HttpClientConfig config = new HttpClientConfig("test-service");
        config.setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        assertDoesNotThrow(() ->
                httpClientLogger.debug("test-service", "request-1", null, (Object[]) null)
        );
    }

    @Test
    @DisplayName("error() — не выбрасывает при null message")
    void errorWithNullMessageShouldNotThrow() {
        HttpClientConfig config = new HttpClientConfig("test-service");
        config.setLogRequests(true);
        httpClientLogger = new HttpClientLogger(config, logService);

        assertDoesNotThrow(() ->
                httpClientLogger.error("test-service", "request-1", null, (Object[]) null)
        );
    }

    private void assertEqual(String expected, String actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError("Expected null but was: " + actual);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + ", but was: " + actual);
        }
    }
}
