package ru.vych.logger.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.vych.logger.impl.LogFilter;
import ru.vych.logger.impl.LogService;
import ru.vych.logger.impl.appenders.ConsoleAppender;
import ru.vych.logger.impl.appenders.LogAppender;

import java.util.List;

/**
 * Автоматическая конфигурация модуля логирования Spring Boot.
 *
 * <p>Регистрирует основные бины: {@link ru.vych.logger.impl.LogService} и
 * {@code ConsoleAppender} (при условии включённой конфигурации).
 *
 * @see LogProperties
 * @see ru.vych.logger.impl.LogService
 * @see ru.vych.logger.impl.appenders.ConsoleAppender
 */
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
public class LoggerAutoConfiguration {
    /**
     * Создаёт главный сервис логирования.
     *
     * @param appenders   список всех зарегистрированных аппендеров
     * @param logFilters  список всех зарегистрированных фильтров
     * @return экземпляр {@link ru.vych.logger.impl.LogService}
     */
    @Bean
    @ConditionalOnMissingBean
    public LogService logService(List<LogAppender> appenders, List<LogFilter> logFilters) {
        return new LogService(appenders, logFilters);
    }

    /**
     * Создаёт аппендер для записи логов в консоль (терминал).
     *
     * <p>Активирован по умолчанию (при отсутствии конфигурации).
     * Управление через свойство {@code logger.console.enabled}.
     *
     * @param properties настройки из application.yaml
     * @return экземпляр {@link ConsoleAppender}
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "logger.console",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public LogAppender consoleAppender(LogProperties properties) {
        return new ConsoleAppender(
                properties.getConsole().getLevel(),
                properties.getConsole().isIncludeEntities(),
                properties.getConsole().isPrettyEntities(),
                properties.getConsole().isEnableColors(),
                properties.getConsole().isDimEntities()
        );
    }
}
