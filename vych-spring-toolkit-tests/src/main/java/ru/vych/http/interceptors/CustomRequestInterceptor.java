package ru.vych.http.interceptors;

import org.springframework.stereotype.Component;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.interceptors.RequestInterceptor;

@Component
public class CustomRequestInterceptor implements RequestInterceptor {
    public static final String HEADER_NAME = "Custom-Rq-Header-Name";
    public static final String HEADER_VALUE = "Custom-Rq-Header-Value";

    private static boolean enabled = false;
    private static String interceptByUuid = "";

    public static void enable(String interceptByUuid) {
        enabled = true;
        CustomRequestInterceptor.interceptByUuid = interceptByUuid;
    }

    public static void disable() {
        enabled = false;
        CustomRequestInterceptor.interceptByUuid = null;
    }

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
