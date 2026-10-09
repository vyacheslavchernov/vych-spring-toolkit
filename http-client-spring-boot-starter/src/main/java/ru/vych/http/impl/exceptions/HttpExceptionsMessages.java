package ru.vych.http.impl.exceptions;

/**
 * Набор констант с сообщениями для исключений {@link HttpClientException},
 * {@link HttpClientConfigurationException} и {@link HttpClientHandleResponseException}.
 */
public final class HttpExceptionsMessages {
    // Configuration errors
    public static String CREATION_ERROR_CONFIGURATION_IS_NULL = "Ошибка создания клиента: Конфигурация не может быть null";
    public static String CREATION_ERROR_ROOT_IS_NULL = "Ошибка создания клиента: root не может быть null";
    public static String CREATION_ERROR_COOKIE_POLICY_IS_NULL = "Ошибка создания клиента: политика cookie не может быть null";
    public static String CREATION_ERROR_COOKIES_IS_NULL = "Ошибка создания клиента: дефолтные cookie не могут быть null";
    public static String CREATION_ERROR_INVALID_TIMEOUT_OR_VERSION = "Ошибка создания клиента: невалидный timeout или version";
    public static String CREATION_ERROR_LOG_SERVICE_IS_NULL = "Ошибка создания клиента: logService не может быть null";

    // Request errors
    public static String REQUEST_ERROR_GENERIC = "Ошибка при отправке запроса";
    public static String REQUEST_ERROR_INVALID_METHOD = "Для запроса необходимо указать используемый HTTP метод.";
    public static String REQUEST_ERROR_INVALID_CONTENT_TYPE = "Для POST запроса необходимо указать тип передаваемого контента.";

    // Execute errors
    public static String EXECUTE_ERROR_TIMEOUT = "Ошибка при отправке запроса: таймаут подключения/ответа";
    public static String EXECUTE_ERROR_CONNECTION_REFUSED = "Ошибка при отправке запроса: недоступность сервера";
    public static String EXECUTE_ERROR_DNS = "Ошибка при отправке запроса: DNS ошибка";
    public static String EXECUTE_ERROR_UNKNOWN = "Ошибка при отправке запроса: другая сетевая ошибка";

    // Response errors
    public static String RESPONSE_ERROR_GENERIC = "Ошибка при обработке ответа";
    public static String RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION = "Ошибка при сериализации тела запроса";
    public static String RESPONSE_ERROR_RESPONSE_BODY_DESERIALIZATION = "Ошибка при десериализации тела ответа";

    // Cookie storage errors
    public static String COOKIE_STORAGE_ERROR_READ = "Ошибка чтения cookie-файла";
    public static String COOKIE_STORAGE_ERROR_WRITE = "Ошибка записи cookie-файла";
    public static String COOKIE_STORAGE_ERROR_CORRUPTED = "Cookie-файл повреждён (невалидный JSON)";
    public static String COOKIE_STORAGE_ERROR_HOSTNAME = "Невозможно определить hostname для persistent storage";
}
