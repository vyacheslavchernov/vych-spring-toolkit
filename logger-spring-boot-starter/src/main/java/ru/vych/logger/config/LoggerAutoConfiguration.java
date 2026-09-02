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
 * Автоконфиг для модуля логирования
 */
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
public class LoggerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LogService logService(List<LogAppender> appenders, List<LogFilter> logFilters) {
        return new LogService(appenders, logFilters);
    }

    /**
     * Бин для записи логов в терминал.
     * Поставляется с модулем и по умолчанию всегда активен.
     *
     * @param properties настройки из application.yaml
     * @return
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
