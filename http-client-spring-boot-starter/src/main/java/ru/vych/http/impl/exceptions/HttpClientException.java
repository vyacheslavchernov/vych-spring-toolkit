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
    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
