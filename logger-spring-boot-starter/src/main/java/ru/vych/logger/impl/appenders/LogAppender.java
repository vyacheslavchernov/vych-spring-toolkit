package ru.vych.logger.impl.appenders;

import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerException;

/**
 * Интерфейс обработчика событий лога.
 * Принимает событие, создаёт из него сообщение.
 * Выводит сообщение или записывает его в хранилище.
 */
public interface LogAppender {
    /**
     * Добавить событие в хранилище логов
     *
     * @param event событие
     * @throws LoggerException в случае, если при обработке события произошла ошибка
     */
    void append(LogEvent event) throws LoggerException;

    /**
     * @return сервис код обработчика событий лога
     */
    String getServiceCode();
}
