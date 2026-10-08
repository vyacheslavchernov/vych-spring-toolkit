# logger-console-appender-spec — ConsoleAppender

## Purpose

Аппендер для вывода лог-сообщений в консоль (`System.out` / `System.err`). Поддерживает фильтрацию по уровню логирования, ANSI-цветирование, сериализацию дополнительных объектов в JSON и разделение потоков вывода (ERROR → `System.err`, остальные → `System.out`).

## Preconditions

- Logger-модуль (`logger-spring-boot-starter`) подключён к Spring Boot приложению
- `ConsoleAppender` зарегистрирован как Spring Bean через `LoggerAutoConfiguration`
- Jackson `ObjectMapper` настроен с `JavaTimeModule` для поддержки Java 8 date/time типов

## Inputs

### Конфигурационные свойства

Свойства привязываются к префиксу `logger.console` через `LogProperties.Console`:

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `logger.console.enabled` | `boolean` | `true` | Включить/выключить консольный аппендер |
| `logger.console.level` | `LoggingLevel` | `INFO` | Минимальный уровень логирования |
| `logger.console.include-entities` | `boolean` | `false` | Включать JSON-представление дополнительных объектов |
| `logger.console.pretty-entities` | `boolean` | `false` | Форматировать ли JSON с отступами |
| `logger.console.enable-colors` | `boolean` | `false` | Использовать ANSI-цвета в выводе |
| `logger.console.dim-entities` | `boolean` | `false` | Делать вывод объектов менее ярким (ANSI dim) |

### ANSI-цвета по уровням

| Уровень | Цвет | ANSI-код |
|---|---|---|
| `DEBUG` | Белый | `AnsiColor.WHITE` |
| `INFO` | Синий | `AnsiColor.BLUE` |
| `WARN` | Жёлтый | `AnsiColor.YELLOW` |
| `ERROR` | Красный | `AnsiColor.RED` |

## Behavior

### Создание и инициализация

1. `ConsoleAppender` создаётся как Spring Bean через `LoggerAutoConfiguration.consoleAppender()`
2. Конфигурация передаётся через `LogProperties.Console`
3. Аппендер **включён по умолчанию** (`matchIfMissing = true` в `@ConditionalOnProperty`)

### Вывод сообщений

1. При вызове `append(LogEvent event)`:
   - Если уровень события ниже настроенного минимального (`loggingLevel`) — событие пропускается
   - Определяется целевой поток вывода:
     - `LoggingLevel.ERROR` → `System.err`
     - Все остальные уровни → `System.out`
   - Формируется строка вывода в формате:
     ```
     <timestamp>     <LEVEL>     [<serviceCode>] : <message> <entities>
     ```
   - Поля разделены фиксированной шириной:
     - `timestamp` — без ограничения ширины
     - `LEVEL` — выровнен по правому краю, ширина 5 символов
     - `[serviceCode]` — выровнен по правому краю, ширина 30 символов
     - `message` — без ограничения ширины
     - `entities` — JSON-представление объектов (если `includeEntities=true`)
2. Если `enableColors=true`:
   - Значение `LEVEL` оборачивается в ANSI-код цвета + `AnsiColor.RESET`
   - Цвет определяется по таблице ANSI-цветов по уровням
3. Если `includeEntities=true`:
   - Объекты сериализуются в JSON через `ObjectMapperUtils`
   - Если `prettyEntities=true` — используется `ObjectMapperUtils.toPrettyJson()`
   - Иначе — `ObjectMapperUtils.toJson()` (компактный JSON)
   - Префикс `": "` добавляется перед JSON-строкой
4. Если `dimEntities=true` и `includeEntities=true`:
   - JSON-строка объектов оборачивается в `AnsiColor.WHITE` + `AnsiColor.RESET`

### Закрытие

Не требуется — `ConsoleAppender` не управляет ресурсами (не открывает файлы, не создаёт потоки).

## Business rules

- ERROR-сообщения всегда выводятся в `System.err`, все остальные — в `System.out`
- Фильтрация по уровню происходит **до** форматирования и вывода
- ANSI-цвета применяются только к значению уровня (например, `INFO`), а не ко всему сообщению
- `dimEntities` работает только вместе с `includeEntities`
- JSON-сериализация объектов использует общий `ObjectMapper` с `JavaTimeModule`
- Если `includeEntities=false`, параметр `prettyEntities` игнорируется

## Errors

| Ситуация | Поведение |
|---|---|
| Ошибка Jackson при сериализации объектов | Бросается `LoggerAppenderException` с сообщением «Не получилось сериализовать LogEvent entities» и cause |
| `event` = `null` в `append()` | Поведение не определено (не ожидается вызов с `null`) |

## Acceptance criteria

- [ ] При `logger.console.enabled=false` консольный аппендер не создаётся
- [ ] При `logger.console.enabled=true` консольный аппендер создаётся по умолчанию
- [ ] Сообщения уровня ниже `logger.console.level` пропускаются
- [ ] ERROR-сообщения выводятся в `System.err`, остальные — в `System.out`
- [ ] Формат строки: `<timestamp>     <LEVEL>     [<serviceCode>] : <message> <entities>`
- [ ] При `enable-colors=true` уровень окрашивается в соответствующий ANSI-цвет
- [ ] При `include-entities=true` объекты сериализуются в JSON и добавляются к сообщению
- [ ] При `pretty-entities=true` JSON форматируется с отступами
- [ ] При `dim-entities=true` JSON-строка объектов оборачивается в ANSI dim
- [ ] Ошибка сериализации объектов бросает `LoggerAppenderException`
- [ ] `getServiceCode()` возвращает `"ConsoleAppender"`

## Examples

### Вывод без entities

Конфигурация: `enable-colors=false`, `include-entities=false`

```
2026-10-08T14:30:22    INFO     [MyService] : User logged in
2026-10-08T14:30:23   ERROR     [MyService] : Connection refused
```

### Вывод с entities и цветами

Конфигурация: `enable-colors=true`, `include-entities=true`, `pretty-entities=false`

```
2026-10-08T14:30:22    INFO     [MyService] : User logged in : [{"userId":123,"ip":"192.168.1.1"}]
```

### Вывод ERROR в stderr

```
# System.err:
2026-10-08T14:30:23   ERROR     [MyService] : Connection refused
```
