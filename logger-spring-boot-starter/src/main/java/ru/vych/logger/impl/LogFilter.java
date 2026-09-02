package ru.vych.logger.impl;

import ru.vych.logger.impl.entities.LogEvent;

/**
 * Интерфейс фильтра событий логирования.
 *
 * <p>Фильтры применяются последовательно к событию перед тем, как оно будет
 * передано в {@link ru.vych.logger.impl.appenders.LogAppender}.
 * Каждый фильтр вызывается отдельно для каждого аппендера.
 *
 * <p>Если хотя бы один фильтр возвращает {@code false}, событие не передаётся
 * в соответствующий аппендер.
 *
 * @see ru.vych.logger.impl.LogService
 * @see ru.vych.logger.impl.appenders.LogAppender
 */
public interface LogFilter {
    /**
     * Проверяет, следует ли логировать данное событие.
     *
     * @param logEvent событие логирования для проверки
     * @return {@code true}, если событие должно быть логировано,
     *         {@code false} — если событие нужно пропустить
     */
    boolean filter(LogEvent logEvent);
}
