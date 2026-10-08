---
last_updated: 2026-10-08
scope: module
---

# Обзор модулей

Список всех модулей проекта с кратким описанием назначения и зависимостей.

## Модули

- [http-client.md](http-client.md) — Spring Boot starter для HTTP-клиента (GET/POST)
- [logger.md](logger.md) — Spring Boot starter для кастомного консольного логгера
- [vych-spring-toolkit-bom.md](vych-spring-toolkit-bom.md) — Bill of Materials
- [vych-spring-toolkit-tests.md](vych-spring-toolkit-tests.md) — интеграционные тесты

## Зависимости между модулями

```
logger-spring-boot-starter     (независимый)
       ▲
       │
http-client-spring-boot-starter  (зависит от logger)
       ▲
       │
vych-spring-toolkit-tests        (зависит от http-client)
```

## Общие характеристики

| Параметр | Значение |
|---|---|
| **Java** | 21 |
| **Spring Boot** | 3.5.6 |
| **Версия проекта** | 0.0.5-SNAPSHOT |
| **Packaging** | POM (parent) + JAR (модули) |
| **Repo** | GitHub Packages (`maven.pkg.github.com/vyacheslavchernov/vych-spring-toolkit`) |

## Кросс-ссылки

- [architecture.md](../architecture.md) — общая архитектура
- [GIGACODE.md](../GIGACODE.md) — build commands, coding standards
