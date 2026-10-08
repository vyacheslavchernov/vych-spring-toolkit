---
last_updated: 2026-10-09
scope: architecture
---

# Архитектура vych-spring-toolkit

Общая архитектура проекта, паттерны и зависимости между модулями.

## Архитектурный обзор

Проект состоит из **4 модулей**, каждый из которых является независимой единицей сборки:

```
vych-spring-toolkit/
├── http-client-spring-boot-starter/   # HTTP-клиент (зависит от logger)
├── logger-spring-boot-starter/        # Logger (независимый)
├── vych-spring-toolkit-bom/           # BOM (управление версиями)
└── vych-spring-toolkit-tests/         # Интеграционные тесты (зависит от http-client)
```

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

**vych-spring-toolkit-bom** — не имеет кода, только `pom.xml` для управления версиями потребителей.

## Паттерны проектирования

### HTTP-клиент
- **Builder pattern** — `HttpClientBuilder` создаёт клиентов, `Request.Builder` формирует запросы
- **Strategy pattern** — `RequestInterceptor` / `ResponseInterceptor` как функциональные интерфейсы
- **Factory pattern** — `HttpClientBuilder.build()` как simple factory с зависимостями
- **Chain of Responsibility** — цепочка интерсепторов выполняется последовательно через `forEach`

### Logger
- **Pipeline pattern** — `LogService` отправляет события всем `LogAppender` через multi-appender pipeline
- **Filter pattern** — `LogFilter` применяется индивидуально к каждому аппендеру
- **Template Method** — `LogService.log()` реализует общий алгоритм: create → filter → append → error handling

### Общие паттерны
- **Auto-configuration** — Spring Boot 3.x `@AutoConfiguration` вместо `@Configuration`
- **Configuration Properties** — `@ConfigurationProperties` для привязки `application.yaml`
- **Dependency Injection** — constructor injection через `@RequiredArgsConstructor`
- **Lombok** — `@Getter`, `@Setter`, `@Accessors(chain = true)`, `@RequiredArgsConstructor`, `@AllArgsConstructor`

## Ключевые решения

1. **Кастомный LogService** — logger не использует SLF4J/Logback, все логи идут через собственный LogService
2. **Внутреннее логирование аппендеров** — для диагностики внутри аппендеров используются `System.out` (инфо) и `System.err` (ошибки), а не LogService
3. **Java 11 HttpClient** — http-client обёртка над native Java 11 HTTP Client
3. **Jackson для JSON** — `ObjectMapper` с `JavaTimeModule` для сериализации/десериализации
4. **Checked exceptions** — все исключения checked, сгруппированы в `impl.exceptions`
5. **UUID-трейсинг** — каждый запрос и клиент имеют UUID для логирования
6. **Cookie isolation** — каждый клиент имеет собственное `ConcurrentHashMap` cookie-хранилище

## Кросс-ссылки

- [modules/README.md](modules/README.md) — обзор модулей
- [packages/http/impl.md](packages/http/impl.md) — детали реализации HTTP-клиента
- [packages/logger/impl.md](packages/logger/impl.md) — детали реализации логгера
- [testing/overview.md](testing/overview.md) — фреймворки тестирования
