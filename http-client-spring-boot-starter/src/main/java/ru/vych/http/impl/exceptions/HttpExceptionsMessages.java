package ru.vych.http.impl.exceptions;

/**
 * Набор констант с сообщениями для исключений {@link HttpClientException},
 * {@link HttpClientConfigurationException} и {@link HttpClientHandleResponseException}.
 */
public final class HttpExceptionsMessages {
    public static String CREATION_ERROR_CONFIGURATION_IS_NULL = "Ошибка создания клиента: Конфигурация не может быть null";
    public static String CREATION_ERROR_CONFIGURATION_IS_INCORRECT = "Ошибка создания клиента: Некорректная конфигурация http-клиента";
    public static String CREATION_ERROR_CONFIGURATION_IS_INCORRECT_ROOT_CANT_BE_NULL = "Ошибка создания клиента: root не может быть null";
    public static String CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIE_HANDLER_CANT_BE_NULL = "Ошибка создания клиента: CookieHandler не может быть null";
    public static String CREATION_ERROR_LOG_SERVICE_IS_NULL = "Ошибка создания клиента: logService не может быть null";
    public static String CREATION_ERROR_CANT_CREATE_COOKIES_HANDLER = "Ошибка создания клиента: Не удалось создать экземпляр хранилища cookie";
    public static String REQUEST_ERROR_GENERIC = "Ошибка при отправке запроса";
    public static String RESPONSE_ERROR_CANT_DESERIALIZE_BODY = "Ошибка при десериализации тела ответа";
    public static String REQUEST_ERROR_CANT_HANDLE_BODY = "Ошибка при обработке тела запроса";
    public static String RESPONSE_ERROR_GENERIC = "Ошибка при обработке ответа";
}
