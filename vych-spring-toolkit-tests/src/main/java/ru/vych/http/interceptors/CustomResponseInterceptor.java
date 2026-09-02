package ru.vych.http.interceptors;

import org.springframework.stereotype.Component;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Response;
import ru.vych.http.impl.interceptors.ResponseInterceptor;

@Component
public class CustomResponseInterceptor implements ResponseInterceptor {
    public static final String HEADER_NAME = "Custom-Rs-Header-Name";
    public static final String HEADER_VALUE = "Custom-Rs-Header-Value";

    private static boolean enabled = false;
    private static String interceptByUuid = "";

    public static void enable(String interceptByUuid) {
        enabled = true;
        CustomResponseInterceptor.interceptByUuid = interceptByUuid;
    }

    public static void disable() {
        enabled = false;
        CustomResponseInterceptor.interceptByUuid = null;
    }

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
