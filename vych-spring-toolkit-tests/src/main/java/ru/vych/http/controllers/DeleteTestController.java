package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.DeleteTestController.DELETE_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования DELETE запросов mock-сервера.
 */
@Path(DELETE_CONTROLLER_PATH)
public class DeleteTestController {

    /** Путь контроллера. */
    public static final String DELETE_CONTROLLER_PATH = "/deleteTest";

    /** Эндпоинт DELETE без тела. */
    public static final String DELETE_WITHOUT_BODY_ENDPOINT = "/withoutBody/{id}";
    /** Эндпоинт DELETE с телом. */
    public static final String DELETE_WITH_BODY_ENDPOINT = "/withBody";

    /**
     * DELETE запрос без тела — возвращает результат удаления.
     * @param id идентификатор удаляемого ресурса
     * @return результат удаления
     */
    @DELETE
    @Path(DELETE_WITHOUT_BODY_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response deleteWithoutBody(@PathParam("id") String id) {
        Map<String, Object> result = Map.of("deletedId", id, "success", true);
        return Response.ok().entity(result).build();
    }

    /**
     * DELETE запрос с телом — эхо тела в ответе.
     * @param body тело запроса
     * @return ответ с эхом тела
     */
    @DELETE
    @Path(DELETE_WITH_BODY_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response deleteWithBody(String body) {
        Map<String, Object> result = Map.of("deleted", true, "body", body);
        return Response.ok().entity(result).build();
    }
}
