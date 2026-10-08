---
last_updated: 2026-10-08
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

**Конструкторы:** `(String message)` — **без конструктора с `Throwable cause`** (аномалия в иерархии).

### `HttpExceptionsMessages`

Utility-класс с 12 статическими константами сообщений (static import). Сообщения на русском языке.

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
