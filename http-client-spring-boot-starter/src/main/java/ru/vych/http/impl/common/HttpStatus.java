package ru.vych.http.impl.common;

/**
 * Константы стандартных HTTP статус-кодов (IANA HTTP Status Code Registry).
 * <p>
 * Класс не имеет публичных конструкторов и не может быть инстанциирован.
 * Содержит только {@code public static final int} константы для всех стандартных
 * HTTP статус-кодов, сгруппированных по категориям:
 * </p>
 * <ul>
 *   <li>1xx — информационные</li>
 *   <li>2xx — успешные</li>
 *   <li>3xx — редиректы</li>
 *   <li>4xx — ошибки клиента</li>
 *   <li>5xx — ошибки сервера</li>
 * </ul>
 *
 * @see ru.vych.http.impl.entities.Response#getStatus()
 */
public final class HttpStatus {
    // 1xx Informational
    /** 100 Continue — сервер получил заголовки, клиент может отправить тело запроса. */
    public static final int CONTINUE = 100;
    /** 101 Switching Protocols — сервер переключает протокол (например, на WebSocket). */
    public static final int SWITCHING_PROTOCOLS = 101;
    /** 102 Processing — сервер обрабатывает запрос, но ещё не завершил. */
    public static final int PROCESSING = 102;
    /** 103 Early Hints — сервер возвращает часть заголовков до завершения обработки. */
    public static final int EARLY_HINTS = 103;

    // 2xx Success
    /** 200 OK — запрос успешно выполнен. */
    public static final int OK = 200;
    /** 201 Created — ресурс успешно создан. */
    public static final int CREATED = 201;
    /** 202 Accepted — запрос принят в обработку, но ещё не завершён. */
    public static final int ACCEPTED = 202;
    /** 203 Non-Authoritative Information — информация из другого источника. */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    /** 204 No Content — запрос успешен, но тела ответа нет. */
    public static final int NO_CONTENT = 204;
    /** 205 Reset Content — клиент должен сбросить состояние формы. */
    public static final int RESET_CONTENT = 205;
    /** 206 Partial Content — часть ресурса (для range-запросов). */
    public static final int PARTIAL_CONTENT = 206;
    /** 207 Multi-Status — несколько статусов (для WebDAV). */
    public static final int MULTI_STATUS = 207;
    /** 208 Already Reported — уже возвращался в ответе DAV. */
    public static final int ALREADY_REPORTED = 208;
    /** 226 IM Used — операция выполнена, ответ содержит инстанс. */
    public static final int IM_USED = 226;

    // 3xx Redirection
    /** 300 Multiple Choices — несколько вариантов ответа. */
    public static final int MULTIPLE_CHOICES = 300;
    /** 301 Moved Permanently — ресурс permanently перемещён. */
    public static final int MOVED_PERMANENTLY = 301;
    /** 302 Found — временный редирект. */
    public static final int FOUND = 302;
    /** 303 See Other — ответ доступен по другому URL (после POST). */
    public static final int SEE_OTHER = 303;
    /** 304 Not Modified — ресурс не изменялся, клиент может использовать кэш. */
    public static final int NOT_MODIFIED = 304;
    /** 305 Use Proxy — устаревший, использовался с HTTP/1.0. */
    public static final int USE_PROXY = 305;
    /** 307 Temporary Redirect — временный редирект с сохранением метода. */
    public static final int TEMPORARY_REDIRECT = 307;
    /** 308 Permanent Redirect — постоянный редирект с сохранением метода. */
    public static final int PERMANENT_REDIRECT = 308;

