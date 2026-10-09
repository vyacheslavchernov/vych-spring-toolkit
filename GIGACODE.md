# GIGACODE.md — Vych Spring Toolkit

## Назначение

Практические инструкции для AI-агентов: build commands, commit guidelines, структура проекта.
Подробная документация — в [`docs/`](docs/).

## Ссылки на документацию

| Тема | Документ |
|---|---|
| Общая архитектура, паттерны, зависимости | [docs/architecture.md](docs/architecture.md) |
| Coding standards, конвенции, паттерны | [docs/coding-standards.md](docs/coding-standards.md) |
| Commit guidelines | [docs/guidelines/commit-guidelines.md](docs/guidelines/commit-guidelines.md) |
| Build commands | [docs/build-commands.md](docs/build-commands.md) |
| Правила написания спецификаций | [docs/guidelines/specifications-guidelines.md](docs/guidelines/specifications-guidelines.md) |
| Правила реализации спецификаций | [docs/guidelines/implementation-guideline.md](docs/guidelines/implementation-guideline.md) |
| Обзор проекта, навигация | [docs/README.md](docs/README.md) |
| Обзор модулей | [docs/modules/README.md](docs/modules/README.md) |
| HTTP-клиент starter | [docs/modules/http-client.md](docs/modules/http-client.md) |
| Logger starter | [docs/modules/logger.md](docs/modules/logger.md) |
| BOM | [docs/modules/vych-spring-toolkit-bom.md](docs/modules/vych-spring-toolkit-bom.md) |
| Интеграционные тесты | [docs/modules/vych-spring-toolkit-tests.md](docs/modules/vych-spring-toolkit-tests.md) |
| Фреймворки и конвенции тестирования | [docs/testing/overview.md](docs/testing/overview.md) |
| Юнит-тесты | [docs/testing/unit-tests.md](docs/testing/unit-tests.md) |
| Интеграционные тесты, mock-сервер | [docs/testing/integration-tests.md](docs/testing/integration-tests.md) |
| Правила создания документации для AI | [docs/guidelines/agent-documentation-guidelines.md](docs/guidelines/agent-documentation-guidelines.md) |

## Структура проекта

```
vych-spring-toolkit/
├── http-client-spring-boot-starter/   # HTTP-клиент (зависит от logger)
├── logger-spring-boot-starter/        # Logger (независимый)
├── vych-spring-toolkit-bom/           # BOM (управление версиями)
├── vych-spring-toolkit-tests/         # Интеграционные тесты (зависит от http-client)
├── docs/                              # Документация для AI-агентов
├── pom.xml                            # Корневой POM
└── GIGACODE.md                        # Этот файл
```

## Практические инструкции для AI-агентов

### Начало работы
1. **Изучи документацию релевантную для задачи**
2. **Составь план выполнения задачи**

### Работа с кодом

1. **Читай документацию перед изменениями** — начни с `docs/README.md`, `docs/architecture.md` и `docs/guidelines/implementation-guideline.md`
2. **Соблюдай coding standards** — `docs/coding-standards.md` содержит все конвенции
3. **Проверяй зависимости между модулями** — logger → http-client → tests (см. `docs/architecture.md`)
4. **Checkstyle** — при провале сборки из-за checkstyle:
   - **Правьте код**, чтобы он проходил проверки (warnings и errors блокируют сборку)
   - **НЕ правьте `checkstyle.xml`** — конфигурация это стандарт проекта, код должен соответствовать стандарту
   - Подробнее: `docs/coding-standards.md` (раздел 0. Checkstyle)

### Работа с документацией

1. **Не дублируй** информацию из `/docs` в GIGACODE.md
2. **Добавляй кросс-ссылки** на соответствующие документы в `/docs`
3. **Обновляй `last_updated`** в YAML-фронтматтере при изменении документов
4. **Используй Explore-агентов** для исследования кода перед созданием/обновлением документации
