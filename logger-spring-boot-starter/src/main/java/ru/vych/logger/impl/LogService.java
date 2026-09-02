package ru.vych.logger.impl;

import org.springframework.stereotype.Service;
import ru.vych.logger.impl.appenders.LogAppender;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.common.LoggingLevel;
import ru.vych.logger.impl.exceptions.LoggerException;

import java.util.List;
import java.util.UUID;

/**
 * Сервис логирования.
 *
 * <p>Обеспечивает централизованную отправку лог-сообщений через конфигурируемые аппендеры
 * с возможностью фильтрации событий логирования.
 *
 * <p>Алгоритм обработки сообщения:
 * <ol>
 *   <li>Создаётся событие {@link LogEvent} из переданных параметров</li>
 *   <li>Для каждого {@link LogAppender} последовательно применяются все {@link LogFilter}
 *       — если хотя бы один фильтр возвращает {@code false}, событие не передаётся в этот аппендер</li>
 *   <li>Прошедшие фильтрацию события передаются во все {@link LogAppender}</li>
 *   <li>При ошибке записи в аппендер ошибка логируется на уровне {@code ERROR}</li>
 * </ol>
 *
 * @see LogAppender
 * @see LogFilter
 * @see LogEvent
 * @see LoggingLevel
 */
@Service
public class LogService {
    /**
     * Код этого сервиса, используется при логировании внутренних сообщений (например,
     * при ошибке записи в аппендер или при инициализации).
     */
    public final static String SERVICE_CODE = "LoggerService";
    private final String uuid = UUID.randomUUID().toString();

    private final List<LogAppender> appenders;
    private final List<LogFilter> logFilters;

    /**
     * Создаёт сервис с указанными аппендерами и фильтрами.
     * При инициализации записывает лог-сообщение {@code INFO} со списком подключённых аппендеров.
     *
     * @param appenders  список аппендеров для записи логов
     * @param logFilters список фильтров для предварительной обработки событий
     */
    public LogService(List<LogAppender> appenders, List<LogFilter> logFilters) {
        this.appenders = appenders;
        this.logFilters = logFilters;
        info(
                SERVICE_CODE, uuid,
                "Инициализирован сервис логирования",
                appenders.stream()
                        .map(Object::getClass)
                        .map(Class::getSimpleName)
                        .toList()
        );
    }

    /**
     * Отправляет лог-сообщение уровня DEBUG.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param message     текст сообщения
     * @param entities    дополнительные данные для логирования
     */
    public void debug(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.DEBUG, message, entities);
    }

    /**
     * Отправляет лог-сообщение уровня DEBUG без текста сообщения.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param entities    дополнительные данные для логирования
     */
    public void debug(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.DEBUG, "", entities);
    }

    /**
     * Отправляет лог-сообщение уровня INFO.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param message     текст сообщения
     * @param entities    дополнительные данные для логирования
     */
    public void info(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.INFO, message, entities);
    }

    /**
     * Отправляет лог-сообщение уровня INFO без текста сообщения.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param entities    дополнительные данные для логирования
     */
    public void info(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.INFO, "", entities);
    }

    /**
     * Отправляет лог-сообщение уровня WARN.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param message     текст сообщения
     * @param entities    дополнительные данные для логирования
     */
    public void warn(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.WARN, message, entities);
    }

    /**
     * Отправляет лог-сообщение уровня WARN без текста сообщения.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param entities    дополнительные данные для логирования
     */
    public void warn(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.WARN, "", entities);
    }

    /**
     * Отправляет лог-сообщение уровня ERROR.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param message     текст сообщения
     * @param entities    дополнительные данные для логирования
     */
    public void error(String serviceCode, String uuid, String message, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.ERROR, message, entities);
    }

    /**
     * Отправляет лог-сообщение уровня ERROR без текста сообщения.
     *
     * @param serviceCode код сервиса, с которого приходит сообщение
     * @param uuid        уникальный идентификатор контекста
     * @param entities    дополнительные данные для логирования
     */
    public void error(String serviceCode, String uuid, Object... entities) {
        log(serviceCode, uuid, LoggingLevel.ERROR, "", entities);
    }

    /**
     * Отправляет лог-сообщение с указанным уровнем логирования.
     * Создаёт событие логирования, применяет фильтры и передаёт событие во все аппендеры.
     * При ошибке записи в аппендер логирует ошибку на уровне ERROR.
     *
     * @param serviceCode     код сервиса, с которого приходит сообщение
     * @param uuid            уникальный идентификатор контекста
     * @param loggingLevel    уровень логирования
     * @param message         текст сообщения
     * @param entities        дополнительные данные для логирования
     */
    public void log(String serviceCode, String uuid, LoggingLevel loggingLevel, String message, Object... entities) {
        appenders.forEach(
                appender -> {
                    var event = LogEvent.create(
                            serviceCode, uuid, loggingLevel,
                            message, entities
                    );

                    for (var filter : logFilters) {
                        if (!filter.filter(event)) {
                            return;
                        }
                    }

                    try {
                        appender.append(event);
                    } catch (LoggerException e) {
                        error(
                                SERVICE_CODE, uuid, "Не удалось отправить лог.",
                                appender.getServiceCode(), event.toString()
                        );
                    }
                }
        );
    }
}
