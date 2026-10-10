package ru.vych.http.controllers;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.PostTestController.POST_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования POST запросов mock-сервера.
 */
@Path(POST_CONTROLLER_PATH)
public class PostTestController {

    /** Путь контроллера. */
    public static final String POST_CONTROLLER_PATH = "/postTest";

    /** Эндпоинт для POST с пустым телом. */
    public static final String EMPTY_POST_ENDPOINT = "/emptyPost";
    /** Эндпоинт для POST с текстовым телом. */
    public static final String STRING_POST_ENDPOINT = "/stringPost";
    /** Эндпоинт для POST с телом в виде байтов. */
    public static final String BYTES_POST_ENDPOINT = "/bytesPost";
    /** Эндпоинт для POST с JSON телом. */
    public static final String JSON_POST_ENDPOINT = "/jsonPost";
    /** Эндпоинт для POST с JSON телом — эхо с ID. */
    public static final String ECHO_JSON_POST_ENDPOINT = "/echoJson";

    /**
     * POST запрос с пустым телом.
     * @return пустой ответ
     */
    @POST
    @Path(EMPTY_POST_ENDPOINT)
    public Response emptyPost() {
        return Response.ok().build();
    }

    /**
     * POST запрос с текстовым телом — эхо строки.
     * @param body тело запроса
     * @return ответ с эхом тела
     */
    @POST
    @Path(STRING_POST_ENDPOINT)
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response stringPost(String body) {
        return Response.ok(body).build();
    }

    /**
     * POST запрос с телом в виде массива байт.
     * @param body тело запроса
     * @return ответ с эхом тела
     */
    @POST
    @Path(BYTES_POST_ENDPOINT)
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response bytesPost(byte[] body) {
        return Response.ok(body).build();
    }

    /**
     * POST запрос с JSON телом — эхо JSON.
     * @param body тело запроса
     * @return ответ с эхом тела
     */
    @POST
    @Path(JSON_POST_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response bytesPost(String body) {
        return Response.ok(body).build();
    }

    /**
     * POST запрос с JSON телом — эхо с добавлением поля id.
     * @param body тело запроса (JSON)
     * @return ответ с эхом тела и добавленным полем id
     */
    @POST
    @Path(ECHO_JSON_POST_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response echoJson(String body) {
        return Response.ok(body).build();
    }
}
