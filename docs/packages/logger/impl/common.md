---
last_updated: 2026-10-08
scope: package
---

# ru.vych.logger.impl.common

Утилиты логгера: ANSI-цвета, уровни логирования, Jackson utilities.

## Публичные классы

### `AnsiColor` (final class)

Хранилище констант ANSI-кодов цветного вывода.

**Поля:** `RESET`, `BLACK`, `RED`, `GREEN`, `YELLOW`, `BLUE`, `PURPLE`, `CYAN`, `WHITE` — все `static final String`.

**Паттерн:** constant class (final, package-private constructor).

### `LoggingLevel` (enum)

Перечисление уровней логирования с числовыми значениями для сравнения.

| Константа | Значение |
|---|---|
| `DEBUG` | 1 |
| `INFO` | 2 |
| `WARN` | 3 |
| `ERROR` | 4 |

**Метод:** `getValue()` — возвращает числовое значение.

**Lombok:** `@Getter`.

**Особенности:** числовые значения позволяют использовать `<`/`>` для фильтрации по уровню.

### `ObjectMapperUtils` (final class)

Утилитарный класс для JSON-сериализации с преднастроенным `ObjectMapper`.

**Методы:**
- `toJson(Object target)` — сериализует в JSON-строку.
- `toPrettyJson(Object target)` — сериализует в отформатированную JSON-строку.

**Lombok:** не используется.

**Особенности:** `private final static ObjectMapper` с `JavaTimeModule` и отключённым `WRITE_DATES_AS_TIMESTAMPS`. **Не thread-safe** при высокой конкурентности.

## Зависимости

- `AnsiColor` — нет
- `LoggingLevel` — нет
- `ObjectMapperUtils` — Jackson (`ObjectMapper`, `JavaTimeModule`)

## Связанные пакеты

- [`impl`](./impl.md) — `LogService`
- [`impl/appenders`](./impl/appenders.md) — `ConsoleAppender` использует все три класса

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
