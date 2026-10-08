# logger-log-service-spec — LogService

## Purpose

Центральный сервис логирования — единая точка входа для отправки лог-сообщений. Создаёт события логирования, применяет фильтры и маршрутизирует события во все подключённые аппендеры. Гарантирует, что ошибки в аппендерах не нарушают работу приложения.

## Preconditions

- Spring Boot application с подключённым `logger-spring-boot-starter`
- `LogService` зарегистрирован как Spring Bean через `LoggerAutoConfiguration`
- Хотя бы один `LogAppender` (по умолчанию — `ConsoleAppender`) доступен в контексте
- `LogEvent` доступен для создания событий

## Inputs

### Публичный API

Каждый уровень логирования имеет два варианта метода: с текстом сообщения и без:

| Метод | Параметры | Описание |
|---|---|---|
| `debug(String serviceCode, String uuid, String message, Object... entities)` | `serviceCode`, `uuid`, `message`, `entities` | Отправить DEBUG-сообщение |
| `debug(String serviceCode, String uuid, Object... entities)` | `serviceCode`, `uuid`, `entities` | Отправить DEBUG-сообщение без текста |
| `info(String serviceCode, String uuid, String message, Object... entities)` | `serviceCode`, `uuid`, `message`, `entities` | Отправить INFO-сообщение |
| `info(String serviceCode, String uuid, Object... entities)` | `serviceCode`, `uuid`, `entities` | Отправить INFO-сообщение без текста |
| `warn(String serviceCode, String uuid, String message, Object... entities)` | `serviceCode`, `uuid`, `message`, `entities` | Отправить WARN-сообщение |
| `warn(String serviceCode, String uuid, Object... entities)` | `serviceCode`, `uuid`, `entities` | Отправить WARN-сообщение без текста |
| `error(String serviceCode, String uuid, String message, Object... entities)` | `serviceCode`, `uuid`, `message`, `entities` | Отправить ERROR-сообщение |
| `error(String serviceCode, String uuid, Object... entities)` | `serviceCode`, `uuid`, `entities` | Отправить ERROR-сообщение без текста |
| `log(String serviceCode, String uuid, LoggingLevel loggingLevel, String message, Object... entities)` | `serviceCode`, `uuid`, `loggingLevel`, `message`, `entities` | Отправить сообщение с произвольным уровнем |

### Параметры

