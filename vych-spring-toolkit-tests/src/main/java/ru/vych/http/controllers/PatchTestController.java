package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.lang.annotation.*;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.PatchTestController.PATCH_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования PATCH запросов mock-сервера.
 */
@Path(PATCH_CONTROLLER_PATH)
public class PatchTestController {
    public static final String PATCH_CONTROLLER_PATH = "/patchTest";

    public static final String PATCH_JSON_ENDPOINT = "/json";
    public static final String PATCH_PATH_ENDPOINT = "/{id}";

    /**
     * Кастомная аннотация для HTTP PATCH метода.
     */
    @HttpMethod("PATCH")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface PATCH {}

    /**
     * PATCH запрос с JSON телом — эхо тела.
     */
    @PATCH
    @Path(PATCH_JSON_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response patchJson(String body) {
        return Response.ok().entity(body).build();
    }

    /**
     * PATCH запрос с path параметром и JSON телом — возвращает патченные данные.
     */
    @PATCH
    @Path(PATCH_PATH_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response patchWithPath(@PathParam("id") String id, String body) {
        Map<String, Object> result = Map.of("id", id, "patchedBody", body);
        return Response.ok().entity(result).build();
    }
}
