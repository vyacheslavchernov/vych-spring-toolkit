package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.net.URI;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;
import static ru.vych.http.controllers.GetTestController.GET_HELLO_ENDPOINT;
import static ru.vych.http.controllers.RedirectTestController.REDIRECT_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования редиректов.
 */
@Path(REDIRECT_CONTROLLER_PATH)
public class RedirectTestController {

    /** Путь контроллера. */
    public static final String REDIRECT_CONTROLLER_PATH = "/redirectTest";

    /** Эндпоинт редиректа на приветствие. */
    public static final String TO_HELLO_ENDPOINT = "/to-hello";
    /** Эндпоинт постоянного редиректа на приветствие. */
    public static final String TO_HELLO_301_ENDPOINT = "/to-hello-301";
    /** Эндпоинт для зацикливания редиректов. */
    public static final String LOOP_ENDPOINT = "/loop";
    /** Эндпоинт внешнего редиректа. */
    public static final String EXTERNAL_ENDPOINT = "/external";
    /** Эндпоинт POST редиректа. */
    public static final String POST_REDIRECT_ENDPOINT = "/post-redirect";
    /** Тестовый payload для редиректов. */
    public static final String REDIRECT_TEST_PAYLOAD = "test-payload";

    /** Путь для редиректа на приветствие. */
    public static final String HELLO_TARGET_PATH = "/getTest" + GET_HELLO_ENDPOINT;
    /** URL внешнего редиректа. */
    public static final String EXTERNAL_REDIRECT_URL = "https://example.com";

    /** Полный URL для редиректа на приветствие. */
    public static final String HELLO_TARGET_LOCATION = TEST_SERVER_URI + HELLO_TARGET_PATH;
    /** URL для зацикливания. */
    public static final String LOOP_TARGET_LOCATION = REDIRECT_CONTROLLER_PATH + LOOP_ENDPOINT;

    /**
     * Редирект на приветствие (302).
     * @return ответ с редиректом
     */
    @GET
    @Path(TO_HELLO_ENDPOINT)
    public Response redirectToHello() {
        return Response.status(302)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }

    /**
     * Постоянный редирект на приветствие (301).
     * @return ответ с постоянным редиректом
     */
    @GET
    @Path(TO_HELLO_301_ENDPOINT)
    public Response permanentRedirectToHello() {
        return Response.status(301)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }

    /**
     * Зацикливание редиректов.
     * @return ответ с редиректом
     */
    @GET
    @Path(LOOP_ENDPOINT)
    public Response redirectLoop() {
        return Response.status(302)
                .location(URI.create(LOOP_TARGET_LOCATION))
                .build();
    }

    /**
     * Внешний редирект на example.com.
     * @return ответ с редиректом
     */
    @GET
    @Path(EXTERNAL_ENDPOINT)
    public Response externalRedirect() {
        return Response.status(302)
                .location(URI.create(EXTERNAL_REDIRECT_URL))
                .build();
    }

    /**
     * POST редирект — принимает тело и возвращает редирект.
     * @param body тело запроса
     * @return ответ с редиректом
     */
    @POST
    @Path(POST_REDIRECT_ENDPOINT)
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response postRedirect(String body) {
        return Response.status(302)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }
}
