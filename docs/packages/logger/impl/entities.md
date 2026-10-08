---
last_updated: 2026-10-08
scope: package
---

# ru.vych.logger.impl.entities

Сущности логгера: LogEvent.

## Публичные классы

### `LogEvent`

Контейнер для данных лог-сообщения, передаваемый между сервисом, фильтрами и аппендерами.

**Поля:** `serviceCode` (код сервиса-отправителя), `uuid` (trace ID), `loggingLevel` (уровень), `message` (текст), `timestamp` (`LocalDateTime`), `entities` (`List<Object>` — доп. объекты).

**Ломбок:** `@Getter @Setter @AllArgsConstructor @ToString`.

**Фабричный метод:** `static create(String serviceCode, String uuid, LoggingLevel, String message, Object... entities)` — создаёт `LogEvent` с текущим timestamp и преобразует varargs в `List`.

**Особенности:**
- **Несоответствие:** Javadoc говорит «неизменяемый», но `@Setter` делает все поля мутируемыми.
- `entities` — `Arrays.asList(varargs)` — **фиксированный список**, `add()`/`remove()` выбросит `UnsupportedOperationException`.
- `timestamp` устанавливается в момент создания через фабричный метод.

## Зависимости

- `LogEvent` → `LoggingLevel`, `LocalDateTime`, `Arrays`, `List`

## Связанные пакеты

- [`impl`](./impl.md) — `LogService` создаёт `LogEvent`
- [`impl/appenders`](./impl/appenders.md) — `ConsoleAppender` получает `LogEvent`
- [`impl/common`](./impl/common.md) — `LoggingLevel`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
