package ru.vych.http.impl.interceptors;

import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Response;

/**
 * Интерфейс перехватчика (interceptor) для последующей обработки HTTP-ответов.
 * <p>
 * Все зарегистрированные {@code ResponseInterceptor} вызываются последовательно
 * в порядке добавления после получения каждого ответа через
 * {@link ru.vych.http.impl.HttpClient#execute(Request)}.
 * </p>
 * <p>
 * Интерсепторы могут использоваться для:
 * <ul>
 *   <li>Валидации ответа (статус-код, заголовки)</li>
 *   <li>Логирования результатов запросов</li>
 *   <li>Обработки ошибок (4xx, 5xx)</li>
 *   <li>Модификации тела ответа</li>
 * </ul>
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient
 * @see RequestInterceptor
 */
public interface ResponseInterceptor {

    /**
     * Выполняет обработку ответа после его получения.
     * <p>
     * Метод может анализировать {@link Response} (статус-код, тело, заголовки)
     * или выполнять побочные действия (логирование, метрики).
     * </p>
     *
     * @param client   клиент, который выполнял запрос; не должен быть {@code null}
     * @param response ответ для обработки; не должен быть {@code null}
     */
    void handle(HttpClient client, Response response);
}
