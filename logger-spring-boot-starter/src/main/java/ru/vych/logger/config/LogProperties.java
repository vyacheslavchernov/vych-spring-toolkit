package ru.vych.logger.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.vych.logger.impl.common.LoggingLevel;


@Getter
@Setter
@ConfigurationProperties(prefix = "logger")
public class LogProperties {
    private Console console = new Console();

    @Getter
    @Setter
    public static class Console {
        private boolean enabled = true;
        private LoggingLevel level = LoggingLevel.INFO;
        private boolean includeEntities = false;
        private boolean prettyEntities = false;
        private boolean enableColors = false;
        private boolean dimEntities = false;
    }
}
