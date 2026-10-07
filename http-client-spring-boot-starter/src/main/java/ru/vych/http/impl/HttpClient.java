package ru.vych.http.impl;

import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.exceptions.HttpClientException;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * Основной интерфейс HTTP-клиента.
 * <p>
 * Предоставляет метод для выполнения HTTP-запросов ({@link #execute(Request)})
 * и получения обработчика cookie. Реализация — {@link ru.vych.http.impl.HttpClientImpl}.
 * </p>
 * <p>
 * Клиент поддерживает обработку запросов и ответов через
 * {@link ru.vych.http.impl.interceptors.RequestInterceptor} и
 * {@link ru.vych.http.impl.interceptors.ResponseInterceptor}.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClientImpl
 * @see ru.vych.http.config.HttpClientBuilder
 */
public interface HttpClient {

    /**
     * Выполняет HTTP-запрос, описанный в переданном {@link Request}, и возвращает результат.
     * <p>
     * Процесс выполнения:
     * <ol>
     *   <li>Выполняются все {@link ru.vych.http.impl.interceptors.RequestInterceptor}.</li>
     *   <li>Формируется и отправляется HTTP-запрос через {@code java.net.http.HttpClient}.</li>
     *   <li>Ответ парсится: body десериализуется в {@link Request#getResponseClass()} (если указан) или возвращается как raw-байты.</li>
     *   <li>Выполняются все {@link ru.vych.http.impl.interceptors.ResponseInterceptor}.</li>
     * </ol>
     * </p>
     *
     * @param request запрос для выполнения; не должен быть {@code null}
     * @return результат выполнения запроса ({@link Response}); никогда не {@code null}
     * @throws ru.vych.http.impl.exceptions.HttpClientException если произошла ошибка при выполнении запроса,
     *         обработке тела или десериализации ответа
     * @throws ru.vych.http.impl.exceptions.HttpClientInvalidRequestException если запрос не валиден
     *         (не указан метод, для POST не указан Content-Type и т. п.)
     */
    Response execute(Request request) throws HttpClientException;

    /**
     * Возвращает копию хранилища cookies для данного хоста.
     *
     * @param host хост для получения cookies
     * @return список cookies, или пустой список если cookies отсутствуют
     */
    List<HttpCookie> getCookies(String host);

    /**
     * Возвращает полную копию хранилища cookies.
     * <p>
     * Ключ — хост (домен), значение — список {@link HttpCookie} для этого хоста.
     * Возвращаемая карта является неизменяемой копией, модификации не влияют
     * на внутреннее хранилище клиента.
     * </p>
     *
     * @return неизменяемая карта "хост → список cookies"
     */
    Map<String, List<HttpCookie>> getAllCookies();

    /**
     * Очищает хранилище cookies для данного хоста.
     *
     * @param host хост для очистки cookies
     */
    void clearCookies(String host);

    /**
     * Возвращает уникальный идентификатор данного экземпляра клиента.
     * <p>
     * Генерируется один раз при создании клиента и не меняется на протяжении
     * всего времени жизни экземпляра. Используется в логировании.
     * </p>
     *
     * @return UUID клиента в виде строки
     */
    String getClientUuid();
}
