package ru.vych.http.impl.exceptions;

/**
 * Исключение, выбрасываемое при некорректной конфигурации HTTP-клиента.
 * <p>
 * Возникает в {@link ru.vych.http.impl.HttpClientImpl} при:
 * <ul>
 *   <li>Некорректном значении тайм-аута или версии протокола</li>
 *   <li>Невозможности создать экземпляр {@link java.net.CookieHandler} через рефлекссию</li>
 * </ul>
 * </p>
 *
 * @see HttpClientException
 */
public class HttpClientConfigurationException extends HttpClientException {
    public HttpClientConfigurationException(String message) {
        super(message);
    }

    public HttpClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
