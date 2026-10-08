---
last_updated: 2026-10-09
scope: package
---

# ru.vych.http.impl

Основные реализации HTTP-клиента.

## Публичные классы

### `HttpClient` (interface)

Контракт HTTP-клиента. Методы: `execute(Request)`, `getCookies(String)`, `getAllCookies()`, `clearCookies(String)`, `getClientUuid()`.

### `HttpClientImpl`

Реализация поверх Java 11 `HttpClient`. Поддерживает GET/POST, кастомные хедеры, cookie-политики, редиректы. UUID-трейсинг для каждого запроса.

**Ключевые методы:** `execute(Request)` — цепочка: request-interceptors → get()/post() → response-interceptors. `buildBody(Request)` — поддержка null/String/byte[]/JSON payload. `buildUri(Request)` — percent-encoding с сохранением `%XX`. `buildResponse(HttpResponse, Request)` — десериализация через Jackson.

**Логирование:** через `HttpClientLogger` (обёртка над `LogService`).

**Особенности:** собственное `ConcurrentHashMap` cookie-хранилище (не глобальный `CookieHandler`). Порядок хедеров: config → request → cookies. **Persistent storage:** cookies сохраняются в JSON-файл при изменении (async auto-save), восстанавливаются при создании клиента. TTL-фильтрация для cookies с `maxAge > 0`. Финальное сохранение при shutdown через shutdown hook. Атомарная запись (tmp file + rename).

### `HttpClientLogger`

Обёртка над `LogService` для условного логирования: info/debug только если `logRequests=true`, error — всегда.

**Ключевые методы:** `info(boolean forced, ...)`, `debug(boolean forced, ...)`, `error(...)`.

**Lombok:** не используется.

## Зависимости

- `HttpClientImpl` → `HttpClientLogger`, `Request.Builder`, `Response`, `HttpClientConfig`, `ObjectMapper`
- `HttpClientLogger` → `LogService` (из logger-модуля)

## Подпакеты

- [`storage`](./impl/storage.md) — `CookieFileStorage` для persistent cookie storage

## Связанные пакеты

- [`config`](./config.md) — `HttpClientBuilder` создаёт `HttpClientImpl`
- [`impl/entities`](./impl/entities.md) — использует `Request` и `Response`
- [`impl/common`](./impl/common.md) — `HttpMethod`, `CookiesPolicies`
- [`impl/exceptions`](./impl/exceptions.md) — checked-исключения
- [`impl/interceptors`](./impl/interceptors.md) — `RequestInterceptor`, `ResponseInterceptor`

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/config.md](./config.md) — документация пакета config
- [packages/http/impl/entities.md](./impl/entities.md) — сущности Request/Response
