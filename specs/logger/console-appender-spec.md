# logger-console-appender-spec — ConsoleAppender (обновлено 2026-10-09: добавлена поддержка кастомного форматирования через шаблон)

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
| `logger.console.format-pattern` | `String` | `null` | Кастомный формат строки лога через плейсхолдеры (если не задан — используется формат по умолчанию) |

### Форматирование строки лога

Пользователь может настроить формат строки лога через свойство `logger.console.format-pattern`. Поддерживаемые плейсхолдеры:

| Плейсхолдер | Описание | Пример |
|---|---|---|
| `%date` | Дата/время события | `2026-10-08 14:30:22` |
| `%level` | Уровень логирования (выровнен по левому краю, ширина 5) | `INFO`, `WARN`, `ERROR` |
| `%serviceCode` | Код сервиса в квадратных скобках (выровнен по правому краю, ширина 30) | `[MyService]` |
| `%message` | Основное сообщение | `User logged in` |
| `%entity` | JSON-представление entities | `{"key":"value"}` |

**Ограничение:** Если в кастомном форматтере плейсхолдер `%level` или `%serviceCode` встречается несколько раз, форматирование с фиксированной шириной применяется только к первому вхождению. Остальные вхождения заменяются на значение без форматирования.

**По умолчанию:** если `format-pattern` не задан, используется формат:
```
<timestamp>     <LEVEL>     [<serviceCode>] : <message> <entities>
```

Пример:
```
2026-10-08 14:30:22    INFO     [MyService] : User logged in
2026-10-08 14:30:23   ERROR     [MyService] : Connection refused
```

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
   - Определяется формат строки:
     - Если `format-pattern` задан — используется кастомный форматтер с подстановкой плейсхолдеров
     - Если `format-pattern` не задан — используется формат по умолчанию (идентичный файловому аппендеру)
   - Если `includeEntities=true`, к сообщению добавляется JSON-представление entities (плейсхолдер `%entity` или суффикс после `:`)
   - Формируется итоговая строка вывода
2. Если `enableColors=true`:
   - Значение `LEVEL` оборачивается в ANSI-код цвета + `AnsiColor.RESET`
   - Цвет определяется по таблице ANSI-цветов по уровням
   - Цвет применяется только к первому вхождению `%level` в кастомном форматтере (или к уровню в формате по умолчанию)
3. Если `includeEntities=true`:
   - Объекты сериализуются в JSON через `ObjectMapperUtils`
   - Если `prettyEntities=true` — используется `ObjectMapperUtils.toPrettyJson()`
   - Иначе — `ObjectMapperUtils.toJson()` (компактный JSON)
   - При формате по умолчанию префикс `": "` добавляется перед JSON-строкой
4. Если `dimEntities=true` и `includeEntities=true`:
   - JSON-строка объектов оборачивается в `AnsiColor.WHITE` + `AnsiColor.RESET`
   - При кастомном форматтере `dimEntities` применяется к значению плейсхолдера `%entity`

### Закрытие

Не требуется — `ConsoleAppender` не управляет ресурсами (не открывает файлы, не создаёт потоки).

## Business rules

- ERROR-сообщения всегда выводятся в `System.err`, все остальные — в `System.out`
- Фильтрация по уровню происходит **до** форматирования и вывода
- ANSI-цвета применяются только к значению уровня (например, `INFO`), а не ко всему сообщению
- `dimEntities` работает только вместе с `includeEntities`
- JSON-сериализация объектов использует общий `ObjectMapper` с `JavaTimeModule`
- Если `includeEntities=false`, параметр `prettyEntities` игнорируется
- Если `format-pattern` не задан или содержит неверный формат (ни одного поддерживаемого плейсхолдера) — используется формат по умолчанию
- Кастомный форматтер и формат по умолчанию поддерживают одинаковый набор плейсхолдеров
- Фиксированная ширина для `%level` (5 символов) и `%serviceCode` (30 символов) применяется только к первому вхождению плейсхолдера

## Errors

| Ситуация | Поведение |
|---|---|
| Ошибка Jackson при сериализации объектов | Бросается `LoggerAppenderException` с сообщением «Не получилось сериализовать LogEvent entities» и cause |
| `event` = `null` в `append()` | Поведение не определено (не ожидается вызов с `null`) |
| Неверный формат форматтера строки лога (не содержит ни одного поддерживаемого плейсхолдера) | Используется формат по умолчанию, выводится диагностическое сообщение в System.err |

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
- [ ] При `format-pattern` не задан используется формат по умолчанию
- [ ] При заданном `format-pattern` строка форматируется по шаблону с подстановкой плейсхолдеров
- [ ] При неверном формате форматтера (нет поддерживаемых плейсхолдеров) используется формат по умолчанию с выводом diagnostics в System.err
- [ ] Фиксированная ширина применяется только к первому вхождению `%level` и `%serviceCode` в кастомном форматтере

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

### Вывод с кастомным форматтером

Конфигурация: `format-pattern="%date [%level] %serviceCode - %message"`, `enable-colors=true`, `include-entities=false`

```
2026-10-08 14:30:22 [INFO] [MyService] - User logged in
2026-10-08 14:30:23 [ERROR] [MyService] - Connection refused
```
