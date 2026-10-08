---
last_updated: 2026-10-09
scope: package
---

# ru.vych.logger.config

Автоконфигурация Spring и свойства логгера.

## Публичные классы

### `LoggerAutoConfiguration`

Автоматическая конфигурация Spring Boot, регистрирующая бины `LogService`, `ConsoleAppender` и `FileAppender`.

**Ключевые методы:**
- `@Bean @ConditionalOnMissingBean logService(List<LogAppender>, List<LogFilter>)` — создаёт `LogService`.
- `@Bean @ConditionalOnProperty(value = "logger.console.enabled", matchIfMissing = true) consoleAppender(LogProperties)` — создаёт `ConsoleAppender` (включён по умолчанию).
- `@Bean @ConditionalOnProperty(prefix = "logger.file", name = "enabled", havingValue = "true") fileAppender(FileAppenderProperties)` — создаёт `FileAppender` (выключён по умолчанию).

**Lombok:** не используется. `@EnableConfigurationProperties({LogProperties.class, FileAppenderProperties.class})`.

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
| `formatPattern` | `String` | `null` | Кастомный формат строки лога через плейсхолдеры (`%date`, `%level`, `%serviceCode`, `%message`, `%entity`) |

**Lombok:** `@Getter @Setter @ConfigurationProperties(prefix = "logger")`.

### `FileAppenderProperties`

Привязка конфигурационных свойств файлового аппендера из `application.yaml` с префиксом `logger.file`.

| Поле | Тип | Default | Описание |
|---|---|---|---|
| `enabled` | `boolean` | `false` | Включён ли файловый аппендер |
| `dir` | `String` | `./logs` | Директория для файлов логов |
| `filenamePattern` | `String` | `app-{date}_{timestamp}.log` | Паттерн имени файла |
| `datePattern` | `String` | `yyyy-MM-dd` | Формат даты (DateTimeFormatter) |
| `encoding` | `String` | `UTF-8` | Кодировка файла |
| `level` | `LoggingLevel` | `INFO` | Минимальный уровень логирования |
| `includeEntities` | `boolean` | `false` | Включать доп. объекты в вывод |
| `prettyEntities` | `boolean` | `false` | Pretty-print JSON объектов |
| `bufferSize` | `int` | `8192` | Размер буфера в байтах |
| `logFormatter` | `String` | `%date     %level     %serviceCode : %message %entity` | Формат строки лога |

**Методы:**
- `formatDate()` — форматирует текущую дату по `datePattern`.
- `generateTimestamp()` — генерирует UNIX timestamp (секунды).
- `generateFilename()` — генерирует имя файла по паттерну с подстановкой даты и timestamp.
- `getFullLogFilePath()` — генерирует уникальный путь (добавляет `_1`, `_2` при коллизии).
- `validate()` — валидирует `datePattern`.

**Lombok:** `@Getter @Setter @ConfigurationProperties(prefix = "logger.file")`.

### `FileAppenderProvider`

Провайдер для создания и инициализации `FileAppender`.

**Методы:**
- `create()` — создаёт `FileAppender` и вызывает `init()`, возвращает настроенный аппендер.

**Lombok:** `@RequiredArgsConstructor`.

## Связанные пакеты

- [`impl`](./impl.md) — `LogService`
- [`impl/appenders`](./impl/appenders.md) — `ConsoleAppender`
- [`impl/common`](./impl/common.md) — `LoggingLevel`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
