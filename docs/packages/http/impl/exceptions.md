---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http.impl.exceptions

Checked-исключения HTTP-клиента.

## Иерархия исключений

```
java.lang.Exception
  └── HttpClientException (корневое checked-исключение)
        ├── HttpClientConfigurationException
        ├── HttpClientExecuteRequestException
        ├── HttpClientHandleResponseException
        └── HttpClientInvalidRequestException
```

## Публичные классы

### `HttpClientException`

Корневое checked-исключение для всех ошибок HTTP-клиента.

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

### `HttpClientConfigurationException`

Ошибка некорректной конфигурации клиента (null-конфиг, null-root, null-cookie-policy, null-cookies, null-logService, некорректная версия/таймаут).

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

### `HttpClientExecuteRequestException`

Ошибка при отправке HTTP-запроса (таймаут, потеря соединения, некорректный URI).

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

### `HttpClientHandleResponseException`

Ошибка десериализации JSON-ответа или сериализации тела POST-запроса (Jackson).

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

### `HttpClientInvalidRequestException`

Ошибка невалидного `Request` (не указан метод, POST с телом без Content-Type).

**Конструкторы:** `(String message)`, `(String message, Throwable cause)`.

### `HttpExceptionsMessages`

Utility-класс с 20 статическими константами сообщений (static import). Сообщения на русском языке.

**Configuration errors (6):** `CREATION_ERROR_CONFIGURATION_IS_NULL`, `CREATION_ERROR_ROOT_IS_NULL`, `CREATION_ERROR_COOKIE_POLICY_IS_NULL`, `CREATION_ERROR_COOKIES_IS_NULL`, `CREATION_ERROR_INVALID_TIMEOUT_OR_VERSION`, `CREATION_ERROR_LOG_SERVICE_IS_NULL`

**Request errors (3):** `REQUEST_ERROR_GENERIC`, `REQUEST_ERROR_INVALID_METHOD`, `REQUEST_ERROR_INVALID_CONTENT_TYPE`

**Execute errors (4):** `EXECUTE_ERROR_TIMEOUT`, `EXECUTE_ERROR_CONNECTION_REFUSED`, `EXECUTE_ERROR_DNS`, `EXECUTE_ERROR_UNKNOWN`

**Response errors (3):** `RESPONSE_ERROR_GENERIC`, `RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION`, `RESPONSE_ERROR_RESPONSE_BODY_DESERIALIZATION`

**Cookie storage errors (4):** `COOKIE_STORAGE_ERROR_READ`, `COOKIE_STORAGE_ERROR_WRITE`, `COOKIE_STORAGE_ERROR_CORRUPTED`, `COOKIE_STORAGE_ERROR_HOSTNAME`

## Точки выброса

| Исключение | Где выбрасывается |
|---|---|
| `HttpClientConfigurationException` | Конструктор `HttpClientImpl` |
| `HttpClientInvalidRequestException` | `Request.Builder.build()` |
| `HttpClientExecuteRequestException` | Приватные `get()` и `post()` при сбое `client.send()` |
| `HttpClientHandleResponseException` | Приватные `buildBody()` (Jackson serialize) и `mapBodyToResponseClass()` (Jackson deserialize) |

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/impl.md](./impl.md) — документация пакета impl
