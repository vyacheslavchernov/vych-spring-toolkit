package ru.vych.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.common.HttpStatus;
import ru.vych.http.impl.entities.CookieEntry;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientConfigurationException;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.exceptions.HttpClientExecuteRequestException;
import ru.vych.http.impl.exceptions.HttpClientHandleResponseException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.vych.http.impl.exceptions.HttpExceptionsMessages.*;

/**
 * Полнофункциональная реализация {@link HttpClient} на базе стандартного
 * {@code java.net.http.HttpClient} (Java 11+ HTTP Client API).
 * <p>
 * Поддерживает HTTP-методы GET и POST, пользовательские перехватчики запросов
 * и ответов, автоматическую десериализацию JSON-ответов через Jackson,
 * а также настройку cookie, редиректов и тайм-аутов через {@link HttpClientConfig}.
 * </p>
 * <p>
 * <b>Процесс выполнения запроса:</b>
 * <ol>
 *   <li>Запускаются все {@link ru.vych.http.impl.interceptors.RequestInterceptor}.</li>
 *   <li>Формируется URI из корневого URL конфига + путь запроса + path- и query-параметры.</li>
 *   <li>Добавляются заголовки: сначала дефолтные из конфига, затем — из запроса.</li>
 *   <li>Для POST тело запроса сериализуется (String → строка, byte[] → байты, остальное → JSON через Jackson).</li>
 *   <li>Запрос отправляется через {@code java.net.http.HttpClient}.</li>
 *   <li>Ответ парсится: body десериализуется в {@link ru.vych.http.impl.entities.Request#getResponseClass()}.</li>
 *   <li>Запускаются все {@link ru.vych.http.impl.interceptors.ResponseInterceptor}.</li>
 * </ol>
 * </p>
 *
 * @see HttpClient
 * @see HttpClientConfig
 * @see ru.vych.http.config.HttpClientBuilder
 */
@Slf4j
public class HttpClientImpl implements HttpClient {

    /**
     * Уникальный идентификатор данного экземпляра клиента.
     * Генерируется один раз при создании и используется в логировании.
     */
    @Getter
    private final String clientUuid = UUID.randomUUID().toString();
    private final java.net.http.HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClientConfig config;
    private final HttpClientLogger httpClientLogger;
    private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();

