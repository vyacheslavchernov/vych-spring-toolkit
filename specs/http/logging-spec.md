# http-logging-spec — HTTP Client Logging

## Purpose

Логирование запросов и ответов HTTP-клиента через `LogService` из logger-модуля. Поддерживает управляемое логирование: можно включить/выключить через конфигурацию, но error логи всегда активны.

## Preconditions

- Logger-модуль (`logger-spring-boot-starter`) доступен в classpath
- `LogService` доступен как Spring bean
- HTTP-клиент создан с валидной конфигурацией

## Inputs

### HttpClientLogger

Обёртка над `LogService` для HTTP-клиента:

| Метод | Параметры | Описание |
|---|---|---|
| `info(boolean forced, String message, Object... entities)` | `forced` — всегда ли логировать | Info уровень |
| `debug(boolean forced, String message, Object... entities)` | `forced` — всегда ли логировать | Debug уровень |
| `error(String message, Object... entities)` | — | Error уровень (всегда активен) |

### Параметры логирования

| Параметр | Тип | По умолчанию | Описание |
|---|---|---|---|
| `logRequests` | `boolean` | `true` | Включить/выключить логирование запросов/ответов |

## Behavior

### Инициализация клиента

1. При создании клиента `HttpClientLogger.info(forced=true, ...)` логирует инициализацию
2. Логирование инициализации **всегда** активно, независимо от `logRequests`
3. В лог записываются: `serviceCode`, `clientUuid`, сообщение об инициализации

### Логирование запросов

1. При выполнении `execute()`:
   - Если `logRequests=true` — debug логирование активно
   - Если `logRequests=false` — debug логирование отключено
2. Логирование интерсепторов (`debug`)
3. Логирование отправки запроса (`debug`)
4. Логирование получения ответа (`debug`)
5. Каждый лог содержит: `serviceCode`, `requestUuid`, `clientUuid`

### Логирование ошибок

1. При любой ошибке в `execute()`:
   - `error()` логирует **всегда**, независимо от `logRequests`
2. В лог записываются: `serviceCode`, `requestUuid`, `clientUuid`, сообщение об ошибке, cause

### Forced логирование

Методы `info()` и `debug()` принимают параметр `forced`:
- `forced=true` — логирует всегда, независимо от `logRequests`
- `forced=false` — логирует только если `logRequests=true`

## Business rules

- `error()` логирует **всегда** — это не зависит от `logRequests`
- `info()` и `debug()` с `forced=false` подчиняются `logRequests`
- `info()` и `debug()` с `forced=true` логируют всегда
- Каждый лог содержит `serviceCode` и `requestUuid` для корреляции
- `clientUuid` используется для идентификации конкретного клиента
- Логирование не влияет на функциональность клиента — ошибки в логере не должны ломать запросы

## Errors

| Ситуация | Поведение |
|---|---|
| `LogService` недоступен | Клиент не создаётся (Spring bean не зарегистрируется) |
| Ошибка в `LogService` при логировании | Ошибка логируется в системный лог, запрос продолжается |

## Acceptance criteria

- [ ] Инициализация клиента логируется всегда (forced=true)
- [ ] Запросы логируются на debug уровне только если `logRequests=true`
- [ ] Запросы не логируются на debug уровне если `logRequests=false`
- [ ] Ошибки логируются на error уровне всегда
- [ ] Каждый лог содержит `serviceCode`, `requestUuid`, `clientUuid`
- [ ] Ошибка в `LogService` не ломает выполнение запроса

## Examples

### Логирование с logRequests=true

```
[MyService] [abc-123] [client-uuid] Sending GET request to https://api.example.com/api/users
[MyService] [abc-123] [client-uuid] Received response: status=200, body={"users":[]}
```

### Логирование с logRequests=false

```
[MyService] [client-uuid] HTTP Client initialized
// Запросы не логируются
// Но ошибки логируются:
[MyService] [abc-123] [client-uuid] Error executing request: Connection timeout
```
