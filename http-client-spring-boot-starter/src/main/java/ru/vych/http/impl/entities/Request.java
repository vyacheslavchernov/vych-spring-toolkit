package ru.vych.http.impl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.exceptions.HttpClientInvalidRequestException;

import java.util.*;

/**
 * Описание HTTP-запроса для выполнения через {@link ru.vych.http.impl.HttpClient}.
 * <p>
 * Содержит все параметры запроса: URL, метод, query/path-параметры, заголовки,
 * тело (payload) и ожидаемый класс для десериализации ответа.
 * </p>
 * <p>
 * Создаётся через pattern Builder: {@code Request.builder().method(GET).url("/api").build()}.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient#execute(Request)
 * @see Request.Builder
 */
@Getter
@AllArgsConstructor
@Accessors(chain = true)
@ToString
public class Request {

    /**
     * Уникальный идентификатор данного экземпляра запроса.
     * Используется в логировании для отслеживания конкретного запроса.
     */
    private final String uuid = UUID.randomUUID().toString();

    /**
     * Путь API (относительный URL), который будет добавлен к {@link HttpClientConfig#getRoot()}.
     * <p>Может содержать параметры пути, которые подставляются через {@link #pathParams}.</p>
     */
    @Setter
    private String url;

    /**
     * HTTP-метод запроса (GET или POST).
     * <p>Обязательный параметр — должен быть установлен перед вызовом {@link Builder#build()}.</p>
     *
     * @see HttpMethod
     */
    @Setter
    private HttpMethod method;

    /**
     * Query-параметры URL в виде мапа "ключ → значение".
     * <p>Ключи {@code null} автоматически удаляются при установке.</p>
     */
    @Setter
    private Map<String, String> queryParams;

    /**
     * Path-параметры URL — вставляются как части пути после основного {@link #url}.
     * <p>Например: url = "/users", pathParams = ["42"] → "/users/42".</p>
     */
    @Setter
    private List<String> pathParams;

    /**
     * Дополнительные HTTP-заголовки запроса.
     * <p>
     * Добавляются поверх дефолтных заголовков из {@link HttpClientConfig#getHeaders()}.
     * Могут переопределять значения из конфига.
     * </p>
     */
    @Setter
    private List<Header> headers;

    /**
     * Ожидаемый класс для десериализации тела ответа.
     * <p>
     * Если не {@code null} и статус ответа OK, тело ответа будет десериализовано
     * в этот класс через Jackson. Если {@code String.class} — тело вернётся как строка.
     * Если {@code byte.class} или {@code byte[].class} — десериализация не выполняется.
     * </p>
     *
     * @see ru.vych.http.impl.entities.Response#getCastedBody()
     */
    @Setter
    private Class<?> responseClass;

    /**
     * Тело запроса (payload).
     * <p>
     * Для POST-запросов: может быть {@code String}, {@code byte[]} или любой сериализуемый
     * в JSON объект. Для GET-запросов обычно {@code null}.
     * </p>
     */
    @Setter
    private Object payload;

    /**
     * Создаёт новый {@code Builder} для построения {@link Request}.
     *
     * @return новый экземпляр Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder для построения {@link Request}.
     * <p>
     * Поддерживает цепочечные вызовы (chain API). Валидирует обязательные
     * параметры при вызове {@link #build()}: должен быть указан метод, а для POST
     * — должен быть установлен Content-Type.
     * </p>
     *
     * @see Request#builder()
     */
    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private String url = "";
        private HttpMethod method;
        private Map<String, String> queryParams = new HashMap<>();
        private List<String> pathParams = new LinkedList<>();
        private List<Header> headers = new LinkedList<>();
        private Class<?> responseClass;
        private Object payload;

        private String contentType;

        /**
         * Устанавливает query-параметры запроса.
         * <p>
         * Удаляет все записи с ключом {@code null} из переданного мапа.
         * </p>
         *
         * @param params мап query-параметров
         * @return этот же builder для цепочечных вызовов
         */
        public Builder setQueryParams(Map<String, String> params) {
            params.remove(null);
            this.queryParams = params;
            return this;
        }

        /**
         * Добавляет один query-параметр.
         * <p>
         * Если ключ {@code null}, параметр не добавляется.
         * </p>
         *
         * @param key   ключ параметра
         * @param value значение параметра
         * @return этот же builder для цепочечных вызовов
         */
        public Builder addQueryParam(String key, String value) {
            if (key != null) {
                queryParams.put(key, value);
            }
            return this;
        }

        /**
         * Добавляет path-параметр.
         * <p>
         * Path-параметры вставляются как части пути после основного URL.
         * </p>
         *
         * @param value значение path-параметра
         * @return этот же builder для цепочечных вызовов
         */
        public Builder addPathParam(String value) {
            pathParams.add(value);
            return this;
        }

        /**
         * Добавляет HTTP-заголовок.
         *
         * @param name  имя заголовка
         * @param value значение заголовка
         * @return этот же builder для цепочечных вызовов
         */
        public Builder addHeader(String name, String value) {
            headers.add(new Header(name, value));
            return this;
        }

        /**
         * Создаёт {@link Request} из установленных параметров.
         * <p>
         * <b>Валидация:</b>
         * <ul>
         *   <li>Метод должен быть указан — иначе {@link HttpClientInvalidRequestException}.</li>
         *   <li>Для POST с телом должен быть установлен Content-Type — иначе {@link HttpClientInvalidRequestException}.</li>
         * </ul>
         * </p>
         *
         * @return новый валидированный экземпляр {@link Request}
         * @throws HttpClientInvalidRequestException если запрос не проходит валидацию
         */
        public Request build() throws HttpClientInvalidRequestException {
            if (method == null) {
                throw new HttpClientInvalidRequestException("Для запроса необходимо указать используемый HTTP метод.");
            }

            if ((method == HttpMethod.POST && payload != null) && (contentType == null || contentType.isEmpty())) {
                throw new HttpClientInvalidRequestException("Для POST запроса необходимо указать тип передаваемого контента.");
            }

            if (contentType != null) {
                this.addHeader("Content-Type", contentType);
            }

            return new Request(
                    url, method, queryParams,
                    pathParams, headers, responseClass,
                    payload
            );
        }
    }
}
