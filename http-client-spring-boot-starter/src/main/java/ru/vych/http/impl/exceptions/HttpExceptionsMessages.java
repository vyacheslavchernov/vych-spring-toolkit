package ru.vych.http.impl.exceptions;

/**
 * Набор констант с сообщениями для исключений {@link HttpClientException},
 * {@link HttpClientConfigurationException} и {@link HttpClientHandleResponseException}.
 */
public final class HttpExceptionsMessages {
    /** Configuration errors. */
    public static String CREATION_ERROR_CONFIGURATION_IS_NULL
            = "Ошибка создания клиента: Конфигурация не может быть null";
    /** Root URL is null. */
    public static String CREATION_ERROR_ROOT_IS_NULL = "Ошибка создания клиента: root не может быть null";
    /** Cookie policy is null. */
    public static String CREATION_ERROR_COOKIE_POLICY_IS_NULL
            = "Ошибка создания клиента: политика cookie не может быть null";
    /** Default cookies are null. */
    public static String CREATION_ERROR_COOKIES_IS_NULL
            = "Ошибка создания клиента: дефолтные cookie не могут быть null";
    /** Invalid timeout or protocol version. */
    public static String CREATION_ERROR_INVALID_TIMEOUT_OR_VERSION
            = "Ошибка создания клиента: невалидный timeout или version";
    /** Log service is null. */
    public static String CREATION_ERROR_LOG_SERVICE_IS_NULL = "Ошибка создания клиента: logService не может быть null";

    /** Generic request error. */
    public static String REQUEST_ERROR_GENERIC = "Ошибка при отправке запроса";
    /** HTTP method is not set. */
    public static String REQUEST_ERROR_INVALID_METHOD
            = "Для запроса необходимо указать используемый HTTP метод.";
    /** Content-Type is missing for POST request. */
    public static String REQUEST_ERROR_INVALID_CONTENT_TYPE
            = "Для POST запроса необходимо указать тип передаваемого контента.";

    /** Timeout error during request execution. */
    public static String EXECUTE_ERROR_TIMEOUT = "Ошибка при отправке запроса: таймаут подключения/ответа";
    /** Connection refused error. */
    public static String EXECUTE_ERROR_CONNECTION_REFUSED = "Ошибка при отправке запроса: недоступность сервера";
    /** DNS resolution error. */
    public static String EXECUTE_ERROR_DNS = "Ошибка при отправке запроса: DNS ошибка";
    /** Unknown network error. */
    public static String EXECUTE_ERROR_UNKNOWN = "Ошибка при отправке запроса: другая сетевая ошибка";

    /** Generic response error. */
    public static String RESPONSE_ERROR_GENERIC = "Ошибка при обработке ответа";
    /** Request body serialization error. */
    public static String RESPONSE_ERROR_REQUEST_BODY_SERIALIZATION = "Ошибка при сериализации тела запроса";
    /** Response body deserialization error. */
    public static String RESPONSE_ERROR_RESPONSE_BODY_DESERIALIZATION = "Ошибка при десериализации тела ответа";

    /** Cookie file read error. */
    public static String COOKIE_STORAGE_ERROR_READ = "Ошибка чтения cookie-файла";
    /** Cookie file write error. */
    public static String COOKIE_STORAGE_ERROR_WRITE = "Ошибка записи cookie-файла";
    /** Cookie file is corrupted. */
    public static String COOKIE_STORAGE_ERROR_CORRUPTED = "Cookie-файл повреждён (невалидный JSON)";
    /** Cannot determine hostname for persistent storage. */
    public static String COOKIE_STORAGE_ERROR_HOSTNAME = "Невозможно определить hostname для persistent storage";
}
