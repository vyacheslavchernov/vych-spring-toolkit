---
last_updated: 2026-10-09
scope: architecture
---

# Документация vych-spring-toolkit

Документация, оптимизированная для AI-агентов. Описывает архитектуру, модули, пакеты и тестирование проекта.

## Обзор

**vych-spring-toolkit** — Maven multi-module Spring Boot 3.5.6 проект (Java 21), предоставляющий два standalone starter'а: HTTP-клиент (GET/POST) и кастомный консольный логгер.

## Навигация

### Архитектура и стандарты
- [architecture.md](architecture.md) — общая архитектура, паттерны, зависимости между модулями
- [coding-standards.md](coding-standards.md) — конвенции написания кода, стили, паттерны, правила тестирования
- [build-commands.md](build-commands.md) — Maven-команды: сборка, тесты, версии, публикация

### Гайдлайны
- [guidelines/commit-guidelines.md](guidelines/commit-guidelines.md) — конвенции оформления коммитов
- [guidelines/agent-documentation-guidelines.md](guidelines/agent-documentation-guidelines.md) — правила создания и поддержки документации для AI-агентов
- [guidelines/specifications-guidelines.md](guidelines/specifications-guidelines.md) — правила написания спецификаций
- [guidelines/implementation-guideline.md](guidelines/implementation-guideline.md) — правила реализации спецификаций

### Модули
- [modules/README.md](modules/README.md) — обзор всех модулей
- [modules/http-client.md](modules/http-client.md) — HTTP-клиент starter
- [modules/logger.md](modules/logger.md) — Logger starter
- [modules/vych-spring-toolkit-bom.md](modules/vych-spring-toolkit-bom.md) — BOM
- [modules/vych-spring-toolkit-tests.md](modules/vych-spring-toolkit-tests.md) — интеграционные тесты

### Пакеты HTTP-клиента
- [packages/http/config.md](packages/http/config.md) — `ru.vych.http.config`
- [packages/http/impl.md](packages/http/impl.md) — `ru.vych.http.impl`
- [packages/http/impl/common.md](packages/http/impl/common.md) — `ru.vych.http.impl.common`
- [packages/http/impl/entities.md](packages/http/impl/entities.md) — `ru.vych.http.impl.entities`
- [packages/http/impl/exceptions.md](packages/http/impl/exceptions.md) — `ru.vych.http.impl.exceptions`
- [packages/http/impl/interceptors.md](packages/http/impl/interceptors.md) — `ru.vych.http.impl.interceptors`

### Пакеты Logger
- [packages/logger/config.md](packages/logger/config.md) — `ru.vych.logger.config`
- [packages/logger/impl.md](packages/logger/impl.md) — `ru.vych.logger.impl`
- [packages/logger/impl/appenders.md](packages/logger/impl/appenders.md) — `ru.vych.logger.impl.appenders`
- [packages/logger/impl/common.md](packages/logger/impl/common.md) — `ru.vych.logger.impl.common`
- [packages/logger/impl/entities.md](packages/logger/impl/entities.md) — `ru.vych.logger.impl.entities`
- [packages/logger/impl/exceptions.md](packages/logger/impl/exceptions.md) — `ru.vych.logger.impl.exceptions`

### Пакеты тестов
- [packages/tests/main.md](packages/tests/main.md) — `ru.vych` (production-код тестов)
- [packages/tests/integration.md](packages/tests/integration.md) — интеграционные тесты (`http`)

### Тестирование
- [testing/overview.md](testing/overview.md) — фреймворки, конвенции
- [testing/unit-tests.md](testing/unit-tests.md) — юнит-тесты
- [testing/integration-tests.md](testing/integration-tests.md) — интеграционные тесты, mock-сервер

### Внешняя документация
- [GIGACODE.md](../GIGACODE.md) — build commands, структура проекта, инструкции для AI-агентов
