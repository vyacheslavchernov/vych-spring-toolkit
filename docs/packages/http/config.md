---
last_updated: 2026-10-09
scope: package
---

# ru.vych.http.config

Автоконфигурация Spring, builder и конфигурация HTTP-клиента.

## Публичные классы

### `HttpClientConfig`

Контейнер конфигурации HTTP-клиента: root URL, таймауты, дефолтные хедеры, cookie, политика редиректов и версии протокола.

**Ключевые поля:** `serviceCode`, `root`, `timeout` (15s default), `headers`, `cookies`, `cookiePolicy`, `redirectPolicy`, `version` (HTTP/1.1 default), `logRequests` (true default), `cookieStorageDir` (default: `~/.config/vych-spring-toolkit/cookies/`), `cookieStorageEnabled` (true default).

**Lombok:** `@Getter @Setter @Accessors(chain = true) @RequiredArgsConstructor` — `serviceCode` — единственный `final`-параметр конструктора.

**Особенности:** Fluent-конфигурация через chain API. Дублирование заголовков из конфига и запроса (не перезапись). Логирование включено по умолчанию.

### `HttpClientBuilder`

Фабрика (builder-паттерн) для создания настроенных экземпляров `HttpClient`.

**Ключевой метод:** `build(HttpClientConfig, LogService, List<RequestInterceptor>, List<ResponseInterceptor>)` — создаёт `HttpClientImpl`.

**Lombok:** не используется.

### `HttpClientAutoConfiguration`

Spring `@AutoConfiguration`, регистрирующий `HttpClientBuilder` как Spring Bean.

**Ключевой метод:** `@Bean httpClientBuilder()` — создаёт и регистрирует `HttpClientBuilder`.

**Lombok:** не используется.

## Зависимости

- `HttpClientAutoConfiguration` → `HttpClientBuilder`
- `HttpClientBuilder` → `HttpClientConfig`, `LogService`, `RequestInterceptor`, `ResponseInterceptor`, `HttpClient`, `HttpClientImpl`, `HttpClientException`
- `HttpClientConfig` → `CookiesPolicies`, `CookieEntry`

## Связанные пакеты

- [`impl`](./impl.md) — `HttpClientBuilder` создаёт `HttpClientImpl`
- [`impl/common`](./impl/common.md) — `CookiesPolicies`
- [`impl/entities`](./impl/entities.md) — `CookieEntry`
- [`impl/exceptions`](./impl/exceptions.md) — `HttpClientException`
- [`impl/interceptors`](./impl/interceptors.md) — `RequestInterceptor`, `ResponseInterceptor`

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/impl.md](./impl.md) — документация пакета impl
