package ru.vych.logger.impl.appenders;

import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerException;

/**
 * Интерфейс обработчика (аппендера) событий логирования.
 *
 * <p>Реализации этого интерфейса определяют способ вывода лог-сообщений:
 * в консоль, файл, внешнюю систему и т. д.
 *
 * @see ru.vych.logger.impl.LogService
 * @see ru.vych.logger.impl.appenders.ConsoleAppender
 */
public interface LogAppender {
    /**
     * Добавляет событие логирования в хранилище или вывод.
     *
     * @param event событие логирования для обработки
     * @throws LoggerException в случае ошибки при обработке события
     */
    void append(LogEvent event) throws LoggerException;

    /**
     * Возвращает код (идентификатор) данного аппендера.
     *
     * @return строковый код аппендера
     */
    String getServiceCode();
}
