---
last_updated: 2026-10-10
---

# http-cache-spec — HTTP Client Caching

## Purpose

Кеширование HTTP-запросов в памяти для уменьшения количества повторных запросов к серверу и снижения нагрузки. Кеш управляется на уровне HTTP-клиента, кешируются только запросы с явным флагом `cached`. Поддерживает LRU-eviction при переполнении, глобальный TTL по умолчанию с возможностью переопределения на уровне запроса, уважает `Cache-Control` заголовки от сервера.

## Preconditions

- Spring Boot application с подключённым `http-client-spring-boot-starter`
- HTTP-клиент инициализирован с настроенным кешем (TTL и максимальный размер)
- Logger-модуль (`logger-spring-boot-starter`) доступен в classpath

## Inputs

### HttpClientConfig

| Свойство | Тип | Описание |
|---|---|---|
| `cacheEnabled` | `boolean` | Включено ли кеширование (по умолчанию `false`) |
| `defaultCacheTtlSeconds` | `long` | Глобальный TTL кеша в секундах (по умолчанию `300`) |
| `cacheMaxSize` | `int` | Максимальное количество записей в кеше (по умолчанию `500`) |

### Request (builder)

| Builder-метод | Описание |
|---|---|
| `.setCached(boolean)` | Обязательный для кеширования. Устанавливает флаг кеширования запроса. По умолчанию `false` |
| `.setCacheTtl(long, TimeUnit)` | Опциональный. Переопределяет глобальный TTL для этого конкретного запроса |

### Response (кешированный)

Кешированные и некешированные ответы возвращаются как единый тип `Response`. Источник ответа определяется флагами:

| Поле | Тип | Описание |
|---|---|---|
| `uuid` | `String` | UUID originating request |
| `request` | `Request` | Исходный запрос |
| `status` | `Integer` | HTTP status code |
| `rawBytes` | `byte[]` | Сырое тело ответа |
| `rawBody` | `String` | Тело ответа как UTF-8 строка |
| `body` | `Object` | Десериализованное тело (Jackson) |
| `headers` | `List<Header>` | Заголовки ответа |
| `isCached` | `boolean` | `true` если ответ возвращён из кеша, `false` если получен с сервера |
| `cachedAt` | `Instant` | Время помещения ответа в кеш (только для `isCached=true`, для `isCached=false` = `null`) |

### Cache key

Ключ кеша формируется из:
1. HTTP-метода запроса
2. Полного URL (root + path)
3. Path-параметров (список строк из `request.getPathParams()`)
4. Отсортированных по ключу query-параметров
5. Тела запроса (payload) — сериализованного в byte array

Ключ хэшируется через SHA-256 для фиксированной длины.

## Behavior

### Инициализация кеша

1. При создании HttpClient с `cacheEnabled=true` инициализируется LRU-кеш с容量 `cacheMaxSize`
2. Кеш использует `ConcurrentHashMap` + `LinkedHashMap` для LRU-поведения
3. Каждая запись в кеше содержит: `CachedEntry(value: Response, expiresAt: Instant)`
4. При каждом доступе к кешу запись перемещается в начало LRU-очереди

### Выполнение запроса с кешированием

1. Если `request.isCached() == false` — запрос выполняется без участия кеша (`isCached=false` в Response)
2. Если `request.isCached() == true`:
   1. Формируется cache key из метода, URL, query-параметров и payload
   2. Выполняется поиск ключа в кеше
   3. **Cache hit** — запись найдена и не истекла:
      - Создаётся `CachedResponse` с `isCached=true`, `cachedAt` = время из кеша
      - Все `ResponseInterceptor` выполняются последовательно
      - `CachedResponse` возвращается вызывающему
   4. **Cache miss** — запись не найдена или истекла:
      - Запрос выполняется через стандартный механизм (`HttpClient.send()`)
      - Ответ сериализуется и сохраняется в кеш:
        - TTL определяется так: `request.cacheTtl != null ? request.cacheTtl : config.defaultCacheTtlSeconds`
        - `expiresAt = Instant.now().plusSeconds(ttl)`
        - Ключ = сформированный cache key
        - Значение = `CachedEntry(value: Response, expiresAt: expiresAt)`
      - Если кеш переполнен (`size >= cacheMaxSize`), удаляется самая старая запись (LRU)
      - Создаётся `CachedResponse` с `isCached=false`, `cachedAt = null`
      - Все `ResponseInterceptor` выполняются последовательно
      - `CachedResponse` возвращается вызывающему

### Уважение Cache-Control заголовков

1. После получения ответа с сервера парсятся `Cache-Control` заголовки
2. Если сервер вернул `no-store` — ответ НЕ сохраняется в кеш, даже если `request.isCached() == true`
3. Если сервер вернул `max-age=N` — ответ сохраняется в кеш с TTL = `min(requestTtl, N)` (не более чем указал сервер)
4. `Cache-Control` заголовки НЕ влияют на чтение из кеша, только на запись
5. Если `max-age=0` или `no-cache` — ответ не кэшируется

### Инвалидация по паттерну URL

1. При выполнении WRITE-запроса (POST, PUT, PATCH, DELETE) с `request.isCached() == true`:
   1. Формируется паттерн инвалидации из URL запроса:
      - Точный URL (root + path + path params без query-параметров)
      - Родительский URL (удаляем последний сегмент path)
   2. Из кеша удаляются все записи, ключ которых начинается с точного URL или родительского URL
   3. Пример: `DELETE /api/users/123` инвалидирует записи с ключами, начинающимися с:
      - `/api/users/123` (точный)
      - `/api/users` (родительский)
2. WRITE-запросы с `request.isCached() == false` НЕ инвалидируют кеш
3. READ-запросы (GET, HEAD, OPTIONS) НЕ инвалидируют кеш

