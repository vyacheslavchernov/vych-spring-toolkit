package ru.vych.logger.impl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.vych.logger.impl.common.LoggingLevel;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LogEvent {
    /**
     * Сервис код для определения принадлежности лога к компоненту
     */
    private String serviceCode;

    /**
     * Уникальный идентификатор события лога
     */
    private String uuid;

    private LoggingLevel loggingLevel;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Объекты, которые будут сериализованы и добавлены к сообщению лога
     */
    private List<Object> entities;

    /**
     * @return сообщение лога
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
