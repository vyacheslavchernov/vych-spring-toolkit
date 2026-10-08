package ru.vych.logger.config;

import lombok.RequiredArgsConstructor;
import ru.vych.logger.impl.appenders.FileAppender;
import ru.vych.logger.impl.exceptions.LoggerAppenderException;

/**
 * Провайдер для создания и инициализации {@link FileAppender}.
 *
 * <p>Создаёт экземпляр аппендера на основе конфигурации и вызывает метод {@code init()}
 * для подготовки файловой системы к записи.
 *
 * @see FileAppender
 * @see FileAppenderProperties
 */
@RequiredArgsConstructor
public class FileAppenderProvider {
    /**
     * Конфигурационные свойства файлового аппендера.
     */
    private final FileAppenderProperties properties;

    /**
     * Создаёт и инициализирует новый экземпляр {@link FileAppender}.
     *
     * @return настроенный и инициализированный аппендер
     * @throws LoggerAppenderException в случае ошибки инициализации
     */
    public FileAppender create() throws LoggerAppenderException {
        var appender = new FileAppender(properties);
        appender.init();
        return appender;
    }
}
