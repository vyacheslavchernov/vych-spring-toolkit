package ru.vych.http.impl;

import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;

/**
 * Функциональный интерфейс для выполнения HTTP-запросов с исключениями.
 * <p>
 * Используется менеджером кеша для делегирования выполнения запросов
 * без участия кеширования.
 * </p>
 *
 * @see HttpClientCacheManager
 */
@FunctionalInterface
public interface RequestExecutor {

    /**
     * Выполняет запрос без участия кеша.
     *
     * @param request запрос для выполнения
     * @return результат выполнения запроса
     * @throws Exception если произошла ошибка при выполнении
     */
    Response execute(Request request) throws Exception;
}
