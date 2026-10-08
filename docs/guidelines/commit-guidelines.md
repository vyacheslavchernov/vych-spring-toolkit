---
last_updated: 2026-10-09
scope: guideline
---

# Commit Guidelines

Принятые в проекте конвенции оформления коммитов.

## Формат сообщения

```
<type>: <subject>

[optional body]
```

**Type** — один из:

- `feat` — новая функциональность
- `fix` — исправление бага
- `refactor` — рефакторинг без изменения поведения
- `docs` — изменения в документации
- `test` — добавление или изменение тестов
- `build` — изменения build-конфигурации (POM, зависимости)
- `chore` — рутинные изменения (версии, конфиги)

**Subject** — краткое описание на английском, с маленькой буквы, без точки в конце.

**Body** — необязательное подробное описание на английском, если изменения неочевидны.

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