    /**
     * Создаёт и настраивает экземпляр HTTP-клиента.
     * <p>
     * Инициализирует внутренний {@code java.net.http.HttpClient} с параметрами
     * из конфига (тайм-аут, редиректы, версия протокола, cookie-хендлер).
     * При {@code storeCookies = true} создаётся cookie-хранилище и инициализируется
     * дефолтными cookie из {@link HttpClientConfig#getCookies()}.
     * </p>
     *
     * @param config               конфигурация клиента; не должен быть {@code null}
     * @param logService           сервис логирования; не должен быть {@code null}
     * @param requestInterceptors  список перехватчиков запросов; может быть пустым
     * @param responseInterceptors список перехватчиков ответов; может быть пустым
     * @throws ru.vych.http.impl.exceptions.HttpClientConfigurationException если некорректная конфигурация
     *                                                                       или ошибка создания cookie-хранилища
     */
    public HttpClientImpl(
            HttpClientConfig config, LogService logService,
            List<RequestInterceptor> requestInterceptors, List<ResponseInterceptor> responseInterceptors
    ) throws HttpClientException {
        if (config == null) {
            throw new HttpClientConfigurationException(CREATION_ERROR_CONFIGURATION_IS_NULL);
        }
        if (logService == null) {
            throw new HttpClientConfigurationException(CREATION_ERROR_LOG_SERVICE_IS_NULL);
        }

        if (config.getRoot() == null) {
            throw new HttpClientConfigurationException(CREATION_ERROR_CONFIGURATION_IS_INCORRECT_ROOT_CANT_BE_NULL);
        }
        this.config = config;
        this.httpClientLogger = new HttpClientLogger(config, logService);

        if (requestInterceptors != null) {
            this.requestInterceptors.addAll(requestInterceptors);
        }
        if (responseInterceptors != null) {
            this.responseInterceptors.addAll(responseInterceptors);
        }

        java.net.http.HttpClient.Builder clientBuilder;
        try {
            clientBuilder = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(config.getTimeout())
                    .followRedirects(config.getRedirectPolicy())
                    .version(config.getVersion());
        } catch (IllegalArgumentException | NullPointerException e) {
            httpClientLogger.error(
                    config.getServiceCode(), clientUuid, CREATION_ERROR_CONFIGURATION_IS_INCORRECT,
                    config, e.toString()
            );
            throw new HttpClientConfigurationException(CREATION_ERROR_CONFIGURATION_IS_INCORRECT, e);
        }

        CookieManager cookieHandler = new CookieManager();

        if (config.getCookiePolicy() == null) {
            throw new HttpClientConfigurationException(CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIE_POLICY_CANT_BE_NULL);
        }

        switch (config.getCookiePolicy()) {
            case ACCEPT_ALL:
                cookieHandler.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                break;
            case ACCEPT_NONE:
                cookieHandler.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
                break;
            case ACCEPT_ORIGINAL_SERVER:
                cookieHandler.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
                break;
        }

        if (config.getCookies() == null) {
            throw new HttpClientConfigurationException(CREATION_ERROR_CONFIGURATION_IS_INCORRECT_COOKIES_CANT_BE_NULL);
        }
        for (CookieEntry cookie : config.getCookies()) {
            cookieHandler.getCookieStore().add(cookie.getUri(), cookie.getCookie());
        }
        clientBuilder.cookieHandler(cookieHandler);

        this.client = clientBuilder.build();

        httpClientLogger.info(
                true, config.getServiceCode(), clientUuid,
                "Инициализирован Http-Client", config
        );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Возвращает внутренний {@link CookieHandler} из настроенного
     * {@code java.net.http.HttpClient}. Если cookie-хранилище не настроено,
     * возвращает {@code null}.
     * </p>
     */
    @Override
    public CookieHandler getCookieHandler() {
        return this.client.cookieHandler().orElse(null);
    }

    @Override
    public Response execute(Request request) throws HttpClientException {
        requestInterceptors.forEach(filter -> {
            httpClientLogger.debug(
                    config.getServiceCode(), request.getUuid(),
                    "Выполнение интерцептора запроса", filter.getClass().getCanonicalName()
            );
            filter.handle(this, request);
        });
        httpClientLogger.debug(config.getServiceCode(), request.getUuid(), "Отправка Http-запроса", request);

        Response response = switch (request.getMethod()) {
            case GET -> get(request);
            case POST -> post(request);
        };

        responseInterceptors.forEach(filter -> {
            httpClientLogger.debug(
                    config.getServiceCode(), request.getUuid(),
                    "Выполнение интерцептора ответа", filter.getClass().getCanonicalName()
            );
            filter.handle(this, response);
        });
        httpClientLogger.debug(config.getServiceCode(), request.getUuid(), "Получен ответ", response);

        return response;
    }

    private Response get(Request request) throws HttpClientException {
        var requestBuilder = HttpRequest.newBuilder(buildUri(request));
        addHeaders(requestBuilder, request);
        requestBuilder.GET();

        HttpResponse<byte[]> rs;
        try {
            rs = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            httpClientLogger.error(
                    config.getServiceCode(), clientUuid, REQUEST_ERROR_GENERIC,
                    request, e.toString()
            );
            throw new HttpClientExecuteRequestException(REQUEST_ERROR_GENERIC, e);
        }
        return buildResponse(rs, request);
    }

    private Response post(Request request) throws HttpClientException {
        Builder requestBuilder = HttpRequest.newBuilder(buildUri(request));
        addHeaders(requestBuilder, request);
        requestBuilder.POST(buildBody(request));

        HttpResponse<byte[]> rs;
        try {
            rs = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            httpClientLogger.error(
                    config.getServiceCode(), clientUuid, REQUEST_ERROR_GENERIC,
                    request, e.toString());
            throw new HttpClientExecuteRequestException(REQUEST_ERROR_GENERIC, e);
        }
        return buildResponse(rs, request);
    }

    /**
     * Формирует {@link HttpRequest.BodyPublisher} из тела запроса.
     * <p>
     * Поддерживает три типа payload:
     * <ul>
     *   <li>{@code String} → отправляется как строка в UTF-8</li>
     *   <li>{@code byte[]} → отправляется как байтовый массив</li>
     *   <li>Любой другой объект → сериализуется в JSON через Jackson</li>
     *   <li>{@code null} → отправляется пустое тело</li>
     * </ul>
     * </p>
     *
     * @param request запрос, содержащий тело
     * @return {@link HttpRequest.BodyPublisher} для отправки тела
     * @throws ru.vych.http.impl.exceptions.HttpClientHandleResponseException если не удалось сериализовать payload в JSON
     */
    private HttpRequest.BodyPublisher buildBody(Request request) throws HttpClientException {
        Object payload = request.getPayload();

        if (payload == null) {
            return HttpRequest.BodyPublishers.noBody();
        }

        try {
            if (payload instanceof String text) {
                return HttpRequest.BodyPublishers.ofString(text, StandardCharsets.UTF_8);
            }

            if (payload instanceof byte[] bytes) {
                return HttpRequest.BodyPublishers.ofByteArray(bytes);
            }

            return HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(payload),
                    StandardCharsets.UTF_8
            );

        } catch (JsonProcessingException e) {
            httpClientLogger.error(
                    config.getServiceCode(), clientUuid, REQUEST_ERROR_CANT_HANDLE_BODY,
                    payload, e.toString());
            throw new HttpClientHandleResponseException(REQUEST_ERROR_CANT_HANDLE_BODY, e);
        }
    }

