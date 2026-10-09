package ru.vych.http.impl.exceptions;

/**
 * Корневое checked-исключение для всех ошибок HTTP-клиента.
 * <p>
 * Все специализированные исключения (конфигурация, выполнение запроса,
 * обработка ответа, невалидный запрос) наследуются от этого класса.
 * </p>
 *
 * @see HttpClientConfigurationException
 * @see HttpClientExecuteRequestException
 * @see HttpClientHandleResponseException
 * @see HttpClientInvalidRequestException
 */
public class HttpClientException extends Exception {
    /**
     * Создаёт исключение с указанной сообщением.
     *
     * @param message сообщение об ошибке
     */
    public HttpClientException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанной сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина возникновения исключения
     */
    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
