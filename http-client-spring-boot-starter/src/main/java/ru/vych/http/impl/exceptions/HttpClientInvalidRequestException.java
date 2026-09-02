package ru.vych.http.impl.exceptions;

/**
 * Исключение, выбрасываемое при невалидном {@link Request}.
 * <p>
 * Возникает в {@link Request.Builder#build()} при:
 * <ul>
 *   <li>Отсутствии HTTP-метода</li>
 *   <li>Наличии payload в POST-запросе без установленного Content-Type</li>
 * </ul>
 * </p>
 *
 * @see HttpClientException
 */
public class HttpClientInvalidRequestException extends HttpClientException {
    public HttpClientInvalidRequestException(String message) {
        super(message);
    }
}
