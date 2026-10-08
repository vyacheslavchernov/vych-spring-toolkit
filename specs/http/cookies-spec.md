---
last_updated: 2026-10-09
scope: specification
---

# http-cookies-spec — HTTP Client Cookies

## Purpose

Изолированное cookie-хранилище для HTTP-клиента с поддержкой default cookies, cookie policies, управления по хостам и **персистентного сохранения кук между запусками приложения**. Каждый экземпляр клиента имеет собственное cookie-хранилище без использования глобального `CookieHandler`. Куки сохраняются в локальный файл и восстанавливаются при следующем запуске клиента с тем же `serviceCode` + hostname.

## Preconditions

- HTTP-клиент создан с валидной конфигурацией
- Cookie-хранилище инициализируется при создании клиента
- `CookiesPolicies` определён в конфигурации
- Приложение запущено на машине с определяемым hostname
- Файловая система доступна для чтения/записи cookie-файла

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

### Persistent Cookie Storage

| Свойство | Тип | Описание |
|---|---|---|
| `filePath` | `Path` | Путь к файлу для сохранения кук. Формируется как `<cookie-storage-dir>/<serviceCode>-<hostname>.cookies` |
| `cookieStorageDir` | `Path` | Каталог для cookie-файлов. По умолчанию: `~/.config/vych-spring-toolkit/cookies/` |
| `enabled` | `boolean` | Включено/выключено. По умолчанию: `true` |

## Behavior

### Инициализация cookie-хранилища

1. При создании клиента cookie-хранилище инициализируется как пустой `ConcurrentHashMap`
2. Default cookies из `config.getCookies()` загружаются в хранилище:
   - Для каждого `CookieEntry` cookie добавляется в хранилище по ключу `entry.getUri().getHost()`
   - Если хост уже есть в хранилище — cookie добавляется в существующий список
3. Определяется `serviceCode` из конфигурации и hostname машины
4. Формируется путь к cookie-файлу: `<cookieStorageDir>/<serviceCode>-<hostname>.cookies`
5. Если persistent storage включён (`enabled = true`):
   - Проверяется существование cookie-файла
   - Если файл существует — куки загружаются из файла и добавляются в cookie-хранилище
   - При загрузке куки с истёкшим TTL **отфильтровываются** и не добавляются в хранилище
   - Загруженные куки с истёкшим TTL логируются в debug
   - Если файл не существует — создаётся пустое хранилище
6. Регистрируется shutdown hook для финального сохранения кук при остановке приложения

### Загрузка кук из файла

1. Файл читается как JSON-объект
2. Десериализуется структура с cookies по хостам
3. Для каждого cookie проверяется `maxAge` / `expires`:
   - Если TTL истёк — cookie пропускается (не добавляется в хранилище)
   - Если TTL не истёк — cookie добавляется в cookie-хранилище
4. После загрузки cookie-хранилище содержит как default cookies из config, так и восстановленные из файла

### Сохранение кук в файл

#### Автоматическое сохранение

1. После **каждого** изменения cookie-хранилища (добавление cookie при получении `Set-Cookie`, удаление, clear) выполняется автосохранение:
   - Cookie-хранилище сериализуется в JSON
   - Записывается во временный файл в том же каталоге с суффиксом `.tmp`
   - Временный файл атомарно переименовывается в целевой файл (rename)
   - Сериализация и запись происходят в фоновом потоке (async), чтобы не блокировать HTTP-запросы
   - При ошибке записи — логирование в error, cookie-хранилище в памяти остаётся корректным

#### Финальное сохранение (Shutdown Hook)

1. При остановке приложения (JVM shutdown hook) выполняется финальное сохранение:
   - Cookie-хранилище сериализуется и записывается в файл
   - Запись выполняется синхронно, с ожиданием завершения
   - Если запись не удалась — логирование в error

### Отправка cookies

