package ru.vych.http.impl.interceptors;

import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Request;

/**
 * Интерфейс перехватчика (interceptor) для предварительной обработки HTTP-запросов.
 * <p>
 * Интерсепторы могут использоваться для:
 * <ul>
 *   <li>Добавления или модификации заголовков</li>
 *   <li>Логирования параметров запроса</li>
 *   <li>Добавления аутентификационных токенов</li>
 *   <li>Модификации тела запроса</li>
 * </ul>
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient
 * @see ResponseInterceptor
 */
public interface RequestInterceptor {

    /**
     * Выполняет предварительную обработку запроса перед его отправкой.
     * <p>
     * Метод может модифицировать переданный {@link Request} (заголовки, payload и т. д.)
     * или выполнять побочные действия (логирование, метрики).
     * </p>
     *
     * @param client  клиент, который будет выполнять запрос; не {@code null}
     * @param request запрос для предварительной обработки; не {@code null}
     */
    void handle(HttpClient client, Request request);
}