    // 4xx Client Error
    /** 400 Bad Request — некорректный запрос от клиента. */
    public static final int BAD_REQUEST = 400;
    /** 401 Unauthorized — требуется аутентификация. */
    public static final int UNAUTHORIZED = 401;
    /** 402 Payment Required — зарезервировано. */
    public static final int PAYMENT_REQUIRED = 402;
    /** 403 Forbidden — сервер отказывает в доступе. */
    public static final int FORBIDDEN = 403;
    /** 404 Not Found — ресурс не найден. */
    public static final int NOT_FOUND = 404;
    /** 405 Method Not Allowed — метод не поддерживается для данного ресурса. */
    public static final int METHOD_NOT_ALLOWED = 405;
    /** 406 Not Acceptable — сервер не может вернуть ответ в запрошенном формате. */
    public static final int NOT_ACCEPTABLE = 406;
    /** 407 Proxy Authentication Required — нужна аутентификация через прокси. */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    /** 408 Request Timeout — сервер ждал слишком долго. */
    public static final int REQUEST_TIMEOUT = 408;
    /** 409 Conflict — конфликт состояния ресурса. */
    public static final int CONFLICT = 409;
    /** 410 Gone — ресурс больше недоступен. */
    public static final int GONE = 410;
    /** 411 Length Required — отсутствует Content-Length. */
    public static final int LENGTH_REQUIRED = 411;
    /** 412 Precondition Failed — условие в заголовке не выполнено. */
    public static final int PRECONDITION_FAILED = 412;
    /** 413 Payload Too Large — тело запроса слишком большое. */
    public static final int PAYLOAD_TOO_LARGE = 413;
    /** 414 URI Too Long — URI превышает допустимую длину. */
    public static final int URI_TOO_LONG = 414;
    /** 415 Unsupported Media Type — неподдерживаемый тип контента. */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    /** 416 Range Not Satisfiable — диапазон не может быть предоставлен. */
    public static final int RANGE_NOT_SATISFIABLE = 416;
    /** 417 Expectation Failed — условие Expect не выполнено. */
    public static final int EXPECTATION_FAILED = 417;
    /** 418 I'm a teapot — Easter egg (RFC 2324). */
    public static final int IM_A_TEAPOT = 418;
    /** 421 Misdirected Request — запрос направлен не на тот сервер. */
    public static final int MISDIRECTED_REQUEST = 421;
    /** 422 Unprocessable Content — сервер понимает контент, но не может обработать. */
    public static final int UNPROCESSABLE_CONTENT = 422;
    /** 423 Locked — ресурс заблокирован. */
    public static final int LOCKED = 423;
    /** 424 Failed Dependency — ошибка из-за зависимости (WebDAV). */
    public static final int FAILED_DEPENDENCY = 424;
    /** 425 Too Early — сервер не может обработать для предотвращения replay-атаки. */
    public static final int TOO_EARLY = 425;
    /** 426 Upgrade Required — клиент должен переключиться на другой протокол. */
    public static final int UPGRADE_REQUIRED = 426;
    /** 428 Precondition Required — требуется заголовок Precondition. */
    public static final int PRECONDITION_REQUIRED = 428;
    /** 429 Too Many Requests — превышен лимит запросов. */
    public static final int TOO_MANY_REQUESTS = 429;
    /** 431 Request Header Fields Too Large — заголовки слишком большие. */
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
    /** 451 Unavailable For Legal Reasons — недоступно по юридическим причинам. */
    public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;

    // 5xx Server Error
    /** 500 Internal Server Error — внутренняя ошибка сервера. */
    public static final int INTERNAL_SERVER_ERROR = 500;
    /** 501 Not Implemented — метод не поддерживается сервером. */
    public static final int NOT_IMPLEMENTED = 501;
    /** 502 Bad Gateway — сервер-шлюз получил некорректный ответ от вышестоящего. */
    public static final int BAD_GATEWAY = 502;
    /** 503 Service Unavailable — сервер временно недоступен. */
    public static final int SERVICE_UNAVAILABLE = 503;
    /** 504 Gateway Timeout — тайм-аут сервера-шлюза. */
    public static final int GATEWAY_TIMEOUT = 504;
    /** 505 HTTP Version Not Supported — версия HTTP не поддерживается. */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    /** 506 Variant Also Negotiates — серверная ошибка конфигурации. */
    public static final int VARIANT_ALSO_NEGOTIATES = 506;
    /** 507 Insufficient Storage — сервер не может обработать (WebDAV). */
    public static final int INSUFFICIENT_STORAGE = 507;
    /** 508 Loop Detected — обнаружен цикл (WebDAV). */
    public static final int LOOP_DETECTED = 508;
    /** 510 Not Extended — нужны дополнительные расширения. */
    public static final int NOT_EXTENDED = 510;
    /** 511 Network Authentication Required — требуется аутентификация сети. */
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;
}
