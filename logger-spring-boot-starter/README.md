# Vych Spring Toolkit — Logger

Spring Boot Starter для настройки и использования собственного логгера.

## Назначение

Предоставляет кастомный сервис логирования с поддержкой:

* мульти-аппендер пайплайна (console и file);
* ANSI-раскраски логов в консоли;
* настраиваемого уровня логирования;
* фильтрации событий через `LogFilter`;
* JSON-сериализации дополнительных объектов;
* автоматической регистрации через Spring Boot AutoConfiguration.

## Подключение

Добавьте BOM и зависимость в `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>vych-spring-toolkit-bom</artifactId>
            <version>0.0.6-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>logger-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

После подключения стартер автоматически зарегистрирует все необходимые бины. Ничего настраивать дополнительно не нужно.

## Использование

### Базовый пример

```java
import org.springframework.stereotype.Service;
import ru.vych.logger.impl.LogService;

@Service
public class MyService {
    private final LogService log;

    public MyService(LogService log) {
        this.log = log;
    }

    public void doSomething() {
        log.info("MyService", "abc-123", "Начало работы");
        // ...
        log.debug("MyService", "abc-123", "Детали", someObject);
        // ...
        log.error("MyService", "abc-123", "Ошибка", exception);
    }
}
```

### Методы логирования

| Метод | Описание |
|---|---|
| `log.debug(serviceCode, uuid, message, entities...)` | Отладочное сообщение |
| `log.info(serviceCode, uuid, message, entities...)` | Информационное сообщение |
| `log.warn(serviceCode, uuid, message, entities...)` | Предупреждение |
| `log.error(serviceCode, uuid, message, entities...)` | Ошибка |
| `log.log(serviceCode, uuid, level, message, entities...)` | Указанный уровень |

Каждый метод имеет перегрузку без `message` — для логирования только объектов.

### Контекст и идентификаторы

* `serviceCode` — код сервиса (например, имя класса или модуля)
* `uuid` — уникальный идентификатор контекста (trace ID) для отслеживания запроса через все сервисы
* `entities` — дополнительные объекты, которые будут сериализованы в JSON и выведены в лог

## Конфигурация

Стартер автоматически подключается при наличии в зависимостях. Консольный аппендер активен по умолчанию.

Управление через свойства `application.yaml` / `application.properties` с префиксом `logger`.

### Консольный аппендер

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `logger.console.enabled` | `boolean` | `true` | Включить/выключить консольный аппендер |
| `logger.console.level` | `DEBUG, INFO, WARN, ERROR` | `INFO` | Минимальный уровень логирования для вывода |
| `logger.console.include-entities` | `boolean` | `false` | Включать дополнительные объекты в вывод |
| `logger.console.pretty-entities` | `boolean` | `false` | Форматировать вывод объектов с отступами (pretty-print) |
| `logger.console.enable-colors` | `boolean` | `false` | Использовать ANSI-цвета в выводе |
| `logger.console.dim-entities` | `boolean` | `false` | Делать вывод объектов менее ярким |

### Файловый аппендер

Файловый аппендер отключён по умолчанию. Включается свойством `logger.file.enabled=true`.

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `logger.file.enabled` | `boolean` | `false` | Включить/выключить файловый аппендер |
| `logger.file.dir` | `String` | `./logs` | Директория для хранения логов |
| `logger.file.filename-pattern` | `String` | `app-{date}_{timestamp}.log` | Шаблон имени файла (`{date}` и `{timestamp}` заменяются на значения) |
| `logger.file.date-pattern` | `String` | `yyyy-MM-dd` | Паттерн для даты в имени файла (Java `DateTimeFormatter`) |
| `logger.file.encoding` | `String` | `UTF-8` | Кодировка файла |
| `logger.file.level` | `DEBUG, INFO, WARN, ERROR` | `INFO` | Минимальный уровень логирования |
| `logger.file.include-entities` | `boolean` | `false` | Включать дополнительные объекты в вывод |
| `logger.file.pretty-entities` | `boolean` | `false` | Форматировать вывод объектов с отступами (pretty-print) |
| `logger.file.buffer-size` | `int` | `8192` | Размер буфера записи (в байтах) |
| `logger.file.log-formatter` | `String` | `%date     %level     %serviceCode : %message %entity` | Шаблон строки лога. Поддерживаются: `%date`, `%level`, `%serviceCode`, `%message`, `%entity` |

### Пример (YAML)

```yaml
logger:
  console:
    enabled: true
    level: DEBUG
    include-entities: true
    pretty-entities: true
    enable-colors: true
    dim-entities: true
  file:
    enabled: true
    dir: ./logs
    filename-pattern: app-{date}_{timestamp}.log
    date-pattern: yyyy-MM-dd
    encoding: UTF-8
    level: INFO
    include-entities: true
    pretty-entities: true
    buffer-size: 8192
    log-formatter: "%date [%level] %serviceCode: %message %entity"
