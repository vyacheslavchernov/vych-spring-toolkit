---
last_updated: 2026-10-08
scope: testing
---

# Интеграционные тесты

Интеграционные тесты, mock-сервер и покрытие сценариев.

## Mock-сервер

**Технология:** Jersey JAX-RS + Grizzly HTTP Server на порту **9090**.

**Конфигурация:** `TestServerConfiguration` (`ru.vych.http.config`) — `@Bean(initMethod="start", destroyMethod="shutdown")`.

**Spring Boot application:** `App` (`ru.vych.App`) — `@SpringBootApplication`, запускает контекст с сервером.

## Mock-эндпоинты

### GET (`/getTest`)
| URL | Метод | Возврат |
|---|---|---|
| `/getHelloWorld` | GET | 200, `"Hello, World!"` |
| `/getQuery?uuid=...` | GET | 200, эхо query-param |
| `/getManyQuery?...` | GET | 200, мапа query-параметров |
| `/{uuid}` | GET | 200, эхо path-параметра |
| `/getPathNQuery/{uuid}?uuid=...` | GET | 200, `Map.of(key, value)` |
| `/getHeaders` | GET | 200, мапа всех входящих заголовков |

### POST (`/postTest`)
| URL | Метод | Возврат |
|---|---|---|
| `/emptyPost` | POST | 200, пустое тело |
| `/stringPost` | POST | 200, эхо строки |
| `/bytesPost` | POST | 200, эхо байт |
| `/jsonPost` | POST | 200, эхо JSON-тела |

### Cookie (`/cookieTest`)
| URL | Метод | Возврат |
|---|---|---|
| `/set` | GET | 200, устанавливает cookie `test_cookie=test_value_123` |
| `/echo` | GET | 200, все cookies от клиента |
| `/check` | GET | 200, `{"hasCookie": true/false}` |

### Error (`/errorTest`)
| URL | Метод | Возврат |
|---|---|---|
| `/400` | GET | 400, `"Bad Request"` |
| `/404` | GET | 404, `"Not Found"` |
| `/500` | GET | 500, `"Internal Server Error"` |
| `/403` | GET | 403, `{"error":"Forbidden","code":403}` |
| `/401` | GET | 401, `"Unauthorized"` |

### Redirect (`/redirectTest`)
| URL | Метод | Возврат |
|---|---|---|
| `/to-hello` | GET | 302 → `/getTest/getHelloWorld` |
| `/to-hello-301` | GET | 301 → `/getTest/getHelloWorld` |
| `/loop` | GET | 302 → `/redirectTest/loop` (loop) |
| `/external` | GET | 302 → `https://example.com` |
| `/post-redirect` | POST | 302 → `/getTest/getHelloWorld` |

## Тестовые классы

| Класс | Сценарии |
|---|---|
| `HttpClientGetTests` | GET без параметров, с query, path, path+query, headers |
| `HttpClientPostTests` | POST empty, string, bytes, List, Map, DummyDto |
| `HttpClientHeadersTests` | Заголовки из конфига, из запроса, дублирование |
| `HttpClientInterceptorsTests` | Request + Response интерсепторы по UUID |
| `HttpClientNon2xxResponseTests` | 400, 404, 500, 403, 401 |
| `HttpClientCookiePoliciesTests` | ACCEPT_ALL, ACCEPT_NONE, ACCEPT_ORIGINAL_SERVER, pre-set cookies |
| `HttpClientRedirectTests` | 302/301 follow, 302 no follow, loop, external, POST→redirect |

## BaseHttpTest

Базовый класс: `@SpringBootTest(classes = App.class)`, внедряет `@Qualifier("TestServerHttpClient")`.

**Allure Steps:** `checkResponseStatus()`, `sendRequest()`, `bodyEqualsTo()`, `bodyContainsExactlyBytes()`, `bodyContainsExactlyEntriesOf()`, `bodyContainsExactlyElementsOf()`, `bodyContainsEntry()`, `bodyContainsExactlyEntry()`, `requestContainsHeader()`, `responseContainsHeader()`.

## Известные пробелы в покрытии

**Critical:**
- Кастомные хедеры (частично покрыто в `HttpClientHeadersTests`)
- Config headers (покрыто)
- Cookie policies (покрыто)
- Redirect handling (покрыто)
- Network errors (не покрыто)
- Non-2xx responses (покрыто)

**Medium:**
- Error scenarios (частично покрыто)
- Invalid requests (не покрыто)

## Кросс-ссылки

- [testing/overview.md](./overview.md) — обзор тестирования
- [packages/tests/integration.md](../packages/tests/integration.md) — документация тестовых пакетов
- [modules/vych-spring-toolkit-tests.md](../modules/vych-spring-toolkit-tests.md) — документация модуля
