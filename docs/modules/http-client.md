---
last_updated: 2026-10-09
scope: module
---

# HTTP Client Starter

Spring Boot starter для HTTP-клиента с поддержкой GET/POST, интерсепторов, cookie и логирования.

## Назначение

Предоставляет типизированный HTTP-клиент с builder-паттерном, Jackson-десериализацией и UUID-трейсингом.

## Пакеты

- [`config`](../packages/http/config.md) — автоконфигурация Spring, `HttpClientBuilder`, `HttpClientConfig`
- [`impl`](../packages/http/impl.md) — основные реализации: `HttpClient`, `HttpClientImpl`, `HttpClientLogger`
- [`impl/common`](../packages/http/impl/common.md) — enum'ы (`HttpMethod`, `CookiesPolicies`), константы статуса и media-type
- [`impl/entities`](../packages/http/impl/entities.md) — сущности: `Request`, `Response`, `CookieEntry`, `Header`
- [`impl/exceptions`](../packages/http/impl/exceptions.md) — checked-исключения: `HttpClientException` + 4 сабкласса
- [`impl/interceptors`](../packages/http/impl/interceptors.md) — функциональные интерфейсы интерсепторов

## Зависимости

- **Compile**: `spring-boot-starter`, `logger-spring-boot-starter`, `lombok`, `jackson-databind`
- **Test**: `junit-jupiter`, `mockito-core`, `mockito-junit-jupiter`, `assertj-core`

## Тесты

Юнит-тесты (6 классов) в `http-client-spring-boot-starter/src/test/`:
- `HttpClientImplTests` — создание клиента, GET/POST, десериализация, построение URI
- `HttpClientCookieStoreTests` — cookie storage, isolation, policies
- `HttpClientLoggerTests` — logging behavior (info/debug/error with logRequests)
- `RequestBuilderTests` — request validation, builder pattern
- `ResponseTests` — response body casting
- `HttpClientInterceptorExceptionTests` — interceptor exception handling

Интеграционные тесты (7 классов) в `vych-spring-toolkit-tests` проверяют работу с mock-сервером (Jersey/Grizzly на :9090).

## Кросс-ссылки

- [modules/README.md](README.md) — обзор модулей
- [packages/http/config.md](../packages/http/config.md) — документация пакета config
- [packages/http/impl.md](../packages/http/impl.md) — документация пакета impl
