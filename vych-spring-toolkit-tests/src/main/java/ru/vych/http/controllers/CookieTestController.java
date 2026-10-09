package ru.vych.http.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * JAX-RS контроллер для тестирования работы с cookies.
 */
@Path("/cookieTest")
public class CookieTestController {

    /** Имя тестового cookie. */
    public static final String COOKIE_NAME = "test_cookie";
    /** Значение тестового cookie. */
    public static final String COOKIE_VALUE = "test_value_123";

    /** Путь для тестов cookies. */
    public static final String COOKIE_TEST_PATH = "/cookieTest";
    /** Эндпоинт для установки cookie. */
    public static final String COOKIE_TEST_SET_ENDPOINT = "/set";
    /** Эндпоинт для эха cookie. */
    public static final String COOKIE_TEST_ECHO_ENDPOINT = "/echo";
    /** Эндпоинт для проверки cookie. */
    public static final String COOKIE_TEST_CHECK_ENDPOINT = "/check";

    /**
     * Устанавливает тестовый cookie в ответ.
     * @return ответ с установленным cookie
     */
    @GET
    @Path("/set")
    @Produces(TEXT_PLAIN)
    public Response setCookie() {
        NewCookie newCookie = new NewCookie(
                COOKIE_NAME, COOKIE_VALUE, "/", null,
                null, -1, false, false
        );
        return Response.ok("Cookie set")
                .cookie(newCookie)
                .build();
    }

    /**
     * Возвращает полученные cookies в виде JSON.
     * @param httpHeaders заголовки запроса
     * @return JSON с полученными cookies
     */
    @GET
    @Path("/echo")
    @Produces(APPLICATION_JSON)
    public Response echoCookie(@Context HttpHeaders httpHeaders) {
        // Получаем cookies через JAX-RS HttpHeaders
        List<String> cookieHeaders = httpHeaders.getRequestHeader("Cookie");

        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            return Response.ok(Map.of("cookies", Collections.emptyList())).build();
        }

        String cookieHeader = cookieHeaders.get(0);
        List<Map<String, String>> result = Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(s -> s.contains("="))
                .map(s -> {
                    String[] parts = s.split("=", 2);
                    return Map.of(parts[0].trim(), parts[1].trim());
                })
                .toList();

        return Response.ok(Map.of("cookies", result)).build();
    }

    /**
     * Проверяет наличие тестового cookie в запросе.
     * @param httpHeaders заголовки запроса
     * @return JSON с результатом проверки
     */
    @GET
    @Path("/check")
    @Produces(APPLICATION_JSON)
    public Response checkCookie(@Context HttpHeaders httpHeaders) {
        List<String> cookieHeaders = httpHeaders.getRequestHeader("Cookie");
        boolean hasCookie = false;

        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            String cookieHeader = cookieHeaders.get(0);
            hasCookie = Arrays.stream(cookieHeader.split(";"))
                    .map(String::trim)
                    .anyMatch(s -> s.equals(COOKIE_NAME + "=" + COOKIE_VALUE));
        }

        return Response.ok(Map.of("hasCookie", hasCookie)).build();
    }
}
