---
last_updated: 2026-10-08
scope: testing
---

# Юнит-тесты

Покрытие, паттерны и структура юнит-тестов.

## http-client-spring-boot-starter

~28 тестовых классов покрывают:

- Создание клиента (`HttpClientImplTests`)
- GET/POST запросы
- Десериализация через Jackson
- Интерсепторы (`HttpClientLoggerTests`)
- Обработка статус-кодов (`ResponseTests`)

### Паттерны

- **Parameterized tests:** `@ParameterizedTest` + `@MethodSource` с провайдерами
- **Test data:** классы в `checkdata` / `checkdata.providers` (e.g. `HttpClientImplBuildResponseCheckData`)
- **Test utilities:** DTO и моки в `entities` подпакете

## logger-spring-boot-starter

**Unit-тесты отсутствуют.** Логика проверяется косвенно через интеграционные тесты.

## Кросс-ссылки

- [testing/overview.md](./overview.md) — обзор тестирования
- [packages/http/impl.md](../packages/http/impl.md) — документация пакета impl (источник для тестов)
- [modules/http-client.md](../modules/http-client.md) — документация модуля http-client
- [modules/logger.md](../modules/logger.md) — документация модуля logger
