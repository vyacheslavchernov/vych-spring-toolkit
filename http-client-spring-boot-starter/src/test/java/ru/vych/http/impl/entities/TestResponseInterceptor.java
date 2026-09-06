package ru.vych.http.impl.entities;

import ru.vych.http.impl.interceptors.ResponseInterceptor;

/**
 * Тестовый {@link ResponseInterceptor} с пустой реализацией.
 */
public class TestResponseInterceptor implements ResponseInterceptor {
    /**
     * Пустая реализация для тестирования.
     */
    @Override
    public void handle(ru.vych.http.impl.HttpClient client, Response response) {

    }
}
