package ru.vych.logger.impl;

import ru.vych.logger.impl.entities.LogEvent;

/**
 * Интерфейс фильтра сообщений лога.
 * Все фильтры последовательно вызываются до того, как событие будет
 * передано на обработку к зарегистрированным {@link ru.vych.logger.impl.appenders.LogAppender}.
 * Вызывается для каждого {@link ru.vych.logger.impl.appenders.LogAppender} отдельно.
 */
public interface LogFilter {
    /**
     * @param logEvent событие лога для предварительной обработки
     * @return {@code true}, если это сообщение следует логировать, иначе {@code false}
     */
    boolean filter(LogEvent logEvent);
}
