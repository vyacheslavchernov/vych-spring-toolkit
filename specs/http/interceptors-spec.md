# http-interceptors-spec — HTTP Client Interceptors

## Purpose

Интерсепторы для расширения поведения HTTP-клиента: модификации запросов до отправки и обработки ответов после получения. Позволяют внедрять кастомную логику без изменения основного кода клиента.

## Preconditions

- HTTP-клиент создан и работает корректно
- `RequestInterceptor` и `ResponseInterceptor` зарегистрированы как Spring beans
- Интерсепторы внедряются в `HttpClientBuilder.build()` автоматически

## Inputs

### RequestInterceptor

```java
public interface RequestInterceptor {
    void handle(HttpClient client, Request request);
}
```

| Параметр | Описание |
|---|---|
| `client` | Текущий `HttpClient` экземпляр |
| `request` | Выполняемый `Request` (mutable) |

### ResponseInterceptor

```java
public interface ResponseInterceptor {
    void handle(HttpClient client, Response response);
}
```

| Параметр | Описание |
|---|---|
| `client` | Текущий `HttpClient` экземпляр |
| `response` | Полученный `Response` |

## Behavior

### Регистрация интерсепторов

1. Интерсепторы регистрируются как Spring beans типов `List<RequestInterceptor>` и `List<ResponseInterceptor>`
2. `HttpClientBuilder.build()` автоматически внедряет списки интерсепторов
3. Пустой список интерсепторов — допустимое состояние (клиент работает без интерсепторов)

### Выполнение RequestInterceptor

1. Перед отправкой HTTP-запроса все `RequestInterceptor` выполняются **последовательно** в порядке регистрации
2. Каждый интерсептор может:
   - Модифицировать `Request` (добавлять/изменять заголовки, payload)
   - Выполнять side effects (логирование, метрики, аутентификация)
3. Если любой интерсептор бросает исключение — выполнение останавливается, запрос не отправляется
4. Исключение пробрасывается вызывающему через `execute()`

### Выполнение ResponseInterceptor

1. После получения и десериализации ответа все `ResponseInterceptor` выполняются **последовательно** в порядке регистрации
2. Каждый интерсептор может:
   - Анализировать `Response` (status code, body, headers)
   - Выполнять side effects (логирование, метрики, обработка ошибок)
3. Если любой интерсептор бросает исключение — оно пробрасывается вызывающему через `execute()`

### Порядок выполнения

```
execute(request)
  → [RequestInterceptor_1]
  → [RequestInterceptor_2]
  → ...
  → send HTTP request
  → build Response
  → deserialize body
  → [ResponseInterceptor_1]
  → [ResponseInterceptor_2]
  → ...
  → return Response
```

## Business rules

- Интерсепторы выполняются **всегда**, независимо от значения `logRequests`
- Порядок выполнения интерсепторов определяется порядком их регистрации в Spring
- Интерсепторы не имеют доступа к внутреннему состоянию клиента, кроме публичного API
- `RequestInterceptor` получает **модифицируемый** `Request` — изменения влияют на следующий интерсептор и на фактический запрос
- `ResponseInterceptor` получает `Response`, но модификации не влияют на возвращаемый объект

## Errors

| Ситуация | Поведение |
|---|---|
| Исключение в `RequestInterceptor` | Запрос не отправляется, исключение пробрасывается через `execute()` |
| Исключение в `ResponseInterceptor` | Исключение пробрасывается через `execute()` |
| `List<RequestInterceptor>` = `null` | Не происходит — внедряется пустой список |
| `List<ResponseInterceptor>` = `null` | Не происходит — внедряется пустой список |

## Acceptance criteria

- [ ] Пустой список интерсепторов не влияет на работу клиента
- [ ] `RequestInterceptor` выполняется перед отправкой запроса
- [ ] `ResponseInterceptor` выполняется после получения ответа
- [ ] Интерсепторы выполняются последовательно в порядке регистрации
- [ ] Исключение в `RequestInterceptor` останавливает выполнение, запрос не отправляется
- [ ] Исключение в `ResponseInterceptor` пробрасывается через `execute()`
- [ ] `RequestInterceptor` может модифицировать `Request`
- [ ] Изменения в `Request` от одного интерсептора видны следующему интерсептору
- [ ] Интерсепторы не влияют на `logRequests` поведение

## Examples

### Аутентификационный интерсептор

```java
@Component
public class AuthInterceptor implements RequestInterceptor {
    @Override
    public void handle(HttpClient client, Request request) {
        request.addHeader("Authorization", "Bearer " + tokenProvider.getToken());
    }
}
```

### Интерсептор для обработки ошибок

```java
@Component
public class ErrorHandlingInterceptor implements ResponseInterceptor {
    @Override
    public void handle(HttpClient client, Response response) {
        if (response.getStatus() == HttpStatus.UNAUTHORIZED) {
            throw new HttpClientUnauthorizedException("Token expired");
        }
    }
}
```
