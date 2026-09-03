package ru.vych.http.impl;

import ru.vych.http.config.HttpClientConfig;
import ru.vych.logger.impl.LogService;

/**
 * Обёртка над {@link LogService}, которая контролирует логирование запросов и ответов
 * на основе флага {@link HttpClientConfig#isLogRequests()}.
 * <p>
 * Если логирование включено в конфиге, методы {@code info} и {@code debug} пишут логи всегда.
 * Если выключено — логи игнорируются, кроме случаев,
 * когда метод вызван с {@code forced = true} (например, лог инициализации клиента).
 * Метод {@code error} всегда пишет логи независимо от конфига.
 * </p>
 *
 * @see HttpClientConfig#isLogRequests()
 * @see LogService
 */
public class HttpClientLogger {
    private final HttpClientConfig config;
    private final LogService logService;

    /**
     * Создаёт обёртку над {@link LogService} с заданной конфигурацией.
     *
     * @param config     конфигурация HTTP-клиента; не должен быть {@code null}
     * @param logService сервис логирования; не должен быть {@code null}
     */
    public HttpClientLogger(HttpClientConfig config, LogService logService) {
        this.config = config;
        this.logService = logService;
    }

    /**
     * Записывает сообщение уровня info.
     * <p>
     * Если логирование запросов включено в конфиге, сообщение пишется всегда.
     * Если логирование выключено, сообщение пишется только при {@code forced = true}.
     * </p>
     *
     * @param forced      если {@code true}, сообщение пишется независимо от конфига
     * @param serviceCode код сервиса
     * @param requestId   идентификатор запроса
     * @param message     сообщение лога
     * @param entities    дополнительные объекты для логирования
     */
    public void info(boolean forced, String serviceCode, String requestId, String message, Object... entities) {
        if (enabled() || forced) {
            logService.info(serviceCode, requestId, message, entities);
        }
    }

    /**
     * Записывает сообщение уровня info.
     * <p>
     * Сообщение пишется только если логирование запросов включено в конфиге.
     * </p>
     *
     * @param serviceCode код сервиса
     * @param requestId   идентификатор запроса
     * @param message     сообщение лога
     * @param entities    дополнительные объекты для логирования
     */
    public void info(String serviceCode, String requestId, String message, Object... entities) {
        info(false, serviceCode, requestId, message, entities);
    }

    /**
     * Записывает сообщение уровня debug.
     * <p>
     * Если логирование запросов включено в конфиге, сообщение пишется всегда.
     * Если логирование выключено, сообщение пишется только при {@code forced = true}.
     * </p>
     *
     * @param forced      если {@code true}, сообщение пишется независимо от конфига
     * @param serviceCode код сервиса
     * @param requestId   идентификатор запроса
     * @param message     сообщение лога
     * @param entities    дополнительные объекты для логирования
     */
    public void debug(boolean forced, String serviceCode, String requestId, String message, Object... entities) {
        if (enabled() || forced) {
            logService.debug(serviceCode, requestId, message, entities);
        }
    }

    /**
     * Записывает сообщение уровня debug.
     * <p>
     * Сообщение пишется только если логирование запросов включено в конфиге.
     * </p>
     *
     * @param serviceCode код сервиса
     * @param requestId   идентификатор запроса
     * @param message     сообщение лога
     * @param entities    дополнительные объекты для логирования
     */
    public void debug(String serviceCode, String requestId, String message, Object... entities) {
        debug(false, serviceCode, requestId, message, entities);
    }

    /**
     * Записывает сообщение уровня error.
     * <p>
     * Сообщение пишется всегда, независимо от конфига логирования.
     * </p>
     *
     * @param serviceCode код сервиса
     * @param requestId   идентификатор запроса
     * @param message     сообщение лога
     * @param entities    дополнительные объекты для логирования
     */
    public void error(String serviceCode, String requestId, String message, Object... entities) {
        logService.error(serviceCode, requestId, message, entities);
    }

    private boolean enabled() {
        return config.isLogRequests();
    }
}