```

### Пример (properties)

```properties
logger.console.enabled=true
logger.console.level=DEBUG
logger.console.include-entities=true
logger.console.pretty-entities=true
logger.console.enable-colors=true
logger.console.dim-entities=true

logger.file.enabled=true
logger.file.dir=./logs
logger.file.filename-pattern=app-{date}_{timestamp}.log
logger.file.date-pattern=yyyy-MM-dd
logger.file.encoding=UTF-8
logger.file.level=INFO
logger.file.include-entities=true
logger.file.pretty-entities=true
logger.file.buffer-size=8192
logger.file.log-formatter=%date [%level] %serviceCode: %message %entity
```

## Кастомные аппендеры

Для добавления собственного аппендера реализуйте интерфейс `LogAppender` и зарегистрируйте его как Spring Bean.

### Шаг 1. Реализация интерфейса

```java
import ru.vych.logger.impl.appenders.LogAppender;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerException;

public class CustomAppender implements LogAppender {
    @Override
    public void append(LogEvent event) throws LoggerException {
        // Логика записи события
        // event.getServiceCode(), event.getUuid(), event.getLoggingLevel(),
        // event.getMessage(), event.getEntities()
    }

    @Override
    public String getServiceCode() {
        return "CustomAppender";
    }
}
```

### Шаг 2. Регистрация как Spring Bean

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {
    @Bean
    public LogAppender customAppender() {
        return new CustomAppender();
    }
}
```

После этого ваш аппендер автоматически будет вызываться вместе с другими при логировании через `LogService`.

## Кастомные фильтры

Для фильтрации событий логирования реализуйте интерфейс `LogFilter` и зарегистрируйте как Spring Bean. Фильтры применяются последовательно для каждого аппендера — если хотя бы один фильтр возвращает `false`, событие не передаётся в этот аппендер.

### Шаг 1. Реализация интерфейса

```java
import org.springframework.stereotype.Component;
import ru.vych.logger.impl.LogFilter;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.common.LoggingLevel;

@Component
public class SensitiveDataFilter implements LogFilter {
    @Override
    public boolean filter(LogEvent logEvent) {
        // Пример: блокировать события уровня DEBUG
        if (logEvent.getLoggingLevel() == LoggingLevel.DEBUG) {
            return false;
        }
        // Пример: фильтровать по serviceCode
        if (logEvent.getServiceCode().startsWith("test-")) {
            return false;
        }
        return true;
    }
}
```

### Шаг 2. Регистрация как Spring Bean

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {
    @Bean
    public LogFilter sensitiveDataFilter() {
        return event -> {
            // Ваша логика фильтрации
            return true;
        };
    }
}
```

Или используйте `@Component` аннотацию на классе-реализации (как в примере выше).

> **Примечание:** Если нужно отключить встроенный консольный аппендер, установите `logger.console.enabled=false`.
