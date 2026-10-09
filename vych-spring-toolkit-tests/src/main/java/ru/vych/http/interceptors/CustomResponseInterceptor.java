package ru.vych.http.interceptors;

import org.springframework.stereotype.Component;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.interceptors.ResponseInterceptor;

/**
 * Перехватчик ответов для добавления кастомного заголовка.
 */
@Component
public class CustomResponseInterceptor implements ResponseInterceptor {

    /** Имя кастомного заголовка. */
    public static final String HEADER_NAME = "Custom-Rs-Header-Name";
    /** Значение кастомного заголовка. */
    public static final String HEADER_VALUE = "Custom-Rs-Header-Value";

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
        CustomResponseInterceptor.interceptByUuid = interceptByUuid;
    }

    /**
     * Отключает перехватчик.
     */
    public static void disable() {
        enabled = false;
        CustomResponseInterceptor.interceptByUuid = null;
    }

    /**
     * Обрабатывает ответ, добавляя кастомный заголовок.
     * @param client HTTP клиент
     * @param response обрабатываемый ответ
     */
    @Override
    public void handle(HttpClient client, Response response) {
        if (!enabled) {
            return;
        }

        if (interceptByUuid == null) {
            throw new RuntimeException("UUID перехватываемого запроса не должен быть null");
        }
        response.getHeaders().add(new Header(HEADER_NAME, HEADER_VALUE));
    }
}