| Параметр | Тип | Описание |
|---|---|---|
| `serviceCode` | `String` | Код сервиса-отправителя (идентификатор источника) |
| `uuid` | `String` | Уникальный идентификатор контекста (trace ID) |
| `loggingLevel` | `LoggingLevel` | Уровень логирования: `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `message` | `String` | Текст сообщения (может быть пустой) |
| `entities` | `Object...` | Дополнительные объекты для сериализации в JSON (может быть пустым) |

### Внутренние поля

| Поле | Тип | Описание |
|---|---|---|
| `SERVICE_CODE` | `String` | Константа `"LoggerService"` для внутренних логов |
| `uuid` | `String` | UUID экземпляра сервиса (генерируется при создании) |
| `appenders` | `List<LogAppender>` | Список подключённых аппендеров |
| `logFilters` | `List<LogFilter>` | Список подключённых фильтров |

## Behavior

### Создание и инициализация

1. `LogService` создаётся как Spring Bean через `LoggerAutoConfiguration.logService()`
2. Конструктор принимает `List<LogAppender>` и `List<LogFilter>` из Spring-контекста
3. При создании генерируется уникальный UUID экземпляра (`UUID.randomUUID()`)
4. Сразу после создания логируется сообщение `INFO`:
   - `serviceCode` = `SERVICE_CODE` (`"LoggerService"`)
   - `uuid` = сгенерированный UUID экземпляра
   - `message` = `"Инициализирован сервис логирования"`
   - `entities` = список имён классов всех подключённых аппендеров

### Обработка лог-сообщения

При вызове любого из публичных методов (`debug`, `info`, `warn`, `error`, `log`):

1. Создаётся событие `LogEvent` через `LogEvent.create(serviceCode, uuid, loggingLevel, message, entities...)`:
   - `serviceCode` — переданный параметр
   - `uuid` — переданный параметр
   - `loggingLevel` — переданный уровень
   - `message` — переданный текст
   - `timestamp` — текущее время (`LocalDateTime.now()`)
   - `entities` — переданные объекты, преобразованные в `List<Object>`

2. Для каждого аппендера из `appenders`:
   - Событие последовательно проходит через все фильтры из `logFilters`
   - Если хотя бы один фильтр возвращает `false` — событие **не передаётся** в этот аппендер, переход к следующему аппендеру
   - Если все фильтры вернули `true` (или фильтров нет) — вызывается `appender.append(event)`

3. При ошибке записи в аппендер (`LoggerException`):
   - Ошибка перехватывается и логируется на уровне `ERROR`
   - `serviceCode` = `SERVICE_CODE` (`"LoggerService"`)
   - `uuid` = UUID экземпляра сервиса
   - `message` = `"Не удалось отправить лог."`
   - `entities` = `appender.getServiceCode()` и строковое представление события (`event.toString()`)
   - Обработка **продолжается** для остальных аппендеров

### Потокобезопасность

Поведение при параллельном вызове методов `LogService` не определено и не гарантируется.

## Business rules

- Каждый вызов публичного метода создаёт новое событие `LogEvent` с актуальным `timestamp`
- UUID передается вызывающим кодом — `LogService` не генерирует UUID для входящих сообщений
- UUID экземпляра `LogService` генерируется один раз при создании и используется для внутренних логов
- Фильтры применяются **перцептивно к каждому аппендеру** — результат фильтрации одного аппендера не влияет на другой
- Ошибка в одном аппендере не влияет на запись в другие аппендеры
- Пустой текст сообщения (`""`) допустим и не обрабатывается особым образом
- Пустой список entities (`entities...` без аргументов) допустим и преобразуется в пустой список

## Errors

| Ситуация | Исключение | Поведение |
|---|---|---|
| Ошибка записи в аппендер | `LoggerException` (или подтип) | Ошибка логируется на уровне `ERROR`, обработка продолжается для остальных аппендеров |
| `LoggerAppenderException` | `LoggerAppenderException` | Частный случай `LoggerException`, поведение как выше |

## Acceptance criteria

- [ ] `LogService` создаётся как Spring Bean с типом `@Service`
- [ ] При создании логируется сообщение `INFO` со списком подключённых аппендеров
- [ ] UUID экземпляра генерируется один раз при создании
- [ ] Методы `debug/info/warn/error` создают события с соответствующим уровнем
- [ ] Варианты методов без `message` передают пустую строку
- [ ] Событие содержит актуальный `timestamp` (`LocalDateTime.now()`)
- [ ] Фильтры применяются последовательно — первый вернувший `false` останавливает обработку для этого аппендера
- [ ] Если хотя бы один фильтр вернул `false`, событие не передаётся в аппендер
- [ ] Ошибка в одном аппендере не останавливает обработку остальных
- [ ] Ошибка в аппендере логируется на уровне `ERROR` с кодом аппендера и событием
- [ ] `SERVICE_CODE` всегда равен `"LoggerService"`

## Examples

### Отправка сообщения

```java
logService.info("UserService", userTraceId, "User logged in", userEntity);
```

Создаётся `LogEvent` с `serviceCode="UserService"`, `loggingLevel=INFO`, `message="User logged in"`, `entities=[userEntity]`. Событие проходит через фильтры и передаётся во все аппендеры.

### Фильтрация

```java
// Если logFilters содержит фильтр, который возвращает false для DEBUG,
// то DEBUG-сообщения не будут переданы ни в один аппендер
logService.debug("MyService", traceId, "Debug info");
// Ничего не выводится — фильтр отсек событие
```

### Обработка ошибки аппендера

```java
// Если один из аппендеров бросает LoggerException:
// 1. Логгируется: error("LoggerService", uuid, "Не удалось отправить лог.", appender.getServiceCode(), event.toString())
// 2. Остальные аппендеры продолжают получать события
```
