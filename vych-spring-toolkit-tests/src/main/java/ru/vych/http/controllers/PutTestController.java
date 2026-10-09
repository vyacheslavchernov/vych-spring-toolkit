package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.PutTestController.PUT_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования PUT запросов mock-сервера.
 */
@Path(PUT_CONTROLLER_PATH)
public class PutTestController {

    /** Путь контроллера. */
    public static final String PUT_CONTROLLER_PATH = "/putTest";

    /** Эндпоинт для PUT с текстовым телом. */
    public static final String PUT_STRING_ENDPOINT = "/string";
    /** Эндпоинт для PUT с JSON телом. */
    public static final String PUT_JSON_ENDPOINT = "/json";
    /** Эндпоинт для PUT с path параметром. */
    public static final String PUT_PATH_ENDPOINT = "/{id}";

    /**
     * PUT запрос с телом в виде строки — эхо строки.
     * @param body тело запроса
     * @return ответ с эхом строки
     */
    @PUT
    @Path(PUT_STRING_ENDPOINT)
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response putString(String body) {
        return Response.ok().entity(body).build();
    }

    /**
     * PUT запрос с JSON телом — эхо JSON.
     * @param body тело запроса
     * @return ответ с эхом JSON
     */
    @PUT
    @Path(PUT_JSON_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response putJson(String body) {
        return Response.ok().entity(body).build();
    }


}
