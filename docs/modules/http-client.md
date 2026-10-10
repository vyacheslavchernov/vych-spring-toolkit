---
last_updated: 2026-10-10
scope: module
---

# HTTP Client Starter

Spring Boot starter для HTTP-клиента с поддержкой GET, POST, PUT, DELETE, PATCH, HEAD и OPTIONS, интерсепторов, cookie и логирования.

## Назначение

Предоставляет типизированный HTTP-клиент с builder-паттерном, Jackson-десериализацией и UUID-трейсингом.

## Пакеты

- [`config`](../packages/http/config.md) — автоконфигурация Spring, `HttpClientBuilder`, `HttpClientConfig`
- [`impl`](../packages/http/impl.md) — основные реализации: `HttpClient`, `HttpClientImpl`, `HttpClientLogger`, `HttpClientCache`, `HttpClientCacheManager`
- [`impl/storage`](../packages/http/impl/storage.md) — `CookieFileStorage` для persistent cookie storage
- [`impl/common`](../packages/http/impl/common.md) — enum'ы (`HttpMethod`, `CookiesPolicies`), константы статуса и media-type
- [`impl/entities`](../packages/http/impl/entities.md) — сущности: `Request`, `Response`, `CookieEntry`, `Header`, `SerializedCookie`, `CachedEntry`
- [`impl/exceptions`](../packages/http/impl/exceptions.md) — checked-исключения: `HttpClientException` + 4 сабкласса
- [`impl/interceptors`](../packages/http/impl/interceptors.md) — функциональные интерфейсы интерсепторов
- [`impl/cache`](../packages/http/impl/cache.md) — кэширование: `HttpClientCache` (LRU), `HttpClientCacheManager`, `HttpClientCacheConfig`

## Зависимости

- **Compile**: `spring-boot-starter`, `logger-spring-boot-starter`, `lombok`, `jackson-databind`
- **Test**: `junit-jupiter`, `mockito-core`, `mockito-junit-jupiter`, `assertj-core`

## Кэширование

Модуль поддерживает LRU-кеш HTTP-запросов в памяти с поддержкой TTL, инвалидации по паттерну URL и уважения `Cache-Control` заголовков.

**Ключевые возможности:**
- **LRU-кеш** — `HttpClientCache` хранит ответы с `ConcurrentHashMap` + LRU-eviction при переполнении
- **SHA-256 ключи** — cache key формируется из HTTP-метода, URL, query-параметров и payload
- **TTL** — глобальный `defaultCacheTtlSeconds` с переопределением на уровне запроса через `request.cacheTtl`
- **Cache-Control** — уважает `no-store` (запрет кеша) и `max-age=N` (ограничение TTL)
- **Инвалидация** — WRITE-запросы (POST/PUT/PATCH/DELETE) инвалидируют кеш по паттерну URL
- **Потокобезопасность** — `ConcurrentHashMap` для синхронизированного доступа

**Конфигурация:** `HttpClientConfig` — `cacheEnabled`, `defaultCacheTtlSeconds`, `cacheMaxSize`.
**API запроса:** `Request.builder().setCached(true).setCacheTtl(60, TimeUnit.SECONDS)`.
**Результат:** `Response.isCached` / `Response.cachedAt` — определение источника ответа.

## Тесты

Юнит-тесты (14 классов) в `http-client-spring-boot-starter/src/test/`:
- `HttpClientImplTests` — создание клиента, GET/POST, десериализация, построение URI
- `HttpClientCacheTests` — LRU-кеш: store/retrieve, TTL, eviction, invalidation, cache key generation
- `HttpClientCacheManagerTests` — менеджер кеша: hit/miss, Cache-Control, interceptor integration
- `entities/ResponseTests` — factory-методы `of()`/`cached()`, casting, serialization
- `ResponseTests` — response body casting (parameterized)
- `HttpClientCookieStoreTests` — cookie storage, isolation, policies
- `HttpClientPersistentCookieTests` — persistent cookie storage интеграция
- `CookieFileStorageTests` — файловое хранилище: save/load, TTL-фильтрация, атомарная запись
- `HttpClientLoggerTests` — logging behavior (info/debug/error with logRequests)
- `RequestBuilderTests` — request validation, builder pattern
- `HttpClientInterceptorExceptionTests` — interceptor exception handling
- `HttpClientMethodValidationTests` — валидация Content-Type для PUT, DELETE, PATCH, OPTIONS, HEAD
- `HttpMethodTests` — проверка всех 7 HTTP-методов в enum

Интеграционные тесты (12 классов) в `vych-spring-toolkit-tests` проверяют работу с mock-сервером (Jersey/Grizzly на :9090):
- `HttpClientGetTests` — GET без параметров, с query, path, path+query, headers
- `HttpClientPostTests` — POST empty, string, bytes, List, Map, DummyDto
- `HttpClientPutTests` — PUT с payload
- `HttpClientDeleteTests` — DELETE с телом
- `HttpClientPatchTests` — PATCH с payload
- `HttpClientHeadTests` — HEAD запросы (только заголовки)
- `HttpClientOptionsTests` — OPTIONS запросы
- `HttpClientHeadersTests` — заголовки из конфига, из запроса, дублирование
- `HttpClientInterceptorsTests` — Request + Response интерсепторы по UUID
- `HttpClientNon2xxResponseTests` — 400, 404, 500, 403, 401
- `HttpClientCookiePoliciesTests` — ACCEPT_ALL, ACCEPT_NONE, ACCEPT_ORIGINAL_SERVER, pre-set cookies
- `HttpClientRedirectTests` — 302/301 follow, 302 no follow, loop, external, POST→redirect

## Кросс-ссылки

- [modules/README.md](README.md) — обзор модулей
- [packages/http/config.md](../packages/http/config.md) — документация пакета config
- [packages/http/impl.md](../packages/http/impl.md) — документация пакета impl
