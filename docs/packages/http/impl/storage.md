---
last_updated: 2026-10-09
scope: package
---

# ru.vych.http.impl.storage

Persistent файловое хранилище HTTP-cookie.

## Публичные классы

### `CookieFileStorage`

Файловое хранилище cookies с JSON-сериализацией. Обеспечивает загрузку, сохранение и TTL-фильтрацию cookies.

**Ключевые методы:**
- `load(ConcurrentHashMap)` — загрузка cookies из файла, фильтрация по TTL
- `asyncSave(ConcurrentHashMap)` — асинхронное сохранение в фоновом потоке
- `syncSave(ConcurrentHashMap)` — синхронное финальное сохранение (shutdown)

**Особенности:**
- Атомарная запись через временный файл + rename
- Session cookies (maxAge < 0) не сохраняются
- Cookies с maxAge == 0 считаются истёкшими
- Cookies с maxAge > 0 сохраняются с createdAt timestamp
- При загрузке cookies с истёкшим TTL отфильтровываются
- Повреждённый JSON-файл игнорируется с логированием ошибки

**Вложенный класс `CookieFileData`:** структура JSON-файла (version, savedAt, hosts).

## Зависимости

- `CookieFileStorage` → `SerializedCookie`, `ObjectMapper`, `LogService`
- `CookieFileData` → `SerializedCookie`

## Связанные пакеты

- [`impl/entities`](./impl/entities.md) — `SerializedCookie`

## Кросс-ссылки

- [packages/http/impl.md](./impl.md) — `HttpClientImpl` использует `CookieFileStorage`
- [modules/http-client.md](../../modules/http-client.md) — persistent cookie storage
