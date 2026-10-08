---
last_updated: 2026-10-09
scope: architecture
---

# Coding Standards & Conventions

Принятые в проекте конвенции написания кода, стили и паттерны. Применяются ко всем модулям: `http-client-spring-boot-starter`, `logger-spring-boot-starter`, `vych-spring-toolkit-tests`.

> **Важно:** В проекте **нет** автоматических линтеров. Все правила enforced вручную или через IDE-настройки.

---

## 1. Naming Conventions

### Пакеты
- Нижний регистр: `ru.vych.http.impl.entities`
- Подпакеты по смыслу: `config`, `common`, `entities`, `exceptions`, `interceptors`, `checkdata`, `providers`

### Классы и интерфейсы
- **PascalCase**: `HttpClientImpl`, `LogService`, `RequestInterceptor`
- **Interface** — для абстракций: `HttpClient`, `LogAppender`, `RequestInterceptor`, `ResponseInterceptor`, `LogFilter`
- **Class** — для реализаций: `HttpClientImpl`, `HttpClientConfig`, `LogProperties`
- **Record** — для простых data-carriers с двумя полями: `Header(String name, String value)`
- **Enum** — для ограниченного набора значений: `HttpMethod`, `CookiesPolicies`, `LoggingLevel`

### Методы и переменные
- **camelCase**: `execute()`, `build()`, `addQueryParam()`, `getCastedBody()`
- Методы билдера: префикс `set` + имя поля: `setQueryParams()`, `addPathParam()`
- Factory-методы: `builder()` (статический), `create()` (статический)
- **`var`** — предпочтительно для локальных переменных
- **`final`** — для неизменяемых полей: `private final String serviceCode`

### Константы
- **UPPER_SNAKE_CASE** для значений: `RESET`, `OK`, `APPLICATION_JSON`
- **UPPER_SNAKE_CASE** для сообщений исключений: `CREATION_ERROR_CONFIGURATION_IS_NULL`
- **`public final static String`** для `SERVICE_CODE`: `"LoggerService"`, `"ConsoleAppender"`

---

## 2. Структура класса

### Порядок элементов (сверху вниз)
1. `static final` константы
2. Поля (сначала `final`, потом обычные)
3. Конструктор(ы)
4. Публичные методы
5. Приватные/protected методы
6. Вложенные классы

### Порядок импортов
1. Java стандартные → 2. Third-party → 3. Статические импорты

---

## 3. Lombok

| Аннотация | Где |
|---|---|
| `@Getter` / `@Setter` | Раздельно, `@Setter` только на мутабельных полях |
| `@RequiredArgsConstructor` | Классы с `final` полями: `HttpClientConfig`, `ConsoleAppender` |
| `@AllArgsConstructor` | Entity-классы: `Request`, `Response`, `LogEvent`, `CookieEntry` |
| `@NoArgsConstructor` | DTO в тестах |
| `@Accessors(chain = true)` | Fluent API: `HttpClientConfig`, `Request`, `Request.Builder`, `CookieEntry` |
| `@ToString` / `@EqualsAndHashCode` | Entity-классы |

**НЕ используется `@Builder`** — вместо него кастомный inner class `Builder`.

---

## 4. Builder Pattern

- Кастомный inner class `Builder` внутри entity-класса (`Request.Builder`)
- Статический factory-метод `builder()`
- `@Accessors(chain = true)` — все сеттеры возвращают `this`
- Валидация в методе `build()`:
  ```java
  public Request build() throws HttpClientInvalidRequestException {
      if (method == null) {
          throw new HttpClientInvalidRequestException(REQUEST_ERROR_INVALID_METHOD);
      }
      return new Request(...);
  }
  ```

---

## 5. Spring Boot

### Автоконфигурация
- **`@AutoConfiguration`** — вместо `@Configuration`
- **`@Bean`**, **`@ConditionalOnMissingBean`**, **`@ConditionalOnProperty`**
- **`@EnableConfigurationProperties`** + **`@ConfigurationProperties(prefix = "...")`**

### Внедрение зависимостей
- Constructor injection через `@RequiredArgsConstructor` или `@Autowired`

---

## 6. Исключения

