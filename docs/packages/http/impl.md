---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http.impl

Основные реализации HTTP-клиента.

## Публичные классы

### `HttpClient` (interface)

Контракт HTTP-клиента. Методы: `execute(Request)`, `getCookies(String)`, `getAllCookies()`, `clearCookies(String)`, `getClientUuid()`.

### `HttpClientImpl`

Реализация поверх Java 11 `HttpClient`. Поддерживает GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, кастомные хедеры, cookie-политики, редиректы. UUID-трейсинг для каждого запроса.

**Ключевые методы:** `execute(Request)` — цепочка: request-interceptors → get()/post() → response-interceptors. `buildBody(Request)` — поддержка null/String/byte[]/JSON payload. `buildUri(Request)` — percent-encoding с сохранением `%XX`. `buildResponse(HttpResponse, Request)` — десериализация через Jackson.

**Логирование:** через `HttpClientLogger` (обёртка над `LogService`).

**Особенности:** собственное `ConcurrentHashMap` cookie-хранилище (не глобальный `CookieHandler`). Порядок хедеров: config → request → cookies. **Persistent storage:** cookies сохраняются в JSON-файл при изменении (async auto-save), восстанавливаются при создании клиента. TTL-фильтрация для cookies с `maxAge > 0`. Финальное сохранение при shutdown через shutdown hook. Атомарная запись (tmp file + rename).

### `HttpClientLogger`

Обёртка над `LogService` для условного логирования: info/debug только если `logRequests=true`, error — всегда.

**Ключевые методы:** `info(boolean forced, ...)`, `debug(boolean forced, ...)`, `error(...)`.

**Lombok:** не используется.

### `HttpClientCache`

Внутренний LRU-кеш для HTTP-клиента. Хранит кешированные ответы в памяти с поддержкой TTL, LRU-eviction и генерации SHA-256 ключей из параметров запроса.

**Ключевые методы:** `get(Request)` — поиск записи (cache hit/miss с ленивым удалением истёкших), `put(Request, Response, long)` — сохранение с LRU-eviction при переполнении, `invalidateByUrlPrefix(String)` — инвалидация по паттерну URL, `clear()`, `size()`, `generateCacheKey(Request)` — SHA-256 хеш из метода, URL, query-параметров и payload.

**Структура:** `ConcurrentHashMap<String, CachedEntry>` для хранения записей + `ConcurrentHashMap<String, Set<String>> urlToCacheKeys` для маппинга URL → keys + `ConcurrentHashMap<String, Long> accessTimestamps` для LRU-поведения (timestamp последнего доступа в nanoTime). Ключ — base64 SHA-256 хеш.

**Генерация ключа:** SHA-256 хеш из HTTP-метода, URL, path-параметров, отсортированных query-параметров и payload. Base64-представление.

**Потокобезопасность:** `ConcurrentHashMap` + обновление `accessTimestamps` при каждом access для LRU-поведения.

**Lombok:** не используется.

### `HttpClientCacheManager`

Менеджер кэширования HTTP-запросов. Инкапсузирует логику hit/miss, инвалидации, парсинга `Cache-Control` и логирования.

**Ключевые методы:** `executeWithCache(Request)` — выполнение с поддержкой кеша (hit → interceptor; miss → server → cache), `invalidateForWrite(Request)` — инвалидация кеша для WRITE-запросов.

**Поведение:** уважает `Cache-Control: no-store` (запрет кеша) и `Cache-Control: max-age=N` (ограничение TTL). WRITE-запросы инвалидируют кеш по точному URL и родительскому паттерну.

**Lombok:** не используется.

### `HttpClientCacheConfig`

Конфигурация для менеджера кеша. Содержит `LogService`, `serviceCode`, `clientUuid`, `defaultCacheTtlSeconds` и список `ResponseInterceptor`.

**Lombok:** не используется, ручной getter.

## Зависимости

- `HttpClientImpl` → `HttpClientLogger`, `Request.Builder`, `Response`, `HttpClientConfig`, `ObjectMapper`
- `HttpClientLogger` → `LogService` (из logger-модуля)

## Подпакеты

- [`storage`](./impl/storage.md) — `CookieFileStorage` для persistent cookie storage
- [`cache`](./impl/cache.md) — `HttpClientCache`, `HttpClientCacheManager`, `HttpClientCacheConfig`

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
