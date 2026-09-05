package ru.vych.http.impl.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Response {

    /**
     * Уникальный идентификатор исходного запроса.
     * Совпадает с {@link Request#getUuid()}.
     */
    private final String uuid;

    /**
     * Исходный запрос, по которому получен данный ответ.
     */
    private final Request request;

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
     * Возвращает тело ответа, приведённое к типу, указанному в исходном запросе.
     * <p>
     * Выполняет приведение {@link #body} к {@code Request.getResponseClass()} через
     * {@link Class#cast(Object)}. Если {@code body} уже имеет нужный тип — возвращает как есть.
     * </p>
     *
     * @param <T> тип, указанный в {@link Request#getResponseClass()}
     * @return десериализованное тело ответа приведённое к целевому типу
     * @throws ClassCastException если {@link #body} не может быть приведено к целевому типу
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getCastedBody() {
        return (T) request.getResponseClass().cast(body);
    }
}