```
Exception (checked)
  └── HttpClientException
       ├── HttpClientConfigurationException
       ├── HttpClientExecuteRequestException
       ├── HttpClientHandleResponseException
       └── HttpClientInvalidRequestException
  └── LoggerException
       └── LoggerAppenderException
```

- **Все исключения — checked** (расширяют `Exception`)
- **Naming**: `{Module}Exception`, `{Module}{Purpose}Exception`
- **Два конструктора**: `(String message)` и `(String message, Throwable cause)`
- **Сообщения** — централизованы в `final`-классах: `HttpExceptionsMessages`, префиксы: `CREATION_ERROR_`, `REQUEST_ERROR_`, `RESPONSE_ERROR_`

---

## 7. Логирование

### Правило

**Только кастомный `LogService`. Никаких внешних библиотек логирования (SLF4J, Logback и т.п.).**

- Каждый сервис имеет `SERVICE_CODE` (например, `"LoggerService"`, `"ConsoleAppender"`)
- Pattern: `logService.debug/info/warn/error(serviceCode, uuid, message, entities...)`
- UUID для отслеживания контекста

### Внутреннее логирование аппендеров и модуля логера

Для диагностических сообщений внутри аппендеров (`FileAppender`, `ConsoleAppender`) и других внутренних классов модуля логера, если логирование через `LogService` не подходит:

- **System.out** — для информационных сообщений: инициализация, закрытие, ротация, успешные операции
- **System.err** — только для сообщений об ошибках

Пример:
```java
System.out.println("[FileAppender] Инициализирован. Файл логов: " + logFilePath);
System.err.println("[FileAppender] Не удалось закрыть файл: " + e.getMessage());
```

**HttpClientLogger** — обёртка над `LogService`:
- `info(forced, ...)` — если `forced=true`, логирует всегда
- `error(...)` — логирует всегда
- `debug(...)` — если `logRequests=true`

---

## 8. Форматирование

- Открывающая `{` — на той же строке (K&R style)
- `{` всегда для `if/else/switch/for/while`, даже для однострочных блоков
- **Switch arrow syntax**: `case DEBUG -> value;`
- **Pattern matching instanceof**: `if (payload instanceof String text)`
- **`List.of()` / `Map.of()`** — вместо `Arrays.asList()` / `new HashMap<>()`
- Stream API активно: `.stream()`, `.map()`, `.filter()`, `.collect()`, `.forEach()`
- **@Override** — на каждом методе интерфейса

---

## 9. JavaDoc

**Обязателен** на каждом публичном классе, методе, поле — на русском языке.

- `<p>` для абзацев, `{@link}` для ссылок, `{@code}` для кода
- `@param`, `@return`, `@throws` — всегда
- `@see` — для связанных классов

---

## 10. Тесты

### Фреймворки
- **JUnit Jupiter** (JUnit 5) + **Mockito** + **AssertJ**

### Аннотации
- **`@DisplayName`** — на **каждом** тестовом классе и **каждом** методе `@Test` / `@ParameterizedTest`
- **`@BeforeEach`** — подготовка
- **`@ExtendWith(MockitoExtension.class)`** — для Mockito
- **`@ParameterizedTest` + `@MethodSource`** — параметризованные тесты

### Mockito
- `@Mock` — моки, `verify(mock).method(...)`, `verify(mock, never()).method(...)`
- `assertThrows(Exception.class, () -> ...)` — проверка исключений

### AssertJ
- `assertThat(...)`, `assertThatThrownBy(...)`, `assertThatCode(...)`
- **`.describedAs("описание")`** — на каждой assertion
- `.extracting("fieldName")` — для приватных полей
- `.asInstanceOf(LIST)` — для type-safe assertions на коллекциях

### Статические импорты
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.*;
```

### Именование
- **Тестовые классы**: `{ClassName}Tests` (множественное число): `HttpClientImplTests`, `RequestBuilderTests`
- **Тестовые методы**: camelCase, описательное: `constructorValidArgs`, `buildResponseBody`
- **Test data providers**: `{methodName}ArgsProvider`, пакет `checkdata.providers`
- Для исключений: `{MethodName}Throws`, `{methodName}ShouldThrow`