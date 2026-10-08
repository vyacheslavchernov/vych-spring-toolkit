---
last_updated: 2026-10-08
scope: testing
---

# Обзор тестирования

Фреймворки, конвенции и структура тестирования в проекте.

## Фреймворки

| Фреймворк | Версия | Назначение |
|---|---|---|
| **JUnit Jupiter** | — | Основной тестовый фреймворк |
| **Mockito** | — | Мокирование (`mockito-core`, `mockito-junit-jupiter`) |
| **AssertJ** | 3.27.3 | Fluent-ассерты |
| **Allure** | 2.29.1 | Отчётность (`allure-junit5`) |
| **AspectJ** | 1.9.22.1 | Weaving для Allure |

## Конвенции тестирования

- **Название тестовых классов:** `<ClassName>Tests` (e.g. `HttpClientImplTests`, `HttpClientLoggerTests`)
- **Parameterized tests:** `@ParameterizedTest` + `@MethodSource` с провайдерами в `*TestsDataProviders`
- **Test data:** классы с данными в `checkdata` / `checkdata.providers` подпакетах
- **DisplayName:** на русском языке
- **Step-ассерты:** Allure `@Step` в `BaseHttpTest` для интеграционных тестов

## Зависимости тестирования по модулям

### http-client-spring-boot-starter
- `junit-jupiter`, `mockito-core`, `mockito-junit-jupiter`, `assertj-core`

### logger-spring-boot-starter
- **Нет тестов** (unit-тесты отсутствуют)

### vych-spring-toolkit-tests
- `spring-boot-starter-test`, `jakarta.ws.rs-api:3.1.0`, `jersey-container-grizzly2-http:3.1.8`, `jersey-media-json-jackson:3.1.8`, `jersey-hk2:3.1.8`, `assertj-core:3.27.3`, `aspectjweaver:1.9.22.1`, `allure-junit5:2.29.1`

## Структура тестовых пакетов

```
http-client-spring-boot-starter/src/test/java/
  └── ru/vych/http/impl/
        ├── HttpClientImplTests
        ├── HttpClientLoggerTests
        └── ResponseTests
        └── checkdata/
        └── entities/

vych-spring-toolkit-tests/src/test/java/
  └── http/ (flat package)
        ├── BaseHttpTest
        ├── HttpClientGetTests
        ├── HttpClientPostTests
        ├── HttpClientHeadersTests
        ├── HttpClientInterceptorsTests
        ├── HttpClientNon2xxResponseTests
        ├── HttpClientCookiePoliciesTests
        └── HttpClientRedirectTests
```

## Кросс-ссылки

- [testing/unit-tests.md](./unit-tests.md) — юнит-тесты
- [testing/integration-tests.md](./integration-tests.md) — интеграционные тесты
- [packages/tests/integration.md](../packages/tests/integration.md) — документация тестовых пакетов
