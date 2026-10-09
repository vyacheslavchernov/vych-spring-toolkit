package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.PutTestController.PUT_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования PUT запросов mock-сервера.
 */
@Path(PUT_CONTROLLER_PATH)
public class PutTestController {
    public static final String PUT_CONTROLLER_PATH = "/putTest";

    public static final String PUT_STRING_ENDPOINT = "/string";
    public static final String PUT_JSON_ENDPOINT = "/json";
    public static final String PUT_PATH_ENDPOINT = "/{id}";

    /**
     * PUT запрос с телом в виде строки — эхо строки.
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
     */
    @PUT
    @Path(PUT_JSON_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response putJson(String body) {
        return Response.ok().entity(body).build();
    }


}
