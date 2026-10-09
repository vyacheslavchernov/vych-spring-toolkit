package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.OptionsTestController.OPTIONS_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования OPTIONS запросов mock-сервера.
 */
@Path(OPTIONS_CONTROLLER_PATH)
public class OptionsTestController {
    public static final String OPTIONS_CONTROLLER_PATH = "/optionsTest";

    public static final String OPTIONS_SIMPLE_ENDPOINT = "/simple";
    public static final String OPTIONS_WITH_BODY_ENDPOINT = "/withBody";

    /**
     * OPTIONS запрос — возвращает поддерживаемые методы.
     */
    @OPTIONS
    @Path(OPTIONS_SIMPLE_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response optionsSimple() {
        Map<String, Object> result = Map.of(
                "allowedMethods", List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"),
                "maxAge", 3600
        );
        return Response.ok().entity(result)
                .header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS")
                .build();
    }

    /**
     * OPTIONS запрос с телом — возвращает информацию о ресурсе.
     */
    @OPTIONS
    @Path(OPTIONS_WITH_BODY_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response optionsWithBody(String body) {
        Map<String, Object> result = Map.of(
                "resourceInfo", "available",
                "receivedBody", body,
                "allowedMethods", List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
        );
        return Response.ok().entity(result).build();
    }
}
