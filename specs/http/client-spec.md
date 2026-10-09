---
last_updated: 2026-10-10
---

# http-client-spec — HTTP Client Core

## Purpose

Типизированный HTTP-клиент для выполнения GET, POST, PUT, DELETE, PATCH, HEAD и OPTIONS запросов с builder-паттерном, Jackson-десериализацией и UUID-трейсингом. Предоставляет единый интерфейс для отправки HTTP-запросов и получения типизированных ответов.

## Preconditions

- Spring Boot application с подключённым `http-client-spring-boot-starter`
- Logger-модуль (`logger-spring-boot-starter`) доступен в classpath
- Jackson `ObjectMapper` доступен для сериализации/десериализации JSON

## Inputs

### HttpClient (интерфейс)

| Метод | Возвращаемый тип | Описание |
|---|---|---|
| `execute(Request)` | `Response` | Выполняет HTTP-запрос |
| `getCookies(String)` | `List<HttpCookie>` | Возвращает cookies для хоста |
| `getAllCookies()` | `Map<String, List<HttpCookie>>` | Возвращает immutable map всех cookies |
| `clearCookies(String)` | `void` | Очищает cookies для хоста |
| `getClientUuid()` | `String` | Возвращает UUID клиента |

### Request (builder)

| Builder-метод | Описание |
|---|---|
| `.setMethod(HttpMethod)` | Обязательный. HTTP-метод запроса (см. `HttpMethod`) |
| `.setUrl(String)` | Относительный URL, добавляемый к config `root` |
| `.addQueryParam(String, String)` | Добавляет query-параметр |
| `.addPathParam(String)` | Добавляет path-параметр как сегмент URL |
| `.addHeader(String, String)` | Добавляет заголовок |
| `.setContentType(String)` | Устанавливает `Content-Type` |
| `.setPayload(Object)` | Тело запроса: `String`, `byte[]` или JSON-сериализуемый объект |
| `.setResponseClass(Class<?>)` | Целевой тип для десериализации ответа |

### HttpMethod

| Значение | Описание |
|---|---|
| `GET` | HTTP GET запрос — получение ресурса. Не изменяет состояние сервера. |
| `POST` | HTTP POST запрос — отправка данных на сервер. Может изменять состояние сервера. |
| `PUT` | HTTP PUT запрос — полное обновление ресурса. Поддерживает тело запроса. |
| `DELETE` | HTTP DELETE запрос — удаление ресурса. Поддерживает тело запроса. |
| `PATCH` | HTTP PATCH запрос — частичное обновление ресурса. Поддерживает тело запроса. |
| `HEAD` | HTTP HEAD запрос — получение только заголовков ресурса. Тело запроса игнорируется. |
| `OPTIONS` | HTTP OPTIONS запрос — получение поддерживаемых методов ресурса. Поддерживает тело запроса. |

### Response

| Поле | Тип | Описание |
|---|---|---|
| `uuid` | `String` | UUID originating request |
| `request` | `Request` | Исходный запрос |
| `status` | `Integer` | HTTP status code |
| `rawBytes` | `byte[]` | Сырое тело ответа |
| `rawBody` | `String` | Тело ответа как UTF-8 строка |
| `body` | `Object` | Десериализованное тело (Jackson) |
| `headers` | `List<Header>` | Заголовки ответа |

## Behavior

### Создание клиента

1. Клиент создаётся через `HttpClientBuilder.build(config, logService, requestInterceptors, responseInterceptors)`
2. При создании клиент генерирует уникальный UUID
3. Клиент инициализируется с настроенным `ObjectMapper` (с `JavaTimeModule`)
4. Default cookies из `config.getCookies()` загружаются в изолированное cookie-хранилище
5. Cookie-хранилище использует `ConcurrentHashMap<String, CopyOnWriteArrayList<HttpCookie>>`

### Формирование URL

Метод `execute()` формирует полный URI из:
1. Конфигурационного `root` URL
2. Request `url` (path)
3. Path-параметров (через `/`)
4. Query-параметров (через `&` как `key=value`)

Custom percent-encoding применяется (RFC 3986), уже закодированные последовательности (например `%20`) сохраняются.

### Выполнение HTTP запроса

1. Все `RequestInterceptor` выполняются последовательно
2. Cookies для хоста запроса добавляются как `Cookie` header
3. Payload сериализуется (если установлен):
   - `null` → тело не устанавливается
   - `String` → отправляется как UTF-8 строка
   - `byte[]` → отправляется как bytes
   - Любой другой объект → Jackson `ObjectMapper.writeValueAsString()`
4. `Content-Type` header устанавливается автоматически из `config.getHeaders()` или из `Request`
5. Формируется `HttpRequest` с методом запроса и `BodyPublisher` (для методов с телом) или через соответствующий метод `HttpRequest.Builder` (для GET/HEAD)
6. Запрос отправляется через Java 11 `HttpClient.send()`
7. Ответ обрабатывается:
   - Status code копируется в `Response.status`
   - Тело ответа сохраняется в `rawBytes` и `rawBody` (UTF-8)
   - Заголовки ответа преобразуются в `List<Header>`
   - Если `responseClass` установлен и это не `String`, `byte`, `byte[]` — тело десериализуется через Jackson
   - `Set-Cookie` заголовки сохраняются в cookie-хранилище (если policy разрешает)
8. Все `ResponseInterceptor` выполняются последовательно
9. `Response` возвращается вызывающему

### Десериализация ответа

