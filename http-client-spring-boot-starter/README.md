# Vych Spring Toolkit — HTTP Client

Spring Boot Starter для работы с HTTP-клиентом.

## Назначение

Предоставляет готовую конфигурацию HTTP-клиента с поддержкой:

* builder-паттерна для настройки клиента;
* chain API для построения запросов;
* interceptors для запросов и ответов;
* автоматической регистрации через Spring Boot AutoConfiguration;
* интеграции с logger-spring-boot-starter.

## Подключение

Добавьте BOM и зависимость в `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>ru.vych</groupId>
            <artifactId>vych-spring-toolkit-bom</artifactId>
            <version>0.0.5-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.vych</groupId>
        <artifactId>http-client-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

После подключения стартер автоматически зарегистрирует `HttpClientBuilder` как Spring Bean. Ничего настраивать дополнительно не нужно.

## Использование

### Вариант 1. Клиент внутри класса

Создавайте клиент прямо в сервисе, куда инжектите `HttpClientBuilder` и `LogService`:

```java
import org.springframework.stereotype.Service;
import ru.vych.http.config.HttpClientBuilder;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.common.HttpMethod;

@Service
public class MyService {
    private final HttpClient httpClient;

    public MyService(HttpClientBuilder builder, LogService logService) {
        HttpClientConfig config = new HttpClientConfig("MyService");
        config.setRoot("https://api.example.com");
        config.setTimeout(10000);
        this.httpClient = builder.build(config, logService, List.of(), List.of());
    }

    public String getData() throws Exception {
        Request request = Request.builder()
                .method(HttpMethod.GET)
                .url("/api/data")
                .addQueryParam("page", "1")
                .responseClass(MyResponse.class)
                .build();

        Response response = httpClient.execute(request);
        return response.getCastedBody();
    }
}
```

### Вариант 2. Клиент как Spring Bean

Создайте клиент как бин и инжектируйте его напрямую:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.config.HttpClientBuilder;
import ru.vych.http.impl.HttpClient;

@Configuration
public class HttpClientConfig {
    private final LogService logService;

    public HttpClientConfig(LogService logService) {
        this.logService = logService;
    }

    @Bean
    public HttpClient httpClient(HttpClientBuilder builder) {
        HttpClientConfig config = new HttpClientConfig("MyService")
                .setRoot("https://api.example.com")
                .setTimeout(10000)
                .setAllowRedirects(true);

        return builder.build(config, logService, List.of(), List.of());
    }
}
```

Теперь в любом сервисе просто инжектите `HttpClient`:

```java
@Service
public class MyService {
    private final HttpClient httpClient;

    public MyService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getData() throws Exception {
        Request request = Request.builder()
                .method(HttpMethod.GET)
                .url("/api/data")
                .responseClass(MyResponse.class)
                .build();

        Response response = httpClient.execute(request);
        return response.getCastedBody();
    }
}
```

### Построение запроса

```java
Request request = Request.builder()
        .method(HttpMethod.POST)
        .url("/api/users")
        .addQueryParam("verbose", "true")
        .addPathParam("42")                           // /api/users/42
        .addHeader("X-Custom-Header", "value")
        .contentType("application/json")
        .payload(new User("John", 30))
        .responseClass(User.class)
        .build();
```

### Результат выполнения

```java
Response response = httpClient.execute(request);

// Статус-код
int status = response.getStatus();

// Сырое тело (строка)
String body = response.getRawBody();

// Десериализованное тело
MyResponse data = response.getCastedBody();

// Заголовки ответа
List<Header> headers = response.getHeaders();
```

## Конфигурация

### HttpClientConfig

