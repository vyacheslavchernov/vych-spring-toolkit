package ru.vych.http.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.controllers.ErrorTestController.ERROR_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования обработки ошибок сервера.
 */
@Path(ERROR_CONTROLLER_PATH)
public class ErrorTestController {

    /** Путь контроллера. */
    public static final String ERROR_CONTROLLER_PATH = "/errorTest";

    /** Эндпоинт для 400 Bad Request. */
    public static final String ERROR_400_ENDPOINT = "/400";
    /** Эндпоинт для 404 Not Found. */
    public static final String ERROR_404_ENDPOINT = "/404";
    /** Эндпоинт для 500 Internal Server Error. */
    public static final String ERROR_500_ENDPOINT = "/500";
    /** Эндпоинт для 403 Forbidden. */
    public static final String ERROR_403_ENDPOINT = "/403";
    /** Эндпоинт для 401 Unauthorized. */
    public static final String ERROR_401_ENDPOINT = "/401";

    /** Текст ответа 400. */
    public static final String BAD_REQUEST_TEXT = "Bad Request";
    /** Текст ответа 404. */
    public static final String NOT_FOUND_TEXT = "Not Found";
    /** Текст ответа 500. */
    public static final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error";
    /** Текст ответа 401. */
    public static final String UNAUTHORIZED_TEXT = "Unauthorized";

    /** Ключ для поля error в JSON 403. */
    public static final String FORBIDDEN_ERROR_KEY = "error";
    /** Значение поля error в JSON 403. */
    public static final String FORBIDDEN_ERROR_VALUE = "Forbidden";
    /** Ключ для поля code в JSON 403. */
    public static final String FORBIDDEN_CODE_KEY = "code";
    /** Значение поля code в JSON 403. */
    public static final Integer FORBIDDEN_CODE_VALUE = 403;

    /**
     * Возвращает ошибку 400 Bad Request.
     * @return ответ с ошибкой 400
     */
    @GET
    @Path(ERROR_400_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(BAD_REQUEST_TEXT)
                .build();
    }

    /**
     * Возвращает ошибку 404 Not Found.
     * @return ответ с ошибкой 404
     */
    @GET
    @Path(ERROR_404_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(NOT_FOUND_TEXT)
                .build();
    }

    /**
     * Возвращает ошибку 500 Internal Server Error.
     * @return ответ с ошибкой 500
     */
    @GET
    @Path(ERROR_500_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response internalError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(INTERNAL_SERVER_ERROR_TEXT)
                .build();
    }

    /**
     * Возвращает ошибку 403 Forbidden с JSON телом.
     * @return ответ с ошибкой 403
     */
    @GET
    @Path(ERROR_403_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of(FORBIDDEN_ERROR_KEY, FORBIDDEN_ERROR_VALUE, FORBIDDEN_CODE_KEY, FORBIDDEN_CODE_VALUE))
                .build();
    }

    /**
     * Возвращает ошибку 401 Unauthorized.
     * @return ответ с ошибкой 401
     */
    @GET
    @Path(ERROR_401_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(UNAUTHORIZED_TEXT)
                .build();
    }
}
