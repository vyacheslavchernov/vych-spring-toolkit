---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http + http (интеграционные тесты)

Интеграционные тесты, конфигурация mock-сервера, контроллеры и тестовые сущности.

## ru.vych.http.config

### `TestServerConfiguration`

Конфигурация встроенного JAX-RS/Grizzly сервера на порту **9090**.

**Метод:** `@Bean(testServer())` — `@Bean(initMethod="start", destroyMethod="shutdown")` создаёт Grizzly HttpServer.

**Константа:** `TEST_SERVER_URI = "http://localhost:9090"`.

**Регистрирует контроллеры:** `CookieTestController`, `ErrorTestController`, `GetTestController`, `PostTestController`, `RedirectTestController`, `JacksonFeature`, `ExceptionHandler`.

### `TestServerDefaultClientConfiguration`

Конфигурация HttpClient по умолчанию с заголовками из конфига и интерсепторами.

**Метод:** `@Primary @Bean defaultClient()` — клиент с корнем `TEST_SERVER_URI`, таймаутом 2 сек, заголовком `X-Config-Header: config-value`.

**Константа:** `DEFAULT_CLIENT_SERVICE_CODE = "TestServerHttpClient"`.

### `TestServerCookieClientConfiguration`

Конфигурация 5 HttpClient с разными cookie-политиками: `ACCEPT_ALL`, `ACCEPT_NONE`, `ACCEPT_ORIGINAL_SERVER`, `ACCEPT_ALL` с pre-set cookie, `ACCEPT_NONE` с pre-set cookie.

### `TestServerRedirectClientConfiguration`

Конфигурация 2 HttpClient с разными политиками редиректов: `Redirect.NORMAL` (follow), `Redirect.NEVER` (no follow).

### `ExceptionHandler` (@Provider)

JAX-RS `ExceptionMapper<Throwable>` — глобальный обработчик исключений сервера. Извлекает HTTP-код через regex `HTTP\d{3}` (default 500), возвращает `Response.status(status).entity(className + ": message")`.

## ru.vych.http.controllers

### `GetTestController` (`@Path("/getTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/getHelloWorld` | GET | 200, `"Hello, World!"` |
| `/getQuery?uuid=...` | GET | 200, эхо query-param |
| `/getManyQuery?...` | GET | 200, мапа всех query-параметров |
| `/{uuid}` | GET | 200, эхо path-параметра |
| `/getPathNQuery/{uuid}?uuid=...` | GET | 200, `Map.of(key, value)` |
| `/getHeaders` | GET | 200, мапа всех входящих заголовков |

### `PostTestController` (`@Path("/postTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/emptyPost` | POST | 200, пустое тело |
| `/stringPost` | POST | 200, эхо строки |
| `/bytesPost` | POST | 200, эхо байт |
| `/jsonPost` | POST | 200, эхо JSON-тела |

### `CookieTestController` (`@Path("/cookieTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/set` | GET | 200, устанавливает cookie `test_cookie=test_value_123` |
| `/echo` | GET | 200, возвращает все cookies от клиента |
| `/check` | GET | 200, `{"hasCookie": true/false}` |

### `ErrorTestController` (`@Path("/errorTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/400` | GET | 400, `"Bad Request"` |
| `/404` | GET | 404, `"Not Found"` |
| `/500` | GET | 500, `"Internal Server Error"` |
| `/403` | GET | 403, `{"error":"Forbidden","code":403}` |
| `/401` | GET | 401, `"Unauthorized"` |

### `RedirectTestController` (`@Path("/redirectTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/to-hello` | GET | 302 → `/getTest/getHelloWorld` |
| `/to-hello-301` | GET | 301 → `/getTest/getHelloWorld` |
| `/loop` | GET | 302 → `/redirectTest/loop` (зацикливание) |
| `/external` | GET | 302 → `https://example.com` |
| `/post-redirect` | POST | 302 → `/getTest/getHelloWorld` |

