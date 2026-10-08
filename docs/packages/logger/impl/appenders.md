---
last_updated: 2026-10-09
scope: package
---

# ru.vych.logger.impl.appenders

Интерфейс аппендера и консольная реализация.

## Публичные классы

### `LogAppender` (interface)

Интерфейс-контракт для всех аппендеров (обработчиков вывода логов).

**Методы:**
- `void append(LogEvent event) throws LoggerException` — добавляет событие в хранилище/вывод.
- `String getServiceCode()` — возвращает идентификатор аппендера.

**Нюанс:** `append` объявляет `throws LoggerException` — проверяемое исключение.

### `ConsoleAppender`

Реализация `LogAppender` для вывода логов в `System.out` / `System.err`.

**Формат вывода:** по умолчанию `timestamp     LEVEL     [serviceCode] : message entities`. Поддерживает кастомное форматирование через плейсхолдеры (`%date`, `%level`, `%serviceCode`, `%message`, `%entity`) — аналогично файловому аппендеру.

**Ключевые методы:**
- `append(LogEvent event)` — проверяет уровень, определяет поток (System.err для ERROR), форматирует строку (кастомный форматтер или формат по умолчанию), применяет ANSI-цвета, сериализует entities в JSON.
- `formatLogLine(LogEvent event)` — форматирует событие в строку по кастомному или дефолтному формату.
- `applyColorToLevel(String line, LoggingLevel level)` — применяет ANSI-цвет к первому вхождению уровня в строке.
- `applyDimToEntities(String line)` — применяет ANSI dim-эффект к JSON-строке entities.
- `getColorByLevel(LoggingLevel)` — маппинг: DEBUG→white, INFO→blue, WARN→yellow, ERROR→red.

**Поля (final, `@RequiredArgsConstructor`):** `loggingLevel`, `includeEntities`, `prettyEntities`, `enableColors`, `dimEntities`, `formatPattern`.

**Lombok:** `@RequiredArgsConstructor`.

**Особенности:**
- Pretty-print entities получает ANSI-white + dim-эффект при `dimEntities=true`.
- Кастомный форматтер применяется при заданном `formatPattern`; при неверном формате (нет поддерживаемых плейсхолдеров) используется формат по умолчанию с выводом diagnostics в System.err.
- Фиксированная ширина для `%level` (5 символов, выравнивание по левому краю) и `%serviceCode` (30 символов, выравнивание по правому краю) применяется только к первому вхождению плейсхолдера.

### `FileAppender`

Реализация `LogAppender` для записи логов в файлы с поддержкой ежедневной ротации, настраиваемого форматирования и буферизации.

**Формат вывода:** по умолчанию идентичен консольному аппендеру: `<timestamp>     <LEVEL>     [<serviceCode>] : <message> <entities>`.

**Ключевые методы:**
- `init()` — валидирует конфигурацию, создаёт директорию, открывает файл для дозаписи.
- `append(LogEvent event)` — проверяет уровень, форматирует строку, записывает в буфер с flush, потокобезопасен.
- `openNewFile()` — открывает новый файл по паттерну с подстановкой даты и timestamp.
- `extractDateFromFilename(String filename)` — извлекает дату из имени файла для проверки ротации.
- `rotateIfNeeded()` — при смене даты открывает новый файл, закрывает старый.
- `close()` — flushит буфер, закрывает файл.

**Поля (final, `@RequiredArgsConstructor`):** `properties` (`FileAppenderProperties`), `writeMonitor` (потокобезопасность).

**Транзиентные поля:** `logFilePath` (`Path`), `currentDateInFilename` (`LocalDate`), `writer` (`BufferedWriter`).

**Lombok:** `@RequiredArgsConstructor`.

**Внутреннее логирование:** `internalLog(String)` → `System.out` (инфо), `internalLogError(String)` → `System.err` (ошибки). Используется для диагностики: инициализация, ротация, закрытие.

**Особенности:**
- Ротация по дате (не по размеру).
- Суффиксы `_1`, `_2` при коллизии имён файлов.
- Потокобезопасная запись через `synchronized(writeMonitor)`.
- Старые файлы не удаляются автоматически.

## Зависимости

- `ConsoleAppender` → `AnsiColor`, `LoggingLevel`, `ObjectMapperUtils`, `LogEvent`, `LoggerAppenderException`

## Связанные пакеты

- [`impl`](./impl.md) — `LogService` вызывает `LogAppender`
- [`impl/common`](./impl/common.md) — `AnsiColor`, `LoggingLevel`, `ObjectMapperUtils`
- [`impl/entities`](./impl/entities.md) — `LogEvent`
- [`impl/exceptions`](./impl/exceptions.md) — `LoggerAppenderException`

## Кросс-ссылки

- [modules/logger.md](../../modules/logger.md) — документация модуля
- [packages/logger/impl.md](./impl.md) — документация пакета impl