### Логирование

1. При cache hit логируется через `LogService.debug()`: `Cache hit: method=url`
2. При cache miss логируется через `LogService.debug()`: `Cache miss: method=url`
3. При инвалидации логируется через `LogService.debug()`: `Cache invalidate: pattern=<url_pattern> count=<removed_count>`

Логирование выполняется в `HttpClientCacheManager`.

## Business rules

- Кеширование **выключено по умолчанию** (`cacheEnabled=false`)
- Запрос кешируется **только** если `request.isCached() == true`
- Кеш хранится **локально в памяти** HTTP-клиента, каждый клиент имеет изолированный кеш
- TTL по умолчанию — `defaultCacheTtlSeconds` из `HttpClientConfig`
- TTL на уровне запроса (`request.cacheTtl`) переопределяет глобальный TTL
- Кеш использует **LRU-eviction** при переполнении (`cacheMaxSize`)
- Ключ кеша включает HTTP-метод, URL, query-параметры и payload
- `Cache-Control: no-store` от сервера запрещает кеширование ответа
- `Cache-Control: max-age=N` ограничивает TTL кеша значением `N`
- WRITE-запросы инвалидируют кеш по паттерну URL (точный + родительский)
- READ-запросы не инвалидируют кеш
- `CachedResponse.isCached=true` означает, что ответ возвращён из ��еша
- `CachedResponse.isCached=false` означает, что ответ получен с сервера
- Истекшие записи (expired) не возвращаются из кеша, но удаляются лениво при доступе

## Errors

| Ситуация | Исключение |
|---|---|
| `cacheMaxSize <= 0` при создании клиента | `HttpClientConfigurationException` |
| `defaultCacheTtlSeconds <= 0` при создании клиента | `HttpClientConfigurationException` |
| `setCacheTtl(0, ...)` или отрицательное значение | `HttpClientInvalidRequestException` (должен бросаться, но не реализован) |
| Ошибка сериализации payload для cache key | `HttpClientHandleResponseException` |

## Acceptance criteria

- [x] `HttpClient` с `cacheEnabled=false` не использует кеш, все запросы выполняются напрямую
- [x] `HttpClient` с `cacheEnabled=true` кеширует GET-запросы с `request.setCached(true)`
- [x] GET-запрос с `request.setCached(false)` не кешируется
- [x] Повторный GET-запрос с тем же ключом возвращает `Response` с `isCached=true`
- [x] Ответ из кеша содержит корректные status, body, headers
- [x] Истекший TTL запись не возвращается из кеша (cache miss)
- [x] Кеш использует глобальный TTL по умолчанию, если `request.cacheTtl` не указан
- [x] `request.setCacheTtl(60, TimeUnit.SECONDS)` переопределяет глобальный TTL
- [x] При переполнении кеша удаляется самая старая запись (LRU)
- [x] `Cache-Control: no-store` от сервера предотвращает сохранение в кеш
- [x] `Cache-Control: max-age=30` ограничивает TTL кеша 30 секундами
- [ ] `Cache-Control: max-age=0` предотвращает сохранение в кеш
- [ ] `Cache-Control: no-cache` предотвращает сохранение в кеш
- [x] WRITE-запрос (POST/PUT/PATCH/DELETE) инвалидирует кеш по паттерну URL
- [ ] `DELETE /api/users/123` инвалидирует кеш для `/api/users/123` и `/api/users`
- [x] READ-запрос (GET) не инвалидирует кеш
- [x] `Response` содержит корректный `isCached` флаг
- [x] `Response.cachedAt` установлен для кешированных ответов и `null` для некешированных
- [x] Каждый Request и Client имеют уникальный UUID (как в `http-client-spec`)
- [x] `HttpClientConfigurationException` бросается при `cacheMaxSize <= 0`
- [x] `HttpClientConfigurationException` бросается при `defaultCacheTtlSeconds <= 0`
- [ ] `HttpClientInvalidRequestException` бросается при `setCacheTtl(0, ...)` или отрицательном значении
- [x] `HttpClientHandleResponseException` бросается при ошибке сериализации payload для cache key

## Examples

### Создание клиента с включённым кешем

```java
HttpClientConfig config = new HttpClientConfig("MyService")
    .setRoot("https://api.example.com")
    .setCacheEnabled(true)
    .setDefaultCacheTtlSeconds(600)
    .setCacheMaxSize(1000);

HttpClient client = builder.build(config, logService, List.of(), List.of());
```

### GET запрос с кешированием

```java
Request request = Request.builder()
    .setMethod(HttpMethod.GET)
    .setUrl("/api/users/123")
    .setCached(true)
    .setResponseClass(User.class)
    .build();

Response response = client.execute(request);
// Первый вызов: isCached=false (получено с сервера, сохранено в кеш)
// Второй вызов: isCached=true (получено из кеша)
User user = response.getCastBody();
```

### GET запрос с переопределением TTL

```java
Request request = Request.builder()
    .setMethod(HttpMethod.GET)
    .setUrl("/api/config")
    .setCached(true)
    .setCacheTtl(30, TimeUnit.SECONDS) // переопределяем глобальный 600s
    .setResponseClass(Config.class)
    .build();

Response response = client.execute(request);
```

### POST запрос с инвалидацией кеша

```java
Request request = Request.builder()
    .setMethod(HttpMethod.POST)
    .setUrl("/api/users")
    .setCached(true) // WRITE-запрос с флагом кеширования инвалидирует кеш
    .setContentType(MediaType.APPLICATION_JSON)
    .setPayload(newUser)
    .setResponseClass(User.class)
    .build();

Response response = client.execute(request);
// Инвалидирует кеш для /api/users и /api (родительские паттерны)
```
