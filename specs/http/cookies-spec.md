# http-cookies-spec — HTTP Client Cookies

## Purpose

Изолированное cookie-хранилище для HTTP-клиента с поддержкой default cookies, cookie policies и управления по хостам. Каждый экземпляр клиента имеет собственное cookie-хранилище без использования глобального `CookieHandler`.

## Preconditions

- HTTP-клиент создан с валидной конфигурацией
- Cookie-хранилище инициализируется при создании клиента
- `CookiesPolicies` определён в конфигурации

## Inputs

### Cookie-хранилище

- Тип: `ConcurrentHashMap<String, CopyOnWriteArrayList<HttpCookie>>`
- Ключ: хост (domain)
- Значение: список cookies для хоста
- Полностью изолировано — каждый `HttpClient` имеет собственное хранилище

### CookiesPolicies

| Значение | Поведение при получении `Set-Cookie` |
|---|---|
| `ACCEPT_ALL` | Принимать все cookies из `Set-Cookie` заголовков |
| `ACCEPT_NONE` | Отклонять все cookies |
| `ACCEPT_ORIGINAL_SERVER` | Принимать только если хост ответа совпадает с хостом root URL |

### CookieEntry

| Поле | Тип | Описание |
|---|---|---|
| `uri` | `URI` | URI для cookie |
| `cookie` | `HttpCookie` | Cookie объект |

## Behavior

### Инициализация cookie-хранилища

1. При создании клиента cookie-хранилище инициализируется как пустой `ConcurrentHashMap`
2. Default cookies из `config.getCookies()` загружаются в хранилище:
   - Для каждого `CookieEntry` cookie добавляется в хранилище по ключу `entry.getUri().getHost()`
   - Если хост уже есть в хранилище — cookie добавляется в существующий список

### Отправка cookies

1. Перед отправкой запроса cookie-хранилище проверяется на наличие cookies для хоста запроса
2. Если cookies найдены — они объединяются и добавляются как `Cookie` header
3. Формат: `Cookie: name1=value1; name2=value2`

### Получение cookies

1. После получения ответа `Set-Cookie` заголовки парсятся
2. Парсинг зависит от `cookiePolicy`:
   - `ACCEPT_ALL` — все cookies сохраняются
   - `ACCEPT_NONE` — cookies игнорируются
   - `ACCEPT_ORIGINAL_SERVER` — cookies сохраняются только если хост ответа совпадает с хостом root URL
3. Сохранённые cookies добавляются в cookie-хранилище по ключу хоста

### API cookie-хранилища

| Метод | Поведение |
|---|---|
| `getCookies(String host)` | Возвращает копию списка cookies для хоста |
| `getAllCookies()` | Возвращает **immutable** map всех cookies |
| `clearCookies(String host)` | Удаляет все cookies для хоста |

## Business rules

- Cookie-хранилище полностью изолировано — нет глобального состояния
- `getAllCookies()` возвращает **immutable** map — попытки модификации бросают `UnsupportedOperationException`
- `getCookies(host)` возвращает **копию** списка — модификация возвращённого списка не влияет на хранилище
- `clearCookies(host)` удаляет все cookies для хоста, но не для других хостов
- Default cookies загружаются один раз при создании клиента и не обновляются автоматически
- Cookie-хранилище потокобезопасно благодаря `ConcurrentHashMap` и `CopyOnWriteArrayList`

## Errors

| Ситуация | Поведение |
|---|---|
| `host == null` в `getCookies()` | Поведение не определено (NEEDS_CLARIFICATION) |
| `host == null` в `clearCookies()` | Поведение не определено (NEEDS_CLARIFICATION) |
| `Set-Cookie` с невалидным форматом | Cookie игнорируется |
| Хост ответа не может быть определён | Cookie игнорируется |

## Acceptance criteria

- [ ] Каждый клиент имеет собственное cookie-хранилище
- [ ] Default cookies из config загружаются при инициализации
- [ ] Cookies для хоста добавляются как `Cookie` header при запросе
- [ ] `Set-Cookie` заголовки сохраняются в cookie-хранилище
- [ ] `ACCEPT_ALL` принимает все cookies
- [ ] `ACCEPT_NONE` отклоняет все cookies
- [ ] `ACCEPT_ORIGINAL_SERVER` принимает только cookies от root host
- [ ] `getCookies(host)` возвращает копию списка
- [ ] `getAllCookies()` возвращает immutable map
- [ ] `clearCookies(host)` удаляет cookies только для указанного хоста
- [ ] Cookie-хранилище потокобезопасно

## Examples

### Default cookies

```java
CookieEntry entry = new CookieEntry(
    URI.create("https://api.example.com"),
    new HttpCookie("session", "abc123")
);

HttpClientConfig config = new HttpClientConfig("MyService")
    .setRoot("https://api.example.com")
    .setCookies(List.of(entry));

// При создании клиента "session=abc123" загружается в cookie-хранилище
// для хоста "api.example.com"
```

### Cookie policies

```java
// ACCEPT_ALL — принимает все Set-Cookie
HttpClientConfig config1 = new HttpClientConfig("S")
    .setCookiePolicy(CookiesPolicies.ACCEPT_ALL);

// ACCEPT_NONE — игнорирует все Set-Cookie
HttpClientConfig config2 = new HttpClientConfig("S")
    .setCookiePolicy(CookiesPolicies.ACCEPT_NONE);

// ACCEPT_ORIGINAL_SERVER — принимает только от api.example.com
HttpClientConfig config3 = new HttpClientConfig("S")
    .setRoot("https://api.example.com")
    .setCookiePolicy(CookiesPolicies.ACCEPT_ORIGINAL_SERVER);
```
