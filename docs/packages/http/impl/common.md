---
last_updated: 2026-10-09
scope: package
---

# ru.vych.http.impl.common

Константы и enum'ы для HTTP-клиента.

## Публичные классы

### `HttpMethod` (enum)

Поддерживаемые HTTP-методы: `GET` (получение ресурса), `POST` (отправка данных). Другие методы могут быть добавлены в будущих версиях. Используется как тип поля `method` в `Request`.

### `HttpStatus` (final class)

Класс-контейнер со всеми стандартными HTTP статус-кодами (1xx–5xx) в виде `public static final int`. Покрытие включает IANA HTTP Status Code Registry, WebDAV, RFC 2324, RFC 8470. Тип `int` позволяет сравнивать напрямую со `Response.status`.

### `MediaType` (final class)

Класс-контейнер со стандартными MIME-типами (application, text, image, audio/video, multipart, font, wildcard). Тип `String` — MIME-типы представляют собой строковые значения. Используется для `Content-Type` в запросах.

### `CookiesPolicies` (enum)

Политики приёма cookies: `ACCEPT_ALL` (все домены), `ACCEPT_NONE` (никакие), `ACCEPT_ORIGINAL_SERVER` (same-origin). Используется при настройке `CookieHandler` в `HttpClientConfig`.

## Зависимости

- `HttpMethod` → `HttpClient`, `Request.getMethod()`
- `HttpStatus` → `Response.getStatus()`
- `MediaType` → `Request.Builder`, `Header`
- `CookiesPolicies` → `java.net.CookiePolicy` (Javadoc reference)

## Связанные пакеты

- [`impl`](./impl.md) — `HttpClientImpl` использует `HttpMethod`, `CookiesPolicies`
- [`impl/entities`](./entities.md) — `Request` использует `HttpMethod`, `MediaType`

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/impl.md](./impl.md) — документация пакета impl
