---
last_updated: 2026-10-08
scope: module
---

# Logger Starter

Spring Boot starter для кастомного консольного логгера с мульти-аппендером, фильтрами и ANSI-цветами.

## Назначение

Предоставляет альтернативу SLF4J/Logback с поддержкой multi-appender pipeline, level-based фильтрации, JSON-сериализации доп. объектов и ANSI-цветов для консоли.

## Пакеты

- [`config`](../packages/logger/config.md) — автоконфигурация Spring, `LogProperties` (`@ConfigurationProperties(prefix = "logger")`)
- [`impl`](../packages/logger/impl.md) — `LogService` (@Service), `LogFilter` (функциональный интерфейс)
- [`impl/appenders`](../packages/logger/impl/appenders.md) — `LogAppender` (интерфейс), `ConsoleAppender` (built-in)
- [`impl/common`](../packages/logger/impl/common.md) — `AnsiColor` (escape codes), `LoggingLevel` (enum), `ObjectMapperUtils` (Jackson utilities)
- [`impl/entities`](../packages/logger/impl/entities.md) — `LogEvent` (serviceCode, uuid, level, message, timestamp, entities)
- [`impl/exceptions`](../packages/logger/impl/exceptions.md) — `LoggerException` (root checked), `LoggerAppenderException`

## Зависимости

- **Compile**: `spring-boot-starter`, `lombok`, `jackson-databind`, `jackson-datatype-jsr310`
- **Test**: нет (unit-тесты отсутствуют)

## Тесты

Unit-тесты для logger-модуля не реализованы. Логика проверяется косвенно через интеграционные тесты в `vych-spring-toolkit-tests`.

## Кросс-ссылки

- [modules/README.md](README.md) — обзор модулей
- [packages/logger/config.md](../packages/logger/config.md) — документация пакета config
- [packages/logger/impl.md](../packages/logger/impl.md) — документация пакета impl
