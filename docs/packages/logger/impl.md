---
last_updated: 2026-10-08
scope: package
---

# ru.vych.logger.impl

Центральный сервис логирования и фильтр.

## Публичные классы

### `LogService` (@Service)

Центральный сервис логирования: создаёт события, применяет фильтры, делегирует запись аппендерам.

**Ключевые методы:** `debug/info/warn/error(String serviceCode, String uuid, String message, Object... entities)` — 5 перегрузок для разных уровней. `log(String serviceCode, String uuid, LoggingLevel, String message, Object... entities)` — универсальный метод, реализующий алгоритм: create `LogEvent` → filter → append → error handling.

**Поля:** `SERVICE_CODE = "LoggerService"`, `uuid` (instance ID).

**Lombok:** не используется.

**Особенности:** конструктор автоматически логирует INFO о своей инициализации. Ошибка в одном аппендере не блокирует другие (`forEach` с `try-catch`). Рекурсия при ошибке аппендера (логирование через тот же uuid).

### `LogFilter` (interface)

Интерфейс фильтрации событий логирования. Применяется последовательно к каждому событию перед передачей в аппендер.

**Метод:** `boolean filter(LogEvent logEvent)` — `true` = пропустить, `false` = не пропустить.

**Особенности:** фильтры применяются **перпендикулярно** каждому аппендеру. Позволяет реализовывать cross-cutting логику (фильтрация по serviceCode, uuid, уровню).

## Зависимости

- `LogService` → `LogAppender`, `LogFilter`, `LogEvent`, `LoggingLevel`, `LoggerException`
- `LogFilter` → `LogEvent`

## Связанные пакеты

- [`config`](./config.md) — `LoggerAutoConfiguration` создаёт `LogService`
- [`impl/appenders`](./impl/appenders.md) — `LogAppender`, `ConsoleAppender`
- [`impl/common`](./impl/common.md) — `LoggingLevel`
- [`impl/entities`](./impl/entities.md) — `LogEvent`
- [`impl/exceptions`](./impl/exceptions.md) — `LoggerException`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/config.md](./config.md) — документация пакета config
