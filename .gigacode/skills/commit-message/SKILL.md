---
name: commit-message
description: Составлять корректные commit message в соответствии с конвенциями проекта. Триггеры: "commit message", "составь commit message", "подготовь коммит".
---

# Commit Message

Составление commit message в соответствии с конвенциями проекта.

## Формат

```
<type>: <subject>

[optional body]
```

**Type** — один из:

| Type | Описание |
|---|---|
| `feat` | новая функциональность |
| `fix` | исправление бага |
| `refactor` | рефакторинг без изменения поведения |
| `docs` | изменения в документации |
| `test` | добавление или изменение тестов |
| `build` | изменения build-конфигурации (POM, зависимости) |
| `chore` | рутинные изменения (версии, конфиги) |

**Subject** — краткое описание на английском, с маленькой буквы, без точки в конце.

**Body** — необязательное подробное описание на английском, если изменения неочевидны.

## Workflow

1. Собери изменения: `git status` + `git diff HEAD`
2. Определи **type** по характеру изменений
3. Определи **scope** — какой модуль/компонент затронут (если уместно)
4. Составь **subject** — что изменилось, коротко
5. Добавь **body**, если изменения неочевидны
6. Проверь по чек-листу

## Чек-лист

- [ ] Type из разрешённого списка
- [ ] Subject на английском, с маленькой буквы
- [ ] Нет точки в конце subject
- [ ] Разделитель — пустая строка между subject и body
- [ ] Body на английском (если есть)
- [ ] Соответствует характеру изменений (feat ≠ fix ≠ refactor)

## Примеры

```
feat(http-client): add response interceptor support

Add RequestInterceptor and ResponseInterceptor interfaces
with chain-of-responsibility execution in HttpClientImpl.
```

```
fix(logger): handle null UUID in LogService

LogService.log() was throwing NPE when UUID was not provided.
Now uses UUID.randomUUID() as fallback.
```

```
docs: update architecture diagram in docs/architecture.md
```

## Правила проекта

Полные правила — [`docs/guidelines/commit-guidelines.md`](docs/guidelines/commit-guidelines.md).
