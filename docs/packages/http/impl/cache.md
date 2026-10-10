---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http.impl.cache

Кэширование HTTP-запросов в памяти.

## Публичные классы

### `HttpClientCache`

Внутренний LRU-кеш для HTTP-клиента. Хранит кешированные ответы в памяти с поддержкой TTL, LRU-eviction и генерации SHA-256 ключей из параметров запроса.

**Ключевые методы:** `get(Request)` — поиск записи (cache hit/miss с ленивым удалением истёкших), `put(Request, Response, long)` — сохранение с LRU-eviction при переполнении, `invalidateByUrlPrefix(String)` — инвалидация по паттерну URL, `clear()`, `size()`, `generateCacheKey(Request)` — SHA-256 хеш из метода, URL, query-параметров и payload.

**Структура:** `ConcurrentHashMap<String, CachedEntry>` для хранения записей + `ConcurrentHashMap<String, Long> accessTimestamps` для LRU-поведения (timestamp последнего доступа в nanoTime). Ключ — base64 SHA-256 хеш.

**Lombok:** не используется.

### `HttpClientCacheManager`

Менеджер кэширования HTTP-запросов. Инкапсузирует логику hit/miss, инвалидации, парсинга `Cache-Control` и логирования.

**Ключевые методы:** `executeWithCache(Request)` — выполнение с поддержкой кеша (hit → interceptor; miss → server → cache), `invalidateForWrite(Request)` — инвалидация кеша для WRITE-запросов (точный URL + родительский паттерн).

**Поведение:** уважает `Cache-Control: no-store` (запрет кеша) и `Cache-Control: max-age=N` (ограничение TTL). WRITE-запросы инвалидируют кеш по паттерну URL.

**Lombok:** не используется.

### `HttpClientCacheConfig`

Конфигурация для менеджера кеша. Содержит `LogService`, `serviceCode`, `clientUuid`, `defaultCacheTtlSeconds` и список `ResponseInterceptor`.

**Lombok:** не используется, ручной getter.

## Зависимости

- `HttpClientCache` → `CachedEntry`, `Request`, `HttpClientHandleResponseException`
- `HttpClientCacheManager` → `HttpClientCache`, `HttpClientCacheConfig`, `HttpClient`, `Response`, `ResponseInterceptor`
- `HttpClientCacheConfig` → `LogService`, `ResponseInterceptor`

## Связанные пакеты

- [`impl`](../impl.md) — `HttpClientImpl` использует `HttpClientCacheManager`
- [`impl/entities`](./entities.md) — `CachedEntry`

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/impl.md](../impl.md) — документация пакета impl
