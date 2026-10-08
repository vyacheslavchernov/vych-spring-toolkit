# http-configuration-spec — HTTP Client Configuration

## Purpose

Конфигурация HTTP-клиента: свойства, значения по умолчанию, валидация параметров. Позволяет настроить базовый URL, таймауты, заголовки, cookie и поведение клиента.

## Preconditions

- Spring Boot application с подключённым `http-client-spring-boot-starter`
- `HttpClientBuilder` доступен как Spring bean или создаётся вручную

## Inputs

### HttpClientConfig

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `serviceCode` | `String` | **required** | Идентификатор сервиса для логирования |
| `root` | `String` | `""` | Базовый URL, добавляемый ко всем запросам |
| `timeout` | `Duration` | `PT15S` | Таймаут подключения и ответа |
| `headers` | `Map<String, String>` | `{}` | Заголовки, добавляемые к каждому запросу |
| `cookies` | `List<CookieEntry>` | `[]` | Default cookies для загрузки при инициализации |
| `cookiePolicy` | `CookiesPolicies` | `ACCEPT_ALL` | Политика принятия cookies |
| `redirectPolicy` | `HttpClient.Redirect` | `NORMAL` | Поведение при редиректах |
| `version` | `HttpClient.Version` | `HTTP_1_1` | Версия HTTP протокола |
| `logRequests` | `boolean` | `true` | Включить/выключить логирование запросов/ответов |

### CookiesPolicies

| Значение | Поведение |
|---|---|
| `ACCEPT_ALL` | Принимать все cookies из `Set-Cookie` заголовков |
| `ACCEPT_NONE` | Отклонять все cookies |
| `ACCEPT_ORIGINAL_SERVER` | Принимать cookies только если хост ответа совпадает с хостом root URL |

### CookieEntry

Держит пару `URI` + `HttpCookie` для default cookies.

## Behavior

### Создание конфигурации

1. `HttpClientConfig` создаётся через конструктор с обязательным `serviceCode`
2. Все остальные свойства устанавливаются через fluent-сеттеры (`@Accessors(chain = true)`)
3. `headers` и `cookies` — mutable коллекции, можно добавлять элементы после создания

### Валидация при создании клиента

`HttpClientBuilder.build()` валидирует конфигурацию:

| Проверка | Исключение |
|---|---|
| `config == null` | `HttpClientConfigurationException` |
| `config.getRoot() == null` | `HttpClientConfigurationException` |
| `config.getCookiePolicy() == null` | `HttpClientConfigurationException` |
| `config.getCookies() == null` | `HttpClientConfigurationException` |
| `config.getTimeout()` — невалидное значение | `HttpClientConfigurationException` |
| `config.getVersion()` — невалидное значение | `HttpClientConfigurationException` |

### Применение конфигурации

При создании клиента:
1. `root` URL используется как префикс для всех запросов
2. `timeout` применяется к Java 11 `HttpClient.Builder.connectTimeout()` и `.responseTimeout()`
3. `headers` из config добавляются ко всем outgoing запросам
4. `cookies` из config загружаются в cookie-хранилище при инициализации
5. `cookiePolicy` применяется при обработке `Set-Cookie` заголовков
6. `redirectPolicy` применяется к Java 11 `HttpClient.Builder.followRedirects()`
7. `version` применяется к Java 11 `HttpClient.Builder.version()`
8. `logRequests` управляет поведением `HttpClientLogger`

## Business rules

- `serviceCode` обязателен — без него клиент не создаётся
- `root` URL не может быть `null`, но может быть пустой строкой
- Значения по умолчанию позволяют создать клиента с минимальной конфигурацией
- `headers` из config добавляются к каждому запросу, но могут быть переопределены в `Request`
- Default cookies из config загружаются один раз при создании клиента
- `logRequests=false` отключает debug/info логирование, но `error` логи всегда активны

## Errors

| Ситуация | Исключение |
|---|---|
| `config == null` | `HttpClientConfigurationException` с сообщением `CREATION_ERROR_CONFIGURATION_IS_NULL` |
| `root == null` | `HttpClientConfigurationException` с сообщением `CREATION_ERROR_ROOT_IS_NULL` |
| `cookiePolicy == null` | `HttpClientConfigurationException` с сообщением `CREATION_ERROR_COOKIE_POLICY_IS_NULL` |
| `cookies == null` | `HttpClientConfigurationException` с сообщением `CREATION_ERROR_COOKIES_IS_NULL` |
| Невалидный timeout или version | `HttpClientConfigurationException` с сообщением `CREATION_ERROR_INVALID_TIMEOUT_OR_VERSION` |

## Acceptance criteria

- [ ] `HttpClientConfig` создаётся с обязательным `serviceCode`
- [ ] Все свойства имеют значения по умолчанию, указанные в таблице
- [ ] `HttpClientBuilder.build(null, ...)` бросает `HttpClientConfigurationException`
- [ ] `HttpClientBuilder.build(configWithNullRoot, ...)` бросает `HttpClientConfigurationException`
- [ ] `HttpClientBuilder.build(configWithNullCookiePolicy, ...)` бросает `HttpClientConfigurationException`
- [ ] `HttpClientBuilder.build(configWithNullCookies, ...)` бросает `HttpClientConfigurationException`
- [ ] `root` URL корректно добавляется к URL запросов
- [ ] `headers` из config добавляются ко всем запросам
- [ ] Default cookies из config загружаются при инициализации клиента
- [ ] `cookiePolicy` влияет на обработку `Set-Cookie` заголовков
- [ ] `redirectPolicy` влияет на поведение при HTTP редиректах
- [ ] `version` устанавливает HTTP версию для Java 11 HttpClient
- [ ] `logRequests=false` отключает debug/info логирование

## Examples

### Минимальная конфигурация

```java
HttpClientConfig config = new HttpClientConfig("MyService");
// root = "", timeout = PT15S, headers = {}, cookies = [],
// cookiePolicy = ACCEPT_ALL, redirectPolicy = NORMAL,
// version = HTTP_1_1, logRequests = true
```

### Полная конфигурация

```java
HttpClientConfig config = new HttpClientConfig("UserService")
    .setRoot("https://api.example.com")
    .setTimeout(Duration.ofSeconds(30))
    .setHeaders(Map.of("X-API-Key", "secret"))
    .setCookiePolicy(CookiesPolicies.ACCEPT_ORIGINAL_SERVER)
    .setRedirectPolicy(HttpClient.Redirect.ALWAYS)
    .setVersion(HttpClient.Version.HTTP_2)
    .setLogRequests(false);
```
