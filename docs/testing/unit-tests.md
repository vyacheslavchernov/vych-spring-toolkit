---
last_updated: 2026-10-09
scope: testing
---

# Юнит-тесты

Покрытие, паттерны и структура юнит-тестов.

## http-client-spring-boot-starter

6 тестовых классов покрывают:

- Создание клиента (`HttpClientImplTests`)
- Cookie storage и isolation (`HttpClientCookieStoreTests`)
- Логирование (`HttpClientLoggerTests`)
- Request builder validation (`RequestBuilderTests`)
- Response body casting (`ResponseTests`)
- Обработка исключений интерсепторов (`HttpClientInterceptorExceptionTests`)

GET/POST запросы, десериализация через Jackson и обработка статус-кодов также покрыты `HttpClientImplTests` и интеграционными тестами.

### Паттерны

- **Parameterized tests:** `@ParameterizedTest` + `@MethodSource` с провайдерами
- **Test data:** классы в `checkdata` / `checkdata.providers` (e.g. `HttpClientImplBuildResponseCheckData`)
- **Test utilities:** DTO и моки в `entities` подпакете

## logger-spring-boot-starter

5 тестовых классов покрывают:

- `FileAppenderTests` — инициализация, запись, фильтрация, сериализация, ротация, потокобезопасность
- `FileAppenderProviderTests` — создание аппендера через провайдер
- `FileAppenderPropertiesTests` — значения по умолчанию, валидация
- `ConsoleAppenderTests` — фильтрация, ANSI цвета, JSON entities, pretty/dim entities
- `LogServiceTests` — обработка ошибок аппендеров, no-message variants, timestamp

## Кросс-ссылки

- [testing/overview.md](./overview.md) — обзор тестирования
- [packages/http/impl.md](../packages/http/impl.md) — документация пакета impl (источник для тестов)
- [modules/http-client.md](../modules/http-client.md) — документация модуля http-client
- [modules/logger.md](../modules/logger.md) — документация модуля logger
