package ru.vych.http.impl.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Результат выполнения HTTP-запроса.
 * <p>
 * Содержит статус-код, тело ответа (в нескольких форматах), заголовки
 * и ссылку на исходный {@link Request}. Тело ответа может быть доступно
 * как raw-байты, raw-строка или десериализованный объект.
 * </p>
 *
 * @see Request
 * @see ru.vych.http.impl.HttpClient#execute(Request)
 */
@Getter
@ToString
@EqualsAndHashCode
public class Response {

    /**
     * Уникальный идентификатор исходного запроса.
     * Совпадает с {@link Request#getUuid()}.
     */
    private String uuid;

    /**
     * Исходный запрос, по которому получен данный ответ.
     */
    private Request request;

    /**
     * HTTP статус-код ответа (200, 404, 500 и т. д.).
     */
    @Setter
    private Integer status;

    /**
     * Тело ответа в виде необработанных байтов.
     */
    @Setter
    private byte[] rawBytes;

    /**
     * Тело ответа в виде строки.
     * <p>
     * Заполняется всегда, кроме случаев, когда статус не OK
     * и {@code responseClass} не указан или равен {@code byte[].class}.
     * </p>
     */
    @Setter
    private String rawBody;

    /**
     * Тело ответа, десериализованное в {@link Request#getResponseClass()}.
     * <p>
     * Заполняется только если статус ответа OK и {@code responseClass}
     * не равен {@code null}, {@code byte.class} или {@code byte[].class}.
     * Для {@code String.class} содержит строку из {@link #rawBody}.
     * </p>
     */
    @Setter
    private Object body;

    /**
     * HTTP-заголовки ответа.
     */
    @Setter
    private List<Header> headers;

    /**
     * Флаг, указывающий, возвращён ли ответ из кеша.
     * <p>
     * {@code true} — ответ возвращён из локального кеша.
     * {@code false} — ответ получен с сервера.
     * </p>
     */
    @Setter
    private boolean isCached;

    /**
     * Время помещения ответа в кеш.
     * <p>
     * Заполняется только если {@link #isCached} равно {@code true}.
     * {@code null} если ответ получен с сервера.
     * </p>
     */
    @Setter
    private Instant cachedAt;

    /**
     * Создаёт новый экземпляр {@link Response} без учёта кеша.
     *
     * @param uuid      уникальный идентификатор запроса
     * @param request   исходный запрос
     * @param status    HTTP статус-код
     * @param body      десериализованное тело ответа
     * @param headers   HTTP-заголовки ответа
     * @return новый экземпляр Response
     */
    public static Response of(String uuid, Request request, Integer status,
                              Object body, List<Header> headers) {
        Response response = new Response();
        response.uuid = uuid;
        response.request = request;
        response.status = status;
        response.body = body;
        response.headers = headers;
        response.isCached = false;
        response.cachedAt = null;
        // rawBody устанавливается из body, если это String
        if (body instanceof String str) {
            response.rawBody = str;
            response.rawBytes = str.getBytes(StandardCharsets.UTF_8);
        }
        return response;
    }

    /**
     * Создаёт новый экземпляр {@link Response} из кешированного ответа.
     *
     * @param base       базовый ответ для клонирования
     * @param uuid       уникальный идентификатор запроса
     * @param request    исходный запрос
     * @param cachedAt   время помещения в кеш
     * @return новый экземпляр Response с флагом isCached=true
     */
    public static Response cached(Response base, String uuid, Request request,
                                  Instant cachedAt) {
        Response response = new Response();
        response.uuid = uuid;
        response.request = request;
        response.status = base.status;
        response.rawBytes = base.rawBytes;
        response.rawBody = base.rawBody;
        response.body = base.body;
        response.headers = base.headers;
        response.isCached = true;
        response.cachedAt = cachedAt;
        return response;
    }

    /**
     * Возвращает тело ответа, приведённое к типу, указанному в исходном запросе.
     * <p>
     * Если {@code responseClass} равен {@code null}, {@code byte.class} или {@code byte[].class},
     * возвращает {@code null}. В остальных случаях выполняет приведение
     * {@link #body} к {@code Request.getResponseClass()} через {@link Class#cast(Object)}.
     * Если {@code body} уже имеет нужный тип — возвращает как есть.
     * </p>
     *
     * @param <T> тип, указанный в {@link Request#getResponseClass()}
     * @return десериализованное тело ответа, приведённое к целевому типу, или {@code null},
     *         если {@code responseClass} равен {@code null}, {@code byte.class} или {@code byte[].class}
     * @throws ClassCastException если {@link #body} не может быть приведено к целевому типу
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getCastedBody() {
        var responseClass = request.getResponseClass();
        if (responseClass == null || responseClass == byte.class || responseClass == byte[].class) {
            return null;
        }
        return (T) request.getResponseClass().cast(body);
    }
}
