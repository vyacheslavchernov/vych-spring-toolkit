# Examples — Spec Edit

Примеры хороших и плохих спецификаций, шаблоны.

## Хорошие примеры

### Пример 1: Новая функциональность

```markdown
# logger-file-appender-spec — File Appender

## Purpose

Файловый аппендер для записи логов в файл с поддержкой ротации по дате.
Позволяет сохранять логи в файловую систему с настраиваемым форматом и кодировкой.

## Preconditions

- Spring Boot application с подключённым `logger-spring-boot-starter`
- Свойство `logger.file.enabled=true` установлено в конфигурации
- Директория для логов доступна для записи (`logger.file.dir`)

## Inputs

### Конфигурация

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `dir` | `String` | `./logs` | Директория для хранения логов |
| `filename-pattern` | `String` | `app-{date}_{timestamp}.log` | Шаблон имени файла |
| `date-pattern` | `String` | `yyyy-MM-dd` | Паттерн даты в имени файла |
| `encoding` | `String` | `UTF-8` | Кодировка файла |
| `level` | `LoggingLevel` | `INFO` | Минимальный уровень логирования |
| `include-entities` | `boolean` | `false` | Включать дополнительные объекты |
| `pretty-entities` | `boolean` | `false` | Pretty-print объектов |
| `buffer-size` | `int` | `8192` | Размер буфера записи в байтах |
| `log-formatter` | `String` | `%date %level %serviceCode: %message %entity` | Шаблон строки лога |

### Шаблон log-formatter

Поддерживаемые токены:
- `%date` — timestamp события
- `%level` — уровень логирования
- `%serviceCode` — код сервиса
- `%message` — текст сообщения
- `%entity` — сериализованные объекты (JSON)

## Behavior

### Создание файла

1. При первом вызове `append(event)`:
   - Формируется имя файла по `filename-pattern`
   - `{date}` заменяется на текущую дату по `date-pattern`
   - `{timestamp}` заменяется на `System.currentTimeMillis()`
   - Создаётся файл с указанной `encoding`
   - Открывается `BufferedWriter` с `buffer-size`

2. При последующих вызовах:
   - Событие записывается в открытый файл
   - Если `pretty-entities=true`, объекты форматируются с отступами
   - Если `include-entities=false`, entities игнорируются

### Ротация файлов

1. При создании нового файла проверяется текущая дата
2. Если дата изменилась с момента последнего создания:
   - Текущий файл закрывается
   - Создаётся новый файл с датой в имени
3. Старые файлы не удаляются автоматически

### Запись события

1. Формируется строка по `log-formatter`:
   - Токены заменяются на значения из `LogEvent`
   - Entities сериализуются в JSON (если включены)
2. Строка записывается в буфер
3. При заполнении буфера или вызове `flush()`:
   - Буфер сбрасывается в файл
   - Если ошибка записи — логируется на `System.err`

## Business rules

- Файл создаётся только при первом событии, не при инициализации
- Имя файла уникально для каждой даты и сессии (timestamp)
- Если `dir` не существует — создаётся автоматически
- Если `dir` недоступен для записи — бросается `LoggerAppenderException`
- Пустой текст сообщения допустим
- Пустой список entities допустим
- `buffer-size` должен быть > 0, иначе бросается `LoggerAppenderException`

## Errors

| Ситуация | Исключение | Поведение |
|---|---|---|
| Директория недоступна для записи | `LoggerAppenderException` | Файл не создаётся, событие не записывается |
| `buffer-size <= 0` | `LoggerAppenderException` | Исключение бросается при создании аппендера |
| Ошибка записи в файл | `LoggerAppenderException` | Ошибка логируется на `System.err`, обработка продолжается |
| Ошибка сериализации entities | `LoggerAppenderException` | Entities не включаются в запись, остальное записывается |

## Acceptance criteria

- [ ] Файл создаётся при первом событии логирования
- [ ] Имя файла формируется по шаблону с датой и timestamp
- [ ] Директория создаётся автоматически, если не существует
- [ ] События записываются в файл с указанной кодировкой
- [ ] `log-formatter` корректно заменяет токены
- [ ] `pretty-entities=true` форматирует объекты с отступами
- [ ] `include-entities=false` игнорирует объекты
- [ ] Ротация происходит при смене даты
- [ ] Ошибка записи логируется на `System.err`
- [ ] `buffer-size` влияет на размер буфера записи

## Examples

### Конфигурация в YAML

```yaml
logger:
  file:
    enabled: true
    dir: ./logs
    filename-pattern: app-{date}_{timestamp}.log
    date-pattern: yyyy-MM-dd
    encoding: UTF-8
    level: DEBUG
    include-entities: true
    pretty-entities: true
    buffer-size: 8192
    log-formatter: "%date [%level] %serviceCode: %message %entity"
