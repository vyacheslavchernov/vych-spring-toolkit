package ru.vych.http.impl.common;

/**
 * Поддерживаемые HTTP-методы.
 * <p>
 * Клиент поддерживает GET, POST, PUT, DELETE, PATCH, HEAD и OPTIONS.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient
 * @see ru.vych.http.impl.entities.Request#getMethod()
 */
public enum HttpMethod {
    /**
     * HTTP GET — получение ресурса. Не изменяет состояние сервера.
     */
    GET,

    /**
     * HTTP POST — отправка данных на сервер. Может изменять состояние сервера.
     */
    POST,

    /**
     * HTTP PUT — полное обновление ресурса. Поддерживает тело запроса.
     */
    PUT,

    /**
     * HTTP DELETE — удаление ресурса. Поддерживает тело запроса.
     */
    DELETE,

    /**
     * HTTP PATCH — частичное обновление ресурса. Поддерживает тело запроса.
     */
    PATCH,

    /**
     * HTTP HEAD — получение только заголовков ресурса. Тело запроса игнорируется.
     */
    HEAD,

    /**
     * HTTP OPTIONS — получение поддерживаемых методов ресурса. Поддерживает тело запроса.
     */
    OPTIONS
}
