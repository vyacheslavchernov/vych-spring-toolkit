---
last_updated: 2026-10-10
---

# Правила проверки coding standards

Полный список правил из `docs/coding-standards.md` с примерами корректного и некорректного кода.

---

## 1. Naming Conventions

### Пакеты
- ✅ `ru.vych.http.impl.entities`
- ❌ `ru.vych.HttpClient.Entities` (PascalCase)

### Классы и интерфейсы
- ✅ `HttpClientImpl`, `LogService`, `RequestInterceptor` (PascalCase)
- ✅ Interface: `HttpClient`, `LogAppender`, `RequestInterceptor`
- ✅ Class: `HttpClientImpl`, `HttpClientConfig`, `LogProperties`
- ✅ Record: `Header(String name, String value)` — только data-carriers с 2 полями
- ✅ Enum: `HttpMethod`, `CookiesPolicies`, `LoggingLevel`

### Методы и переменные
- ✅ `execute()`, `build()`, `addQueryParam()`, `getCastedBody()` (camelCase)
- ✅ Factory: `builder()` (static), `create()` (static)
- ✅ Локальные переменные: `var` предпочтительно
- ✅ Неизменяемые поля: `private final String serviceCode`

### Константы
- ✅ `RESET`, `OK`, `APPLICATION_JSON` (UPPER_SNAKE_CASE)
- ✅ `CREATION_ERROR_CONFIGURATION_IS_NULL` (сообщения исключений)
- ✅ `public final static String SERVICE_CODE = "LoggerService"`

---

## 2. Структура класса

### Порядок элементов
```java
public class Example {
    // 1. static final константы
    private static final String CONSTANT = "value";
    
    // 2. Поля (сначала final)
    private final String name;
    private int count;
    
    // 3. Конструктор(ы)
    public Example(String name) {
        this.name = name;
    }
    
    // 4. Публичные методы
    public void doSomething() { }
    
    // 5. Приватные/protected методы
    private void helper() { }
    
    // 6. Вложенные классы
    public static class Builder { }
}
```

### Порядок импортов
1. Third-party (`org.springframework.*`, `lombok.*`)
2. Java стандартные (`java.util.*`, `java.io.*`)
3. Статические импорты (`import static ...`)

---

## 3. Lombok

| Аннотация | Где использовать | Пример |
|---|---|---|
| `@Getter` / `@Setter` | Раздельно, `@Setter` только на мутабельных | `@Getter @Setter private int count;` |
| `@RequiredArgsConstructor` | Классы с `final` полями | `HttpClientConfig`, `ConsoleAppender` |
| `@AllArgsConstructor` | Entity-классы | `Request`, `Response`, `LogEvent` |
| `@NoArgsConstructor` | DTO в тестах | Test DTOs |
| `@Accessors(chain = true)` | Fluent API | `HttpClientConfig`, `Request` |
| `@ToString` / `@EqualsAndHashCode` | Entity-классы | `Request`, `Response` |

**НЕ использовать `@Builder`** — вместо него кастомный inner class `Builder`.

---

## 4. Builder Pattern

```java
// ✅ Правильно
public class Request {
    private final HttpMethod method;
    private final String url;
    
    public static Request builder() {
        return new Request.Builder().build();
    }
    
    public static class Builder {
        private HttpMethod method;
        private String url;
        
        @Accessors(chain = true)
        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }
        
        public Request build() throws HttpClientInvalidRequestException {
            if (method == null) {
                throw new HttpClientInvalidRequestException(REQUEST_ERROR_INVALID_METHOD);
            }
            return new Request(method, url);
        }
    }
}

// ❌ Неправильно — @Builder из Lombok
@Builder
public class Request { }
```

---

## 5. Spring Boot

```java
// ✅ Правильно
@AutoConfiguration
public class HttpClientAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient(HttpClientConfig config) {
        return new HttpClientImpl(config);
    }
}

@ConfigurationProperties(prefix = "vych.http")
public class HttpClientConfig { }

// ❌ Неправильно — @Configuration вместо @AutoConfiguration
@Configuration
public class HttpClientConfiguration { }
```

