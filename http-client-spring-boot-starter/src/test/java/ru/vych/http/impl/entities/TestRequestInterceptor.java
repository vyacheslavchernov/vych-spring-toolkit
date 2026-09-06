package ru.vych.http.impl.entities;

import ru.vych.http.impl.interceptors.RequestInterceptor;

/**
 * Тестовый {@link RequestInterceptor} с пустой реализацией.
 */
public class TestRequestInterceptor implements RequestInterceptor {
    @Override
    public void handle(ru.vych.http.impl.HttpClient client, Request request) {

    }
}
