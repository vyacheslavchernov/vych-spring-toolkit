package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.HeadTestController.HEAD_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования HEAD запросов mock-сервера.
 */
@Path(HEAD_CONTROLLER_PATH)
public class HeadTestController {

    /** Путь контроллера. */
    public static final String HEAD_CONTROLLER_PATH = "/headTest";

    /** Эндпоинт для простого HEAD запроса. */
    public static final String HEAD_SIMPLE_ENDPOINT = "/simple";
    /** Эндпоинт для HEAD запроса с заголовками. */
    public static final String HEAD_WITH_HEADERS_ENDPOINT = "/withHeaders";

    /**
     * HEAD запрос без тела — возвращает только заголовки.
     * @return ответ без тела
     */
    @HEAD
    @Path(HEAD_SIMPLE_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response headSimple() {
        return Response.ok().build();
    }

    /**
     * HEAD запрос с заголовками — возвращает только заголовки без тела.
     * @return ответ без тела
     */
    @HEAD
    @Path(HEAD_WITH_HEADERS_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response headWithHeaders() {
        return Response.ok().build();
    }
}