| `responseClass` | Поведение |
|---|---|
| `String.class` | `body` = `rawBody` (UTF-8 строка) |
| `null` | `body` = `null` |
| `byte.class` / `byte[].class` | `body` = `null` |
| Любой другой тип | Jackson `mapper.readValue(rawBody, responseClass)` |

### getCastBody()

Метод `Response.getCastBody()` возвращает типизированное тело:
- `null` если `responseClass` = `null`, `byte.class` или `byte[].class`
- Иначе возвращает `body`, приведённый к `responseClass`

## Business rules

- Поддерживаются HTTP-методы: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`, `OPTIONS`
- `HEAD` запрос игнорирует тело запроса (ограничение HTTP-протокола)
- Все остальные методы поддерживают тело запроса (payload)
- Каждый `Request` получает уникальный `UUID` при создании через `Request.builder()`
- Каждый `HttpClient` имеет уникальный `UUID` при создании через `HttpClientBuilder.build()`
- Cookie-хранилище полностью изолировано — каждый клиент имеет собственное хранилище
- `getAllCookies()` возвращает **immutable** map
- UUID используются для корреляции логов и запросов
- Jackson `ObjectMapper` настроен с `JavaTimeModule` для поддержки Java 8 date/time типов

## Errors

| Ситуация | Исключение |
|---|---|
| `Request` без установленного метода | `HttpClientInvalidRequestException` |
| Запрос с payload, но без `Content-Type` | `HttpClientInvalidRequestException` |
| Ошибка сети (таймаут, недоступность) | `HttpClientExecuteRequestException` |
| Ошибка Jackson при сериализации request body | `HttpClientHandleResponseException` |
| Ошибка Jackson при десериализации response body | `HttpClientHandleResponseException` |
| `config` = `null` при создании клиента | `HttpClientConfigurationException` |

## Acceptance criteria

- [ ] Клиент создаётся через `HttpClientBuilder` с валидной конфигурацией
- [ ] `execute()` с `GET` запросом возвращает `Response` с правильным status code
- [ ] `execute()` с `POST` запросом отправляет сериализованное тело
- [ ] `execute()` с `PUT` запросом отправляет сериализованное тело для обновления ресурса
- [ ] `execute()` с `DELETE` запросом отправляет запрос с телом для удаления ресурса
- [ ] `execute()` с `PATCH` запросом отправляет сериализованное тело для частичного обновления
- [ ] `execute()` с `HEAD` запросом возвращает только заголовки без тела
- [ ] `execute()` с `OPTIONS` запросом возвращает поддерживаемые методы ресурса
- [ ] `Request` без метода бросает `HttpClientInvalidRequestException`
- [ ] Запрос с payload без `Content-Type` бросает `HttpClientInvalidRequestException`
- [ ] Jackson десериализует JSON response в указанный `responseClass`
- [ ] `Response.getCastBody()` возвращает типизированное тело или `null`
- [ ] Query-параметры корректно добавляются в URL
- [ ] Path-параметры корректно добавляются в URL
- [ ] Cookies для хоста добавляются как `Cookie` header
- [ ] `Set-Cookie` заголовки сохраняются в cookie-хранилище
- [ ] `getAllCookies()` возвращает immutable map
- [ ] `clearCookies(host)` удаляет cookies для хоста
- [ ] Network errors бросают `HttpClientExecuteRequestException`
- [ ] Jackson errors бросают `HttpClientHandleResponseException`
- [ ] Каждый Request и Client имеют уникальный UUID

## Examples

### Создание и выполнение GET запроса

```java
HttpClientConfig config = new HttpClientConfig("MyService")
    .setRoot("https://api.example.com");

HttpClient client = builder.build(config, logService, List.of(), List.of());

Request request = Request.builder()
    .setMethod(HttpMethod.GET)
    .setUrl("/api/users")
    .addQueryParam("page", "1")
    .setResponseClass(UserList.class)
    .build();

Response response = client.execute(request);
UserList body = response.getCastBody();
```

### Создание и выполнение POST запроса

```java
Request request = Request.builder()
    .setMethod(HttpMethod.POST)
    .setUrl("/api/users")
    .setContentType(MediaType.APPLICATION_JSON)
    .setPayload(new User("John", 30))
    .setResponseClass(User.class)
    .build();

Response response = client.execute(request);
User created = response.getCastBody();
```

### Создание и выполнение PUT запроса

```java
Request request = Request.builder()
    .setMethod(HttpMethod.PUT)
    .setUrl("/api/users/123")
    .setContentType(MediaType.APPLICATION_JSON)
    .setPayload(new User("John", 31))
    .setResponseClass(User.class)
    .build();

Response response = client.execute(request);
User updated = response.getCastBody();
```

### Создание и выполнение DELETE запроса

```java
Request request = Request.builder()
    .setMethod(HttpMethod.DELETE)
    .setUrl("/api/users/123")
    .setResponseClass(DeleteResult.class)
    .build();

Response response = client.execute(request);
DeleteResult result = response.getCastBody();
```

### Создание и выполнение PATCH запроса

```java
Request request = Request.builder()
    .setMethod(HttpMethod.PATCH)
    .setUrl("/api/users/123")
    .setContentType(MediaType.APPLICATION_JSON)
    .setPayload(Map.of("age", 31))
    .setResponseClass(User.class)
    .build();

Response response = client.execute(request);
User patched = response.getCastBody();
```

### Создание и выполнение HEAD запроса

```java
Request request = Request.builder()
    .setMethod(HttpMethod.HEAD)
    .setUrl("/api/users/123")
    .setResponseClass(null)
    .build();

Response response = client.execute(request);
// response.getBody() == null, но response.getHeaders() содержит заголовки ресурса
```
