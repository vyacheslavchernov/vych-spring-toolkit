---
last_updated: 2026-10-08
scope: module
---

# Tests Module

Интеграционные тесты для проверки работы http-client starter с mock-сервером.

## Назначение

Spring Boot приложение с встроенным Jersey/Grizzly mock-сервером на порту **9090**. Предоставляет набор интеграционных тестов для проверки GET/POST запросов, cookie-политик, редиректов и интерсепторов.

## Пакеты

- [`ru.vych`](../packages/tests/main.md) — `App` (Spring Boot main), `RandomUtils` (test utilities)
- [`ru.vych.http.config`](../packages/tests/integration.md) — `TestServerConfiguration`, `TestServerDefaultClientConfiguration`, `TestServerCookieClientConfiguration`, `TestServerRedirectClientConfiguration`, `ExceptionHandler`
- [`ru.vych.http.controllers`](../packages/tests/integration.md) — `GetTestController` (5 GET endpoints), `PostTestController` (4 POST endpoints), `CookieTestController`, `ErrorTestController`, `RedirectTestController`
- [`ru.vych.http.entities`](../packages/tests/integration.md) — `DummyDto` (test DTO)
- [`ru.vych.http.interceptors`](../packages/tests/integration.md) — `CustomRequestInterceptor`, `CustomResponseInterceptor` (test interceptors)
- [`http`](../packages/tests/integration.md) — `BaseHttpTest`, `HttpClientGetTests`, `HttpClientPostTests`, `HttpClientHeadersTests`, `HttpClientInterceptorsTests`, `HttpClientNon2xxResponseTests`, `HttpClientCookiePoliciesTests`, `HttpClientRedirectTests`

## Mock-сервер

| Путь | Метод | Описание |
|---|---|---|
| `/getTest/*` | GET | 5 GET endpoint'ов для тестирования параметров |
| `/postTest/*` | POST | 4 POST endpoint'а для тестирования payload |
| `/cookieTest/*` | GET | 3 endpoint'а для тестирования cookie-политик |
| `/errorTest/*` | GET | 5 endpoint'ов для тестирования non-2xx ответов |
| `/redirectTest/*` | GET/POST | 5 endpoint'ов для тестирования редиректов |

## Зависимости

- **Runtime**: `spring-boot-starter`, `http-client-spring-boot-starter`
- **Test**: `spring-boot-starter-test`, `jersey-container-grizzly2-http:3.1.8`, `jersey-media-json-jackson:3.1.8`, `jakarta.ws.rs-api:3.1.0`, `assertj-core:3.27.3`, `aspectjweaver:1.9.22.1`, `allure-junit5:2.29.1`

## Кросс-ссылки

- [modules/README.md](README.md) — обзор модулей
- [packages/tests/main.md](../packages/tests/main.md) — production-код тестов
- [packages/tests/integration.md](../packages/tests/integration.md) — интеграционные тесты
- [testing/integration-tests.md](../testing/integration-tests.md) — документация по интеграционным тестам
