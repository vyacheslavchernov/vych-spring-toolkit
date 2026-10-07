# Vych Spring Toolkit — Logger

Spring Boot Starter для настройки и использования собственного логгера.

## Назначение

Предоставляет кастомный сервис логирования с поддержкой:

* нескольких appenders (console и расширяемый);
* ANSI-раскраски логов в консоли;
* настраиваемого уровня логирования;
* автоматической регистрации через Spring Boot AutoConfiguration.

## Подключение

Добавьте BOM и зависимость в `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>vych-spring-toolkit-bom</artifactId>
            <version>0.0.5-SNAPSHOT</version>
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
```

### Пример (properties)

```properties
logger.console.enabled=true
logger.console.level=DEBUG
logger.console.include-entities=true
logger.console.pretty-entities=true
logger.console.enable-colors=true
logger.console.dim-entities=true
```

## Кастомные аппендеры

Для добавления собственного аппендера реализуйте интерфейс `LogAppender` и зарегистрируйте его как Spring Bean.

### Шаг 1. Реализация интерфейса

```java
import ru.vych.logger.impl.appenders.LogAppender;
import ru.vych.logger.impl.entities.LogEvent;
import ru.vych.logger.impl.exceptions.LoggerException;

public class FileAppender implements LogAppender {
    @Override
    public void append(LogEvent event) throws LoggerException {
        // Логика записи события в файл
        // ...
    }

    @Override
    public String getServiceCode() {
        return "FileAppender";
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
    public LogAppender fileAppender() {
        return new FileAppender();
    }
}
```

После этого ваш аппендер автоматически будет вызываться вместе с другими при логировании через `LogService`.

> **Примечание:** Если нужно отключить встроенный консольный аппендер, установите `logger.console.enabled=false`.
