---
last_updated: 2026-10-08
scope: package
---

# ru.vych.logger.impl.exceptions

Checked-исключения логгера.

## Иерархия исключений

```
java.lang.Exception
  └── LoggerException (корневое checked-исключение)
        └── LoggerAppenderException
```

## Публичные классы

### `LoggerException`

Корневое checked-исключение для всего модуля логирования.

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

**Особенности:** все реализации `LogAppender.append()` обязаны его декларировать.

### `LoggerAppenderException`

Специализированное checked-исключение для ошибок аппендеров.

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

**Использование:** `ConsoleAppender.writeEntities()` при ошибке JSON-сериализации.

## Зависимости

- `LoggerException` — нет (кроме `java.lang.Exception`)
- `LoggerAppenderException` → `LoggerException`

## Связанные пакеты

- [`impl`](./impl.md) — `LogService` ловит `LoggerException` при вызове аппендеров
- [`impl/appenders`](./impl/appenders.md) — `ConsoleAppender` бросает `LoggerAppenderException`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
