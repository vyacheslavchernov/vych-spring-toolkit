---
last_updated: 2026-10-08
scope: package
---

# ru.vych.logger.config

Автоконфигурация Spring и свойства логгера.

## Публичные классы

### `LoggerAutoConfiguration`

Автоматическая конфигурация Spring Boot, регистрирующая бины `LogService` и `ConsoleAppender`.

**Ключевые методы:**
- `@Bean @ConditionalOnMissingBean logService(List<LogAppender>, List<LogFilter>)` — создаёт `LogService`.
- `@Bean @ConditionalOnProperty(value = "logger.console.enabled", matchIfMissing = true) consoleAppender(LogProperties)` — создаёт `ConsoleAppender` (включён по умолчанию).

**Lombok:** не используется. `@EnableConfigurationProperties(LogProperties.class)`.

### `LogProperties`

Привязка конфигурационных свойств из `application.yaml` с префиксом `logger`.

**Вложенный класс `Console`:**
| Поле | Тип | Default | Описание |
|---|---|---|---|
| `enabled` | `boolean` | `true` | Включён ли консольный аппендер |
| `level` | `LoggingLevel` | `INFO` | Минимальный уровень логирования |
| `includeEntities` | `boolean` | `false` | Включать доп. объекты в вывод |
| `prettyEntities` | `boolean` | `false` | Pretty-print JSON объектов |
| `enableColors` | `boolean` | `false` | Использовать ANSI-цвета |
| `dimEntities` | `boolean` | `false` | Делать вывод объектов менее ярким |

**Lombok:** `@Getter @Setter @ConfigurationProperties(prefix = "logger")`.

## Связанные пакеты

- [`impl`](./impl.md) — `LogService`
- [`impl/appenders`](./impl/appenders.md) — `ConsoleAppender`
- [`impl/common`](./impl/common.md) — `LoggingLevel`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