```

### Использование

```java
// ConsoleAppender и FileAppender подключены через Spring
logService.info("MyService", uuid, "Сообщение", someObject);
// Результат:
// 2026-10-09 15:30:00 [INFO] MyService: Сообщение {"key":"value"}
```
```

### Пример 2: Обновление существующей спецификации

```markdown
# http-cookies-spec — Cookie Management

## Purpose

Управление cookie в HTTP-клиенте. Изолированное хранилище cookie для каждого экземпляра клиента.

## Changes from previous version

### Добавлено
- Метод `getAllCookies()` — возвращает immutable map всех cookie
- Метод `clearCookies(String host)` — очистка cookie для хоста
- `CookiesPolicies` enum с тремя значениями вместо двух

### Изменено
- `getCookies()` теперь возвращает `List<HttpCookie>` вместо `Map`
- Cookie-хранилище использует `CopyOnWriteArrayList` вместо `ArrayList`

### Удалено
- Метод `getCookie(String name)` — использовать `getCookies()` вместо него

## Preconditions

- HTTP-клиент инициализирован с валидной конфигурацией
- Cookie-политика разрешает приём cookie (по умолчанию `ACCEPT_ALL`)

## Inputs

### CookiesPolicies

| Значение | Описание |
|---|---|
| `ACCEPT_ALL` | Принимать все cookie от серверов |
| `ACCEPT_NONE` | Отклонять все cookie |
| `ACCEPT_ORIGINAL_SERVER` | Принимать только cookie от исходного сервера |

### HttpClient методы

| Метод | Возвращаемый тип | Описание |
|---|---|---|
| `getCookies(String host)` | `List<HttpCookie>` | Cookies для конкретного хоста |
| `getAllCookies()` | `Map<String, List<HttpCookie>>` | Все cookies (immutable map) |
| `clearCookies(String host)` | `void` | Очистить cookies для хоста |
| `getClientUuid()` | `String` | UUID клиента |

## Behavior

### Сохранение cookie

1. При получении ответа с `Set-Cookie` заголовком:
   - Проверяется cookie-политика
   - Если политика разрешает — cookie сохраняется в хранилище
   - Хранилище: `ConcurrentHashMap<String, CopyOnWriteArrayList<HttpCookie>>`
   - Ключ — host (из URL запроса)

2. При отправке запроса:
   - Cookies для хоста добавляются как `Cookie` header
   - Если cookies нет — header не добавляется

### Получение cookie

1. `getCookies(host)` — возвращает копию списка cookie для хоста
2. `getAllCookies()` — возвращает **immutable** map: `Collections.unmodifiableMap()`
3. Если host не найден — возвращается пустой список / пустая карта

### Очистка cookie

1. `clearCookies(host)` — удаляет все cookies для хоста
2. Если host не найден — исключение не бросается, операция игнорируется

## Business rules

- Каждый клиент имеет **полностью изолированное** хранилище cookie
- Глобальный `CookieHandler` не используется
- `getAllCookies()` всегда возвращает immutable map
- Cookie привязаны к хосту, не к пути
- `CopyOnWriteArrayList` обеспечивает thread-safe чтение

## Errors

| Ситуация | Исключение |
|---|---|
| `host == null` в `getCookies()` | `HttpClientInvalidRequestException` |
| `host == null` в `clearCookies()` | `HttpClientInvalidRequestException` |

## Acceptance criteria

- [ ] `getCookies(host)` возвращает cookies для хоста
- [ ] `getCookies(null)` бросает `HttpClientInvalidRequestException`
- [ ] `getAllCookies()` возвращает immutable map
- [ ] `clearCookies(host)` удаляет cookies для хоста
- [ ] `clearCookies(null)` бросает `HttpClientInvalidRequestException`
- [ ] `clearCookies(unknown-host)` не бросает исключение
- [ ] Cookie изолированы между клиентами
- [ ] Чтение cookie thread-safe

## Examples

### Настройка политики

```java
HttpClientConfig config = new HttpClientConfig("MyService")
    .setRoot("https://api.example.com")
    .setCookiePolicy(CookiesPolicies.ACCEPT_NONE);
