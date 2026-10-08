# http-error-handling-spec — HTTP Client Error Handling

## Purpose

Обработка ошибок HTTP-клиента через систему checked-исключений. Все исключения checked, сгруппированы по типам для точной обработки.

## Preconditions

- HTTP-клиент создан и работает корректно
- Вызовущий код обрабатывает `HttpClientException` или его подтипы

## Inputs

### Иерархия исключений

```
Exception (checked)
  └── HttpClientException (checked)
       ├── HttpClientConfigurationException
       ├── HttpClientInvalidRequestException
       ├── HttpClientExecuteRequestException
       └── HttpClientHandleResponseException
```

### HttpClientException

Базовое checked-исключение для всех ошибок HTTP-клиента.

| Конструктор | Описание |
|---|---|
| `(String message)` | Создаёт исключение с сообщением |
| `(String message, Throwable cause)` | Создаёт исключение с сообщением и cause |

### HttpClientConfigurationException

Ошибка валидации конфигурации при создании клиента.

| Ситуация | Сообщение |
|---|---|
| `config == null` | `CREATION_ERROR_CONFIGURATION_IS_NULL` |
| `root == null` | `CREATION_ERROR_ROOT_IS_NULL` |
| `cookiePolicy == null` | `CREATION_ERROR_COOKIE_POLICY_IS_NULL` |
| `cookies == null` | `CREATION_ERROR_COOKIES_IS_NULL` |
| Невалидный timeout или version | `CREATION_ERROR_INVALID_TIMEOUT_OR_VERSION` |

### HttpClientInvalidRequestException

Ошибка валидации запроса при создании `Request`.

| Ситуация | Сообщение |
|---|---|
| `Request` без установленного метода | `REQUEST_ERROR_INVALID_METHOD` |
| `POST` с payload, но без `Content-Type` | `REQUEST_ERROR_INVALID_CONTENT_TYPE` |

### HttpClientExecuteRequestException

Ошибка на уровне сети при отправке запроса.

| Ситуация | Сообщение |
|---|---|
| Таймаут подключения/ответа | `EXECUTE_ERROR_TIMEOUT` |
| Недоступность сервера | `EXECUTE_ERROR_CONNECTION_REFUSED` |
| DNS ошибка | `EXECUTE_ERROR_DNS` |
| Другая сетевая ошибка | `EXECUTE_ERROR_UNKNOWN` |

### HttpClientHandleResponseException

Ошибка сериализации/десериализации.

| Ситуация | Сообщение |
|---|---|
| Jackson ошибка при сериализации request body | `RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION` |
| Jackson ошибка при десериализации response body | `RESPONSE_ERROR_RESPONSE_BODY_DESERIALIZATION` |

## Behavior

### Валидация конфигурации

1. `HttpClientBuilder.build()` вызывает валидацию конфигурации
2. Если валидация не пройдена — бросается `HttpClientConfigurationException`
3. Клиент **не создаётся** — bean не регистрируется в Spring

### Валидация запроса

1. `Request.Builder.build()` валидирует обязательные поля
2. Если валидация не пройдена — бросается `HttpClientInvalidRequestException`
3. `Request` **не создаётся** — исключение пробрасывается вызывающему

### Выполнение запроса

1. При отправке запроса через Java 11 `HttpClient.send()`:
   - `ConnectTimeoutException` → `HttpClientExecuteRequestException`
   - `IOException` → `HttpClientExecuteRequestException`
   - Другая `RuntimeException` → `HttpClientExecuteRequestException`
2. Исключение пробрасывается через `execute()`

### Обработка ответа

1. При сериализации request body:
   - `JsonProcessingException` → `HttpClientHandleResponseException`
2. При десериализации response body:
   - `JsonProcessingException` → `HttpClientHandleResponseException`
3. Исключение пробрасывается через `execute()`

### Interceptor exceptions

1. Исключение из `RequestInterceptor` пробрасывается через `execute()`
2. Исключение из `ResponseInterceptor` пробрасывается через `execute()`
3. Исключения интерсепторов **не оборачиваются** в `HttpClientException`

## Business rules

- Все исключения HTTP-клиента — **checked** (расширяют `Exception`)
- Вызывающий код **обязан** обрабатывать `HttpClientException` или его подтипы
- Иерархия исключенийflat — нет вложенности подтипов дальше одного уровня
- Все сообщения исключений централизованы в `HttpExceptionsMessages`
- Каждый exception имеет два конструктора: `(message)` и `(message, cause)`

## Acceptance criteria

- [ ] `HttpClientException` — checked exception
- [ ] Все подтипы — checked exceptions
- [ ] `HttpClientConfigurationException` бросается при невалидной конфигурации
- [ ] `HttpClientInvalidRequestException` бросается при невалидном запросе
- [ ] `HttpClientExecuteRequestException` бросается при сетевых ошибках
- [ ] `HttpClientHandleResponseException` бросается при Jackson ошибках
- [ ] Исключения интерсепторов не оборачиваются
- [ ] Все исключения имеют два конструктора: `(message)` и `(message, cause)`
- [ ] Сообщения исключений централизованы в `HttpExceptionsMessages`

## Examples

### Обработка конфигурационной ошибки

```java
try {
    HttpClient client = builder.build(null, logService, List.of(), List.of());
} catch (HttpClientConfigurationException e) {
    // config == null
}
```

### Обработка ошибки запроса

```java
try {
    Request request = Request.builder()
        .setUrl("/api/users")
        .build();
} catch (HttpClientInvalidRequestException e) {
    // method == null
}
```

### Обработка сетевой ошибки

```java
try {
    Response response = client.execute(request);
} catch (HttpClientExecuteRequestException e) {
    // timeout, connection refused, DNS error
}
```

### Обработка ошибки десериализации

```java
try {
    Response response = client.execute(request);
} catch (HttpClientHandleResponseException e) {
    // Jackson deserialization error
}
```
