package ru.vych.http.interceptors;

import org.springframework.stereotype.Component;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.interceptors.RequestInterceptor;

/**
 * Перехватчик запросов для добавления кастомного заголовка.
 */
@Component
public class CustomRequestInterceptor implements RequestInterceptor {

    /** Имя кастомного заголовка. */
    public static final String HEADER_NAME = "Custom-Rq-Header-Name";
    /** Значение кастомного заголовка. */
    public static final String HEADER_VALUE = "Custom-Rq-Header-Value";

    /** Флаг включения перехватчика. */
    private static boolean enabled = false;
    /** UUID перехватываемого запроса. */
    private static String interceptByUuid = "";

    /**
     * Включает перехватчик для указанного UUID.
     * @param interceptByUuid UUID перехватываемого запроса
     */
    public static void enable(String interceptByUuid) {
        enabled = true;
        CustomRequestInterceptor.interceptByUuid = interceptByUuid;
    }

    /**
     * Отключает перехватчик.
     */
    public static void disable() {
        enabled = false;
        CustomRequestInterceptor.interceptByUuid = null;
    }

    /**
     * Обрабатывает запрос, добавляя кастомный заголовок.
     * @param client HTTP клиент
     * @param request обрабатываемый запрос
     */
    @Override
    public void handle(HttpClient client, Request request) {
        if (!enabled) {
            return;
        }

        if (interceptByUuid == null) {
            throw new RuntimeException("UUID перехватываемого запроса не должен быть null");
        }
        request.getHeaders().add(new Header(HEADER_NAME, HEADER_VALUE));
    }
}
