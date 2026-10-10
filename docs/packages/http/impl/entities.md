---
last_updated: 2026-10-10
scope: package
---

# ru.vych.http.impl.entities

Сущности HTTP-клиента: Request, Response, CachedEntry, CookieEntry, Header.

## Публичные классы

### `Request`

Описание HTTP-запроса: URL, метод, query/path-параметры, заголовки, тело (payload), флаг кеширования и TTL.

**Поля:** `uuid` (final, UUID.randomUUID()), `url`, `method` (обязательный), `queryParams`, `pathParams`, `headers`, `responseClass`, `payload`, `cached` (флаг кеширования), `cacheTtl` (переопределение глобального TTL).

**Lombok:** `@Getter @AllArgsConstructor @Accessors(chain = true) @ToString @EqualsAndHashCode`.

**Вложенный класс `Builder`:** `setQueryParams()`, `addQueryParam()`, `addPathParam()`, `addHeader()`, `setContentType()`, `setCached(boolean)`, `setCacheTtl(long, TimeUnit)`, `build()` (валидация: method ≠ null; для POST, PUT, DELETE, PATCH, OPTIONS с payload → Content-Type обязателен; HEAD игнорирует тело).

**Особенности:** `@ToString`/`@EqualsAndHashCode` включают `uuid` — два запроса с одинаковыми параметрами, но разными UUID, **не равны**. `responseClass`: null/byte/byte[] → десериализация пропускается; String → raw string; иначе → Jackson.

### `Response`

Результат HTTP-запроса: статус, тело в трёх форматах (raw bytes, raw string, десериализованный object), заголовки, ссылка на запрос, флаг кеширования и время помещения в кеш.

**Поля:** `uuid`, `request`, `status`, `rawBytes`, `rawBody`, `body`, `headers`, `isCached` (флаг: true если из кеша), `cachedAt` (время помещения в кеш).

**Lombok:** `@Getter @ToString @EqualsAndHashCode` на уровне класса, `@Setter` на каждом поле (`status`, `rawBytes`, `rawBody`, `body`, `headers`, `isCached`, `cachedAt`).

**Factory-методы:** `of(String, Request, Integer, Object, List<Header>)` — создание ответа с сервера (`isCached=false`); `cached(Response, String, Request, Instant)` — создание ответа из кеша (`isCached=true`).

**Метод `getCastedBody()`:** `@JsonIgnore`, приведение `body` к `request.getResponseClass()` через `Class.cast()`.

### `CookieEntry`

DTO для хранения пары `URI` + `HttpCookie`. Используется для инициализации дефолтных cookies в конфиге.

**Lombok:** `@Getter @Setter @Accessors(chain = true) @AllArgsConstructor`.

### `Header` (record)

HTTP-заголовок — пара «имя → значение». Используется как элемент `List<Header>` в `Request` и `Response`.

**Компоненты:** `name` (String), `value` (String). Record自带 `equals`/`hashCode`/`toString`.

### `CachedEntry`

Запись в кеше HTTP-клиента. Содержит результат HTTP-запроса (`Response`), время помещения в кеш и время истечения (`Instant`).

**Поля:** `response` (кешированный ответ), `cachedAt` (время помещения в кеш), `expiresAt` (время истечения).

**Методы:** `isExpired()` — проверка истечения TTL (`Instant.now().isAfter(expiresAt)`).

**Lombok:** `@Getter @ToString @EqualsAndHashCode`, ручной constructor.

### `SerializedCookie`

DTO для JSON-сериализации HTTP-cookie в persistent storage. Хранит атрибуты cookie плюс `createdAt` для TTL-валидации.

**Поля:** `name`, `value`, `domain`, `path`, `secure`, `httpOnly`, `maxAge`, `createdAt`.

**Методы:** `fromHttpCookie(HttpCookie)` — создание из `HttpCookie`; `toHttpCookie()` — восстановление; `hasTtl()` — проверка наличия TTL; `isNotExpired()` — проверка TTL.

**Lombok:** не используется, `@JsonCreator` + `@JsonProperty` для Jackson.

## Зависимости

- `Request` → `HttpMethod`, `Header`, `HttpClientInvalidRequestException`, `HttpExceptionsMessages`
- `Response` → `Request`, `Header`
- `CachedEntry` → `Response`
- `CookieEntry` → `java.net.URI`, `java.net.HttpCookie`

## Связанные пакеты

- [`impl`](./impl.md) — `HttpClientImpl` использует `Request` и `Response`; `HttpClientCache` использует `CachedEntry`
- [`impl/common`](./impl/common.md) — `HttpMethod`
- [`impl/exceptions`](./impl/exceptions.md) — `HttpClientInvalidRequestException`

## Кросс-ссылки

- [modules/http-client.md](../../modules/http-client.md) — документация модуля
- [packages/http/impl.md](./impl.md) — документация пакета impl