    /**
     * Формирует полный URI для запроса.
     * <p>
     * Собирает URI из:
     * <ol>
     *   <li>Корневого URL из {@link HttpClientConfig#getRoot()}.</li>
     *   <li>Пути из {@link ru.vych.http.impl.entities.Request#getUrl()}.</li>
     *   <li>Path-параметров из {@link ru.vych.http.impl.entities.Request#getPathParams()} — вставляются как части пути.</li>
     *   <li>Query-параметров из {@link ru.vych.http.impl.entities.Request#getQueryParams()} — форматируются как {@code key=value&...}.</li>
     * </ol>
     * </p>
     *
     * @param request запрос, содержащий путь и параметры
     * @return полный URI для HTTP-запроса
     */
    private URI buildUri(Request request) {
        var root = config.getRoot().endsWith("/") ? config.getRoot() : config.getRoot() + "/";

        var path = request.getUrl().startsWith("/") ? request.getUrl().substring(1) : request.getUrl();
        var pathParams = String.join("/", request.getPathParams());

        var queryParams = request.getQueryParams().entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        var fullPathBuilder = new StringBuilder(root);
        fullPathBuilder.append(path);
        if (!pathParams.isEmpty()) {
            fullPathBuilder.append("/");
            fullPathBuilder.append(pathParams);
        }
        if (!queryParams.isEmpty()) {
            fullPathBuilder.append("?");
            fullPathBuilder.append(queryParams);
        }
        return URI.create(fullPathBuilder.toString());
    }

    /**
     * Добавляет HTTP-заголовки к builder'у запроса.
     * <p>
     * Сначала добавляются заголовки из {@link HttpClientConfig#getHeaders()},
     * затем — заголовки из {@link ru.vych.http.impl.entities.Request#getHeaders()}
     * (могут переопределить предыдущие).
     * </p>
     *
     * @param builder builder для {@link HttpRequest}
     * @param request запрос, содержащий дополнительные заголовки
     */
    private void addHeaders(Builder builder, Request request) {
        config.getHeaders().forEach(builder::header);
        request.getHeaders().forEach(header -> builder.header(header.name(), header.value()));
    }

    /**
     * Десериализует тело ответа в указанный класс.
     * <p>
     * <ul>
     *   <li>{@code String.class} → возвращает тело как строку</li>
     *   <li>{@code null}, {@code byte.class}, {@code byte[].class} → возвращает {@code null}</li>
     *   <li>Любой другой класс → десериализует JSON через Jackson</li>
     * </ul>
     * </p>
     *
     * @param body          тело ответа в виде строки
     * @param responseClass целевой класс для десериализации
     * @return десериализованный объект или {@code null}
     * @throws ru.vych.http.impl.exceptions.HttpClientHandleResponseException если не удалось десериализовать JSON
     */
    private Object mapBodyToResponseClass(String body, Class<?> responseClass) throws HttpClientException {
        if (responseClass == String.class) {
            return body;
        }

        if (responseClass == null || responseClass == byte.class || responseClass == byte[].class) {
            return null;
        }

        try {
            return mapper.readValue(body, responseClass);
        } catch (JsonProcessingException e) {
            httpClientLogger.error(
                    config.getServiceCode(), clientUuid, RESPONSE_ERROR_CANT_DESERIALIZE_BODY,
                    body, responseClass, e.toString());
            throw new HttpClientHandleResponseException(RESPONSE_ERROR_CANT_DESERIALIZE_BODY, e);
        }
    }

    /**
     * Формирует {@link Response} из сырого {@link HttpResponse}.
     * <p>
     * Декодирует тело в UTF-8, извлекает статус-код и заголовки.
     * Если статус OK и указан {@code responseClass} — десериализует body в этот класс.
     * В противном случае body хранится как raw-строка.
     * </p>
     *
     * @param httpResponse сырой HTTP-ответ от {@code java.net.http.HttpClient}
     * @param request      исходный запрос, содержащий {@code responseClass}
     * @return сконструированный {@link Response}
     * @throws ru.vych.http.impl.exceptions.HttpClientHandleResponseException если не удалось десериализовать body
     */
    protected Response buildResponse(HttpResponse<byte[]> httpResponse, Request request) throws HttpClientException {
        String bodyText = new String(httpResponse.body(), StandardCharsets.UTF_8);
        var rsType = request.getResponseClass();
        return new Response(
                request.getUuid(),
                request,
                httpResponse.statusCode(),
                httpResponse.body(),
                httpResponse.statusCode() != HttpStatus.OK || rsType != null && rsType != byte.class && rsType != byte[].class
                        ? bodyText
                        : null,
                httpResponse.statusCode() != HttpStatus.OK
                        ? null
                        : mapBodyToResponseClass(bodyText, request.getResponseClass()),
                extractHeaders(httpResponse)
        );
    }

    /**
     * Извлекает все заголовки из HTTP-ответа.
     * <p>
     * Каждый pair (имя, значение) преобразуется в отдельный {@link Header}.
     * Если заголовок имеет несколько значений, для каждого создаётся отдельный {@link Header}.
     * </p>
     *
     * @param httpResponse HTTP-ответ
     * @return список всех заголовков ответа
     */
    protected List<Header> extractHeaders(HttpResponse<byte[]> httpResponse) {
        List<Header> headers = new ArrayList<>();
        httpResponse.headers().map().forEach((name, values) -> {
            values.forEach(value -> {
                headers.add(new Header(name, value));
            });
        });
        return headers;
    }
}
