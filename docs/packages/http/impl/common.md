---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http.impl.common

Константы и enum'ы для HTTP-клиента.

## Публичные классы

### `HttpMethod` (enum)

Поддерживаемые HTTP-методы:

| Значение | Описание |
|---|---|
| `GET` | Получение ресурса. Не изменяет состояние сервера. |
| `POST` | Отправка данных на сервер. Может изменять состояние сервера. |
| `PUT` | Полное обновление ресурса. Поддерживает тело запроса. |
| `DELETE` | Удаление ресурса. Поддерживает тело запроса. |
| `PATCH` | Частичное обновление ресурса. Поддерживает тело запроса. |
| `HEAD` | Получение только заголовков ресурса. Тело запроса игнорируется. |
| `OPTIONS` | Получение поддерживаемых методов ресурса. Поддерживает тело запроса. |

Используется как тип поля `method` в `Request`.

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
