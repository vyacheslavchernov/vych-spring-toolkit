package ru.vych.logger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.vych.logger.impl.common.LoggingLevel;


/**
 * Конфигурационные свойства модуля логирования.
 *
 * <p>Привязывается к свойствам с префиксом {@code logger} из application.yaml / application.properties.
 *
 * @see LogProperties.Console
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "logger")
public class LogProperties {
    /**
     * Настройки консольного аппендера.
     */
    private Console console = new Console();

    /**
     * Настройки консольного аппендера.
     *
     * @see LoggingLevel
     */
    @Getter
    @Setter
    public static class Console {
        /**
         * Включён ли консольный аппендер. По умолчанию — {@code true}.
         */
        private boolean enabled = true;

        /**
         * Минимальный уровень логирования для консоли.
         * По умолчанию — {@link LoggingLevel#INFO}.
         */
        private LoggingLevel level = LoggingLevel.INFO;

        /**
         * Включать ли дополнительные объекты (entities) в вывод.
         */
        private boolean includeEntities = false;

        /**
         * Форматировать ли JSON объектов с отступами (pretty-print).
         */
        private boolean prettyEntities = false;

        /**
         * Использовать ли ANSI-цвета в выводе.
         */
        private boolean enableColors = false;

        /**
         * Делать ли вывод объектов менее ярким (ANSI dim).
         */
        private boolean dimEntities = false;
    }
}