1. Перед отправкой запроса cookie-хранилище проверяется на наличие cookies для хоста запроса
2. Если cookies найдены — они фильтруются по TTL (просроченные пропускаются)
3. Оставшиеся cookies объединяются и добавляются как `Cookie` header
4. Формат: `Cookie: name1=value1; name2=value2`

### Получение cookies

1. После получения ответа `Set-Cookie` заголовки парсятся
2. Парсинг зависит от `cookiePolicy`:
   - `ACCEPT_ALL` — все cookies сохраняются
   - `ACCEPT_NONE` — cookies игнорируются
   - `ACCEPT_ORIGINAL_SERVER` — cookies сохраняются только если хост ответа совпадает с хостом root URL
3. Сохранённые cookies добавляются в cookie-хранилище по ключу хоста
4. После успешного добавления запускается автосохранение в файл

### API cookie-хранилища

| Метод | Поведение |
|---|---|
| `getCookies(String host)` | Возвращает копию списка cookies для хоста с фильтрацией по TTL (просроченные куки не возвращаются) |
| `getAllCookies()` | Возвращает **immutable** map всех cookies |
| `clearCookies(String host)` | Удаляет все cookies для хоста, запускает автосохранение |

## TTL (Time-To-Live)

### Источники TTL

TTL cookie определяется из следующих источников (в порядке приоритета):

| Источник | Поле | Описание |
|---|---|---|
| `maxAge` (секунды) | `HttpCookie.getMaxAge()` | Время жизни в секундах с момента установки |
| `expires` | `Set-Cookie` header | Дата истечения (HTTP-date формат) |
| Отсутствует | — | Cookie является session-cookie (живёт до конца сессии, **не сохраняется** в файл) |

### Правила TTL

- Cookie с `maxAge <= 0` считается истёкшим немедленно
- Cookie с `maxAge > 0` имеет TTL = `maxAge` секунд от момента установки
- Cookie без `maxAge` и без `expires` — session cookie, не сохраняется в persistent storage
- При сериализации в файл для cookie с `maxAge > 0` сохраняется:
  - `name`, `value`, `domain`, `path`, `secure`, `httpOnly`
  - `maxAge` (исходное значение)
  - `createdAt` — timestamp (epoch millis) момента первого получения/сохранения
- При десериализации из файла TTL проверяется как: `createdAt + maxAge * 1000 > currentMillis`

### Удаление просроченных кук

1. **При загрузке из файла:** просроченные куки не добавляются в cookie-хранилище, логируются в debug
2. **При отправке запроса:** перед добавлением cookies в `Cookie` header просроченные куки пропускаются
3. **При явном удалении:** `clearCookies(host)` удаляет все куки для хоста независимо от TTL

## Business rules

- Cookie-хранилище полностью изолировано — нет глобального состояния
- `getAllCookies()` возвращает **immutable** map — попытки модификации бросают `UnsupportedOperationException`
- `getCookies(host)` возвращает **копию** списка — модификация возвращённого списка не влияет на хранилище
- `clearCookies(host)` удаляет все cookies для хоста, но не для других хостов
- Default cookies загружаются один раз при создании клиента и не обновляются автоматически
- Cookie-хранилище потокобезопасно благодаря `ConcurrentHashMap` и `CopyOnWriteArrayList`
- Persistent storage использует **serviceCode + hostname** как уникальный ключ cookie-файла
- Каждый клиент с одинаковым `serviceCode` на одной и той же машине использует **один и тот же** cookie-файл
- Session cookies (без `maxAge` и `expires`) **не сохраняются** в файл
- Автосохранение выполняется асинхронно и не блокирует HTTP-запросы
- Финальное сохранение при shutdown выполняется синхронно
- Атомарная запись файла через временный файл + rename
- При ошибке сохранения в файл — логирование в error, cookie-хранилище в памяти остаётся корректным
- Просроченные куки фильтруются при загрузке из файла и при отправке запросов

## Errors

