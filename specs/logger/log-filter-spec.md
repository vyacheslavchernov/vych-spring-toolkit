# logger-log-filter-spec — LogFilter

## Purpose

Интерфейс для фильтрации событий логирования. Позволяет пользователю определять пользовательские правила, которые определяют, должно ли событие быть передано в конкретный аппендер. Фильтры применяются перед отправкой события в каждый аппендер.

## Preconditions

- Logger-модуль (`logger-spring-boot-starter`) подключён к Spring Boot приложению
- `LogService` зарегистрирован как Spring Bean
- `LogEvent` доступен для создания событий

## Inputs

### LogFilter (интерфейс)

| Метод | Параметры | Возвращаемое значение | Описание |
|---|---|---|---|
| `filter(LogEvent logEvent)` | `logEvent` — событие логирования | `boolean` — `true` если событие должно быть логировано, `false` если нужно пропустить | Проверяет, следует ли логировать данное событие |

### LogEvent

Событие логирования, передаваемое в фильтр:

| Поле | Тип | Описание |
|---|---|---|
| `serviceCode` | `String` | Код сервиса-отправителя |
| `uuid` | `String` | UUID контекста (trace ID) |
| `loggingLevel` | `LoggingLevel` | Уровень логирования |
| `message` | `String` | Текст сообщения |
| `timestamp` | `LocalDateTime` | Временная метка создания |
| `entities` | `List<Object>` | Дополнительные объекты |

## Behavior

### Применение фильтров

1. Фильтры регистрируются как Spring Beans типа `LogFilter` в контексте приложения
2. При отправке лог-сообщения через `LogService`:
   - Для каждого аппендера событие проходит через все фильтры последовательно
   - Каждый фильтр вызывается отдельно для каждого аппендера
   - Если хотя бы один фильтр возвращает `false` — событие **не передаётся** в этот аппендер
   - Если все фильтры вернули `true` (или список фильтров пуст) — событие передаётся в аппендер
3. Результат фильтрации одного аппендера **не влияет** на фильтрацию другого аппендера

### Порядок применения

1. `LogService.log()` создаёт `LogEvent` из параметров
2. Для каждого аппендера `appender` в `appenders`:
   - Для каждого фильтра `filter` в `logFilters`:
     - Вызывается `filter.filter(event)`
     - Если возвращает `false` — цикл прерывается, переход к следующему аппендеру
   - Если все фильтры прошли — вызывается `appender.append(event)`

### Реализация пользовательских фильтров

Пользователь должен реализовать интерфейс `LogFilter` самостоятельно. Фильтр может:

- Анализировать любой поле `LogEvent` (`serviceCode`, `uuid`, `loggingLevel`, `message`, `timestamp`, `entities`)
- Содержать внутреннее состояние
- Быть потокобезопасным (если ожидается параллельный вызов)

## Business rules

- Фильтры применяются **только** в контексте `LogService` — они не вызываются напрямую
- Каждый фильтр вызывается **отдельно для каждого аппендера** — один и тот же фильтр может вернуть разные значения для разных аппендеров
- Фильтрация происходит **до** вызова `appender.append()`
- Если список фильтров пуст — все события передаются во все аппендеры без фильтрации
- Порядок применения фильтров соответствует порядку в `List<LogFilter>`
- `true` = пропустить событие в аппендер, `false` = пропустить событие для этого аппендера

## Errors

| Ситуация | Поведение |
|---|---|
| Фильтр бросает исключение | Поведение не определено — фильтр должен обрабатывать все ошибки internally |
| `logEvent` = `null` в `filter()` | Поведение не определено (не ожидается вызов с `null`) |

## Acceptance criteria

- [ ] `LogFilter` — функциональный интерфейс с методом `filter(LogEvent)`
- [ ] Фильтры регистрируются как Spring Beans и автоматически подключаются к `LogService`
- [ ] Если хотя бы один фильтр возвращает `false`, событие не передаётся в аппендер
- [ ] Если все фильтры возвращают `true`, событие передаётся в аппендер
- [ ] Если список фильтров пуст, все события передаются без фильтрации
- [ ] Фильтры применяются последовательно для каждого аппендера отдельно
- [ ] Результат фильтрации одного аппендера не влияет на другой аппендер

## Examples

### Фильтрация по уровню логирования

```java
@Component
public class InfoOnlyFilter implements LogFilter {
    @Override
    public boolean filter(LogEvent logEvent) {
        // Пропускаем только INFO и выше
        return logEvent.getLoggingLevel().getValue() >= LoggingLevel.INFO.getValue();
    }
}
```

### Фильтрация по сервису

```java
@Component
public class ServiceFilter implements LogFilter {
    private final Set<String> allowedServices;

    public ServiceFilter(Set<String> allowedServices) {
        this.allowedServices = allowedServices;
    }

    @Override
    public boolean filter(LogEvent logEvent) {
        return allowedServices.contains(logEvent.getServiceCode());
    }
}
```

### Фильтрация по времени

```java
@Component
public class BusinessHoursFilter implements LogFilter {
    @Override
    public boolean filter(LogEvent logEvent) {
        LocalDateTime timestamp = logEvent.getTimestamp();
        int hour = timestamp.getHour();
        // Логируем только в рабочее время (9:00–18:00)
        return hour >= 9 && hour < 18;
    }
}
```
