package ru.vych.logger.impl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.vych.logger.impl.common.LoggingLevel;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Событие логирования — неизменяемый контейнер для данных лога.
 *
 * <p>Содержит:
 * <ul>
 *   <li>Код сервиса-отправителя</li>
 *   <li>UUID контекста</li>
 *   <li>Уровень логирования</li>
 *   <li>Текст сообщения</li>
 *   <li>Временную метку</li>
 *   <li>Дополнительные объекты (entities)</li>
 * </ul>
 *
 * @see ru.vych.logger.impl.LogService
 * @see LoggingLevel
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class LogEvent {
    /**
     * Код сервиса, с которого пришло событие логирования.
     */
    private String serviceCode;

    /**
     * Уникальный идентификатор контекста (trace ID).
     */
    private String uuid;

    /**
     * Уровень логирования события.
     */
    private LoggingLevel loggingLevel;

    /**
     * Текст сообщения.
     */
    private String message;

    /**
     * Временная метка создания события.
     */
    private LocalDateTime timestamp;

    /**
     * Дополнительные объекты, сериализуемые в JSON и добавляемые к сообщению.
     */
    private List<Object> entities;

    /**
     * Создаёт новое событие логирования.
     *
     * @param serviceCode код сервиса-отправителя
     * @param uuid        уникальный идентификатор контекста
     * @param loggingLevel уровень логирования
     * @param message     текст сообщения
     * @param entities    дополнительные объекты
     * @return новый экземпляр {@link LogEvent}
     */
    public static LogEvent create(String serviceCode, String uuid, LoggingLevel loggingLevel, String message, Object... entities) {
        return new LogEvent(
                serviceCode,
                uuid,
                loggingLevel,
                message,
                LocalDateTime.now(),
                Arrays.asList(entities)
        );
    }
}
