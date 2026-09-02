package ru.vych.http.impl.common;

/**
 * Поддерживаемые HTTP-методы.
 * <p>
 * В данный момент клиент поддерживает только GET и POST.
 * Другие методы могут быть добавлены в будущих версиях.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient
 * @see Request#method
 */
public enum HttpMethod {
    /**
     * HTTP GET — получение ресурса. Не изменяет состояние сервера.
     */
    GET,

    /**
     * HTTP POST — отправка данных на сервер. Может изменять состояние сервера.
     */
    POST
}