### `PutTestController` (`@Path("/putTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/putString` | PUT | 200, эхо строки |
| `/putJson` | PUT | 200, эхо JSON-тела |
| `/putBytes` | PUT | 200, эхо байт |

### `DeleteTestController` (`@Path("/deleteTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/deleteWithBody` | DELETE | 200, эхо тела запроса |
| `/deleteWithPath` | DELETE | 200, эхо path-параметра |

### `PatchTestController` (`@Path("/patchTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/patchJson` | PATCH | 200, эхо JSON-тела |
| `/patchString` | PATCH | 200, эхо строки |

### `HeadTestController` (`@Path("/headTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/headHelloWorld` | HEAD | 200, только заголовки |
| `/headWithPath` | HEAD | 200, только заголовки с path-параметром |

### `OptionsTestController` (`@Path("/optionsTest")`)

| URL | Метод | Возврат |
|---|---|---|
| `/optionsHelloWorld` | OPTIONS | 200, эхо тела запроса |
| `/optionsWithPath` | OPTIONS | 200, эхо path-параметра и тела |

## ru.vych.http.entities

### `DummyDto`

DTO для тестирования сериализации/десериализации JSON.

**Поля:** `value` (String), `listValue` (List<String>), `mapValue` (Map<String,String>).

**Метод:** `static getDummy()` — фабричный метод с рандомными данными через `RandomUtils`.

**Lombok:** `@Getter @Setter @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode @ToString`.

## ru.vych.http.interceptors

### `CustomRequestInterceptor`

Тестируемый `RequestInterceptor` — добавляет заголовок `Custom-Rq-Header-Name: Custom-Rq-Header-Value` к запросу, если активирован по UUID.

**Методы:** `enable(String uuid)`, `disable()` (статические), `handle(HttpClient, Request)`.

**Нюанс:** использует статические переменные `enabled` и `interceptByUuid` — состояние глобальное.

### `CustomResponseInterceptor`

Аналогичен `CustomRequestInterceptor`, но для `ResponseInterceptor` — добавляет заголовок `Custom-Rs-Header-Name: Custom-Rs-Header-Value`.

## http (flat package — интеграционные тесты)

### `BaseHttpTest` (@SpringBootTest(classes = App.class))

Базовый класс для всех HTTP-тестов. Внедряет `defaultClient` (`@Qualifier("TestServerHttpClient")`).

**Allure Steps:** `checkResponseStatus()`, `sendRequest()`, `bodyEqualsTo()`, `bodyContainsExactlyBytes()`, `bodyContainsExactlyEntriesOf()`, `bodyContainsExactlyElementsOf()`, `bodyContainsEntry()`, `bodyContainsExactlyEntry()`, `requestContainsHeader()`, `responseContainsHeader()`.

### Тестовые классы

| Класс | Сценарии |
|---|---|
| `HttpClientGetTests` | GET без параметров, с query, path, path+query, headers |
| `HttpClientPostTests` | POST empty, string, bytes, List, Map, DummyDto |
| `HttpClientHeadersTests` | Заголовки из конфига, из запроса, дублирование |
| `HttpClientInterceptorsTests` | Request + Response интерсепторы по UUID |
| `HttpClientNon2xxResponseTests` | 400, 404, 500, 403, 401 |
| `HttpClientCookiePoliciesTests` | ACCEPT_ALL, ACCEPT_NONE, ACCEPT_ORIGINAL_SERVER, pre-set cookies |
| `HttpClientRedirectTests` | 302/301 follow, 302 no follow, loop, external, POST→redirect |

## Кросс-ссылки

- [modules/vych-spring-toolkit-tests.md](../../modules/vych-spring-toolkit-tests.md) — документация модуля
- [packages/tests/main.md](./main.md) — production-код тестов
- [testing/integration-tests.md](../../testing/integration-tests.md) — документация по интеграционным тестам
