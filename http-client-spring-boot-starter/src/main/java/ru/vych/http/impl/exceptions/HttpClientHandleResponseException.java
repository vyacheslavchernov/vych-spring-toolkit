package ru.vych.http.impl.exceptions;

/**
 * Исключение, выбрасываемое при ошибке обработки тела HTTP-ответа.
 * <p>
 * Возникает в {@link ru.vych.http.impl.HttpClientImpl} при:
 * <ul>
 *   <li>Невозможности десериализовать JSON в целевой класс (Jackson error)</li>
 *   <li>Невозможности сериализовать тело POST-запроса в JSON</li>
 * </ul>
 * </p>
 *
 * @see HttpClientException
 */
public class HttpClientHandleResponseException extends HttpClientException {
    public HttpClientHandleResponseException(String message) {
        super(message);
    }

    public HttpClientHandleResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
