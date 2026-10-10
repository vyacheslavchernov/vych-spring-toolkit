# http — HTTP Client Module

Спецификации для модуля HTTP-клиента Spring Boot Starter.

## Функциональность

- Типизированный HTTP-клиент с builder-паттерном
- GET и POST запросы с Jackson-десериализацией
- Интерсепторы запросов и ответов
- Изолированное cookie-хранилище
- Логирование запросов/ответов через logger-модуль
- Checked-исключения с детализацией по типам ошибок

## Спецификации

- [HTTP Client Core](client-spec.md) — основной функционал: GET/POST, builder, execute, десериализация
- [Configuration](configuration-spec.md) — свойства конфигурации и валидация
- [Interceptors](interceptors-spec.md) — RequestInterceptor и ResponseInterceptor
- [Cookies](cookies-spec.md) — изолированное cookie-хранилище и cookie policies
- [Logging](logging-spec.md) — логирование запросов и ответов через LogService
- [Error Handling](error-handling-spec.md) — иерархия checked-исключений и условия их возникновения
- [Caching](cache-spec.md) — кеширование GET-запросов в памяти, LRU-eviction, TTL, инвалидация по паттерну URL
