---
last_updated: 2026-10-08
scope: package
---

# ru.vych (production-код тестов)

Production-код тестового модуля: точка входа Spring Boot приложения.

## Публичные классы

### `App` (@SpringBootApplication)

Точка входа Spring Boot-приложения, которое запускает встроенный JAX-RS сервер (Grizzly) для тестирования HttpClient.

**Метод:** `main(String[] args)` — `SpringApplication.run(App.class, args)`.

**Особенности:** `@SpringBootApplication` автоматически сканирует все подпакеты (`ru.vych.http.*`) и регистрирует `@Configuration`, `@Component`, `@Bean`.

## Зависимости

Нет публичных зависимостей.

## Связанные пакеты

- [`ru.vych.http.config`](../integration.md) — `TestServerConfiguration` использует `App`
- [`ru.vych.http.controllers`](../integration.md) — контроллеры
- [`http`](../integration.md) — интеграционные тесты

## Кросс-ссылки

- [modules/vych-spring-toolkit-tests.md](../../modules/vych-spring-toolkit-tests.md) — документация модуля
- [packages/tests/integration.md](./integration.md) — документация интеграционных тестов
