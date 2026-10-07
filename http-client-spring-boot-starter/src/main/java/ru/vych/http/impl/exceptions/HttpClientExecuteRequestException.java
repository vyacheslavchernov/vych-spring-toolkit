package ru.vych.http.impl.exceptions;

/**
 * Исключение, выбрасываемое при ошибке отправки HTTP-запроса.
 * <p>
 * Возникает в {@link ru.vych.http.impl.HttpClientImpl} при сбое
 * при вызове {@code java.net.http.HttpClient.send()} — например,
 * при потере соединения, тайм-ауте или некорректном URI.
 * </p>
 *
 * @see HttpClientException
 */
public class HttpClientExecuteRequestException extends HttpClientException {
    public HttpClientExecuteRequestException(String message) {
        super(message);
    }

    public HttpClientExecuteRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