```

### Управление cookie

```java
// Получение cookies
List<HttpCookie> cookies = client.getCookies("api.example.com");

// Все cookies
Map<String, List<HttpCookie>> all = client.getAllCookies();

// Очистка
client.clearCookies("api.example.com");
```
```

## Плохие примеры

### Пример 1: Слишком общая спецификация

```markdown
# Bad Spec

## Описание
HTTP-клиент для работы с API.

## Поведение
Делает запросы к серверу и возвращает ответ. Обычно работает корректно,
если сервер доступен.

## Критерии
- Клиент работает хорошо
- Ошибки обрабатываются правильно
```

**Почему плохо:**
- Нет чёткого описания поведения
- Нет входных данных и ограничений
- Нет конкретных ошибок и исключений
- Критерии не проверяемые ("работает хорошо", "обрабатывается правильно")
- Нет business rules

### Пример 2: Описание реализации вместо поведения

```markdown
# Bad Spec — Реализация

## Purpose
Клиент, который использует Java 11 HttpClient и Jackson для десериализации.

## Implementation
1. `HttpClientImpl` расширяет `AbstractHttpClient`
2. В конструкторе создаётся `ObjectMapper` с `JavaTimeModule`
3. Метод `execute()` вызывает `httpClient.send(request, responseHandler)`
4. Ответ обрабатывается через `HttpResponse.BodyHandlers.ofString()`

## Dependencies
- `spring-boot-starter`
- `lombok`
- `jackson-databind`
```

**Почему плохо:**
- Описывает **как**, а не **что**
- Упоминает классы, методы, фреймворки
- Спецификация должна быть независима от реализации
- Это документация к коду, а не спецификация поведения

### Пример 3: Неоднозначные формулировки

```markdown
# Bad Spec — Неоднозначность

## Behavior
- Клиент **обычно** отправляет запросы
- При ошибке **как правило** логируется сообщение
- Клиент **может** поддерживать retry **по возможности**
- Таймаут **примерно** 15 секунд
```

**Почему плохо:**
- `обычно`, `как правило`, `по возможности`, `примерно` — неопределённые формулировки
- Каждое требование должно быть проверяемым
- Нельзя определить, выполнено требование или нет

## Шаблоны

### Шаблон новой спецификации

```markdown
# {ID}-spec — {Название}

## Purpose
{Что и зачем делает функциональность. 1-2 предложения.}

## Preconditions
{Условия, необходимые для выполнения.}
- { precondition 1 }
- { precondition 2 }

## Inputs
{Входные данные и ограничения.}

### {Сущность 1}
| Поле | Тип | Описание |
|---|---|---|
| { field } | { type } | { description } |

### {Сущность 2}
...

## Behavior
{Подробное описание ожидаемого поведения.}

### {Сценарий 1}
1. { шаг 1 }
2. { шаг 2 }
3. { шаг 3 }

### {Сценарий 2}
...

## Business rules
{Правила и ограничения.}
- { rule 1 }
- { rule 2 }

## Errors
| Ситуация | Исключение | Поведение |
|---|---|---|
| { situation } | { exception } | { behavior } |

## Acceptance criteria
- [ ] { criterion 1 }
- [ ] { criterion 2 }
- [ ] { criterion N }

## Examples
{Примеры поведения, если они помогают устранить неоднозначность.}
```

### Шаблон обновления спецификации

```markdown
# {ID}-spec — {Название}

## Purpose
{Те же purpose, что в оригинале}

## Changes from previous version

### Добавлено
- { что добавлено }
- { что добавлено }

### Изменено
- { что изменено: было → стало }
- { что изменено: было → стало }

### Удалено
- { что удалено }

## { Остальные секции — обновить в соответствии с изменениями }
...
```