| Ситуация | Исключение / Поведение |
|---|---|
| `host == null` в `getCookies()` | `NullPointerException` с сообщением `"Host cannot be null in getCookies()"` |
| `host == null` в `clearCookies()` | `NullPointerException` с сообщением `"Host cannot be null in clearCookies()"` |
| `Set-Cookie` с невалидным форматом | Cookie игнорируется |
| Хост ответа не может быть определён | Cookie игнорируется |
| Cookie-файл не существует при загрузке | Ничего — используется пустое хранилище |
| Cookie-файл повреждён (невалидный JSON) | Файл игнорируется, логирование в error, используется пустое хранилище |
| Ошибка записи cookie-файла | Логирование в error, cookie-хранилище в памяти остаётся корректным |
| Ошибка чтения cookie-файла | Логирование в error, cookie-хранилище в памяти остаётся корректным |
| Невозможно определить hostname | Persistent storage отключается, используется только memory storage |

## Acceptance criteria

- [ ] Каждый клиент имеет собственное cookie-хранилище в памяти
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
- [ ] При создании клиента с существующим cookie-файлом куки загружаются из файла
- [ ] При создании клиента с несуществующим cookie-файлом используется пустое хранилище
- [ ] Session cookies (без `maxAge`/`expires`) не сохраняются в файл
- [ ] Cookies с `maxAge > 0` сохраняются в файл с `createdAt` timestamp
- [ ] При загрузке из файла куки с истёкшим TTL отфильтровываются
- [ ] При отправке запроса просроченные куки не добавляются в `Cookie` header
- [ ] Автосохранение выполняется асинхронно после каждого изменения cookie-хранилища
- [ ] Финальное сохранение выполняется при shutdown приложения
- [ ] Запись в файл атомарная (tmp file + rename)
- [ ] При ошибке сохранения cookie-хранилище в памяти остаётся корректным
- [ ] Повреждённый cookie-файл не ломает клиент (игнорируется, логгируется error)
- [ ] Клиент с `serviceCode=A` на машине `host1` и клиент с `serviceCode=A` на машине `host2` используют разные cookie-файлы

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

### Persistent storage: сценарий между запусками

```
Запуск 1 (hostname: "workstation-1", serviceCode: "AuthService"):
  1. Клиент создан, cookie-файл ~/.config/vych-spring-toolkit/cookies/AuthService-workstation-1.cookies не существует
  2. Выполняется GET /api/login, сервер возвращает Set-Cookie: "auth_token=xyz; Max-Age=3600"
  3. Cookie сохраняется в cookie-хранилище: {"api.example.com": [auth_token=xyz (createdAt=1728000000000)]}
  4. Автосохранение в файл (async)
  5. Приложение остановлено, финальное сохранение (sync)

Запуск 2 (hostname: "workstation-1", serviceCode: "AuthService"):
  1. Клиент создан, cookie-файл ~/.config/vych-spring-toolkit/cookies/AuthService-workstation-1.cookies существует
  2. Куки загружаются из файла: auth_token=xyz (createdAt=1728000000000)
  3. Проверяется TTL: createdAt + 3600*1000 > currentMillis?
     - Если да — cookie добавляется в хранилище
     - Если нет — cookie пропускается, логируется в debug
  4. Клиент может продолжать использовать восстановленные куки
```

### Формат cookie-файла (JSON)

```json
{
  "version": 1,
  "savedAt": 1728000001000,
  "hosts": {
    "api.example.com": [
      {
        "name": "auth_token",
        "value": "xyz",
        "domain": "api.example.com",
        "path": "/",
        "secure": false,
        "httpOnly": true,
        "maxAge": 3600,
        "createdAt": 1728000000000
      }
    ],
    "cdn.example.com": [
      {
        "name": "session_id",
        "value": "abc",
        "domain": "cdn.example.com",
        "path": "/",
        "secure": true,
        "httpOnly": false,
        "maxAge": 1800,
        "createdAt": 1728000000500
      }
    ]
  }
}
```