---

## 6. Исключения

```java
// ✅ Правильно
public class HttpClientConfigurationException extends Exception {
    public HttpClientConfigurationException(String message) {
        super(message);
    }
    
    public HttpClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Сообщения централизованы
final class HttpExceptionsMessages {
    static final String CREATION_ERROR_CONFIGURATION_IS_NULL = "Configuration is null";
    static final String REQUEST_ERROR_INVALID_METHOD = "Method is invalid";
}

// ❌ Неправильно — unchecked exception
public class HttpClientException extends RuntimeException { }

// ❌ Неправильно — сообщения разбросаны по коду
throw new Exception("Configuration is null");
```

---

## 7. Логирование

```java
// ✅ Правильно — LogService
logService.debug(serviceCode, uuid, "Processing request: {}", request);

// ✅ Правильно — System.out/err для аппендеров
System.out.println("[FileAppender] Initialized. Log file: " + logFilePath);
System.err.println("[FileAppender] Failed to close file: " + e.getMessage());

// ❌ Неправильно — SLF4J/Logback
log.info("Processing request");
logger.debug("Error occurred");
```

---

## 8. Форматирование

```java
// ✅ K&R style
if (condition) {
    doSomething();
}

// ✅ Switch arrow
switch (level) {
    case DEBUG -> log.debug(...);
    case INFO  -> log.info(...);
}

// ✅ Pattern matching instanceof
if (payload instanceof String text) {
    return text.toUpperCase();
}

// ✅ List.of() / Map.of()
var headers = Map.of("Content-Type", "application/json");
var list = List.of("a", "b", "c");

// ❌ Arrays.asList() / new HashMap<>()
var headers = Arrays.asList(...);
var map = new HashMap<String, String>();

// ✅ @Override на каждом методе интерфейса
@Override
public Response execute(Request request) { }

// ❌ @Override пропущен
public Response execute(Request request) { }
```

---

## 9. JavaDoc

```java
// ✅ Обязателен для публичных
/**
 * Executes HTTP request and returns response.
 *
 * @param request request to execute
 * @return response from server
 * @throws HttpClientExecuteRequestException if execution fails
 * @throws HttpClientInvalidRequestException if request is invalid
 * @see Response
 */
public Response execute(Request request) throws HttpClientExecuteRequestException { }

// ❌ Неправильно — нет @throws
/**
 * Executes request.
 *
 * @param request request to execute
 * @return response from server
 */
public Response execute(Request request) throws Exception { }

// ❌ Неправильно — нет JavaDoc на публичном
public Response execute(Request request) { }
```

---

## 10. Тесты

```java
// ✅ Правильно
@DisplayName("HttpClientImpl tests")
class HttpClientImplTests {
    
    @DisplayName("Execute request should return response")
    @Test
    void executeRequestReturnsResponse() {
        assertThat(response.getStatus())
            .describedAs("Status code should be 200")
            .isEqualTo(200);
    }
    
    @DisplayName("Build without method should throw exception")
    @Test
    @SneakyThrows
    void buildWithoutMethodThrows() {
        assertThatThrownBy(() -> builder.build())
            .isInstanceOf(HttpClientInvalidRequestException.class)
            .describedAs("Exception on build without method");
    }
}

// ❌ Неправильно — нет @DisplayName
@Test
void test1() { }

// ❌ Неправильно — throws в сигнатуре теста
@Test
void testThrows() throws Exception { }  // вместо @SneakyThrows

// ❌ Неправильно — нет .describedAs()
assertThat(status).isEqualTo(200);
```

---

## 11. Статические импорты

```java
// ✅ Правильно
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.*;

// ❌ Неправильно — прямое обращение
Assertions.assertThat(...);
HttpExceptionsMessages.CREATION_ERROR_CONFIGURATION_IS_NULL;
```
