package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.HeadTestController.HEAD_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования HEAD запросов mock-сервера.
 */
@Path(HEAD_CONTROLLER_PATH)
public class HeadTestController {
    public static final String HEAD_CONTROLLER_PATH = "/headTest";

    public static final String HEAD_SIMPLE_ENDPOINT = "/simple";
    public static final String HEAD_WITH_HEADERS_ENDPOINT = "/withHeaders";

    /**
     * HEAD запрос без тела — возвращает только заголовки.
     */
    @HEAD
    @Path(HEAD_SIMPLE_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response headSimple() {
        return Response.ok().build();
    }

    /**
     * HEAD запрос с заголовками — возвращает только заголовки без тела.
     */
    @HEAD
    @Path(HEAD_WITH_HEADERS_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response headWithHeaders() {
        return Response.ok().build();
    }
}