| Свойство | Тип | По умолчанию | Описание |
|---|---|---|---|
| `serviceCode` | `String` | *(обязательно)* | Код сервиса для логирования |
| `root` | `String` | `""` | Корневой URL (base URL) для всех запросов |
| `timeout` | `int` | `15000` | Тайм-аут в миллисекундах |
| `headers` | `Map<String, String>` | `{}` | Дефолтные заголовки для всех запросов |
| `cookies` | `Map<String, String>` | `{}` | Дефолтные cookie, устанавливаемые при инициализации |
| `storeCookies` | `boolean` | `false` | Сохранять cookie из ответов |
| `cookieHandlerClass` | `Class<? extends CookieHandler>` | `CookieManager.class` | Класс обработчика cookie |
| `allowRedirects` | `boolean` | `false` | Автоматическое следование за редиректами |
| `version` | `HttpClient.Version` | `HTTP_1_1` | Версия HTTP-протокола |

## Константы

В модуле доступны классы-константы для удобства работы:

* `HttpMethod` — HTTP-методы: `GET`, `POST`
* `HttpStatus` — все стандартные HTTP статус-коды: `OK`, `NOT_FOUND`, `INTERNAL_SERVER_ERROR` и т. д.
* `MediaType` — MIME-типы: `APPLICATION_JSON`, `TEXT_PLAIN`, `MULTIPART_FORM_DATA` и т. д.

```java
Request request = Request.builder()
        .method(HttpMethod.POST)
        .contentType(MediaType.APPLICATION_JSON)
        .build();

if (response.getStatus() == HttpStatus.OK) {
    // ...
}
```

## Перехватчики (Interceptors)

Interceptors позволяют перехватывать запросы и ответы для добавления общей логики — аутентификации, логирования, валидации, метрик.

### Как работают

* **RequestInterceptor** — вызывается **перед** отправкой каждого запроса. Может модифицировать `Request` (заголовки, payload) или выполнять побочные действия.
* **ResponseInterceptor** — вызывается **после** получения ответа. Может анализировать `Response` (статус-код, тело, заголовки) или выполнять побочные действия.
* Все interceptors вызываются **последовательно** в порядке добавления в списки.
* Если interceptor выбрасывает исключение, выполнение цепочки прерывается, и запрос не будет отправлен.

### Шаг 1. Реализация интерфейса

```java
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;

// Перехватчик запросов — добавляет токен авторизации
public class AuthInterceptor implements RequestInterceptor {
    @Override
    public void handle(HttpClient client, Request request) {
        String token = getToken();
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
    }

    private String getToken() {
        // Получение токена из хранилища
        return "...";
    }
}

// Перехватчик ответов — логирует ошибки
public class ErrorLoggingInterceptor implements ResponseInterceptor {
    @Override
    public void handle(HttpClient client, Response response) {
        if (response.getStatus() >= 400) {
            System.err.println("[ERROR] " + response.getRequest().getMethod() + " "
                    + response.getRequest().getUrl() + " -> " + response.getStatus());
            System.err.println(response.getRawBody());
        }
    }
}
```

### Шаг 2. Регистрация как Spring Bean

Зарегистрируйте interceptors как `List<RequestInterceptor>` и `List<ResponseInterceptor>`:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class InterceptorConfig {
    @Bean
    public List<RequestInterceptor> requestInterceptors() {
        return List.of(new AuthInterceptor(), new LoggingInterceptor());
    }

    @Bean
    public List<ResponseInterceptor> responseInterceptors() {
        return List.of(new ErrorLoggingInterceptor());
    }
}
```

Зарегистрированные interceptors автоматически передаются в `HttpClientBuilder.build()` и применяются ко всем запросам.

### Примеры использования

| Сценарий | Интерсептор | Что делает |
|---|---|---|
| Аутентификация | `RequestInterceptor` | Добавляет `Authorization` заголовок |
| Логирование запросов | `RequestInterceptor` | Записывает URL, метод, заголовки в лог |
| Метрики (тайминги) | `RequestInterceptor` + `ResponseInterceptor` | Замеряет время выполнения запроса |
| Валидация ответов | `ResponseInterceptor` | Проверяет статус-код и тело ответа |
| Retry-логика | `ResponseInterceptor` | Повторяет запрос при 5xx |
| Добавление заголовков | `RequestInterceptor` | Добавляет `X-Request-Id`, `Content-Type` |
