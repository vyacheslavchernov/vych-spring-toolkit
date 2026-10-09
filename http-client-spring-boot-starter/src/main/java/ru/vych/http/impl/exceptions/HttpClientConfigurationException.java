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
    /**
     * Создаёт исключение с указанной сообщением.
     *
     * @param message сообщение об ошибке
     */
    public HttpClientConfigurationException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанной сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина возникновения исключения
     */
    public HttpClientConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
