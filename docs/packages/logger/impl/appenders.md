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

**Формат вывода:** `timestamp | LEVEL | [serviceCode] : message [entities]`.

**Ключевые методы:**
- `append(LogEvent event)` — проверяет уровень, определяет поток (System.err для ERROR), форматирует строку, применяет ANSI-цвета, сериализует entities в JSON.
- `getColorByLevel(LoggingLevel)` — маппинг: DEBUG→white, INFO→blue, WARN→yellow, ERROR→red.
- `writeEntities(List<Object>)` — JSON через `ObjectMapperUtils`.

**Поля (final, `@RequiredArgsConstructor`):** `loggingLevel`, `includeEntities`, `prettyEntities`, `enableColors`, `dimEntities`.

**Lombok:** `@RequiredArgsConstructor`.

**Особенности:** pretty-print entities получает ANSI-white + dim-эффект при `dimEntities=true`.

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
