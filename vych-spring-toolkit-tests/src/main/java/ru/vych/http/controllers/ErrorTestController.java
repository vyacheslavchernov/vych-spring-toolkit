package ru.vych.http.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.controllers.ErrorTestController.ERROR_CONTROLLER_PATH;

@Path(ERROR_CONTROLLER_PATH)
public class ErrorTestController {
    public static final String ERROR_CONTROLLER_PATH = "/errorTest";

    public static final String ERROR_400_ENDPOINT = "/400";
    public static final String ERROR_404_ENDPOINT = "/404";
    public static final String ERROR_500_ENDPOINT = "/500";
    public static final String ERROR_403_ENDPOINT = "/403";
    public static final String ERROR_401_ENDPOINT = "/401";

    public static final String BAD_REQUEST_TEXT = "Bad Request";
    public static final String NOT_FOUND_TEXT = "Not Found";
    public static final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error";
    public static final String UNAUTHORIZED_TEXT = "Unauthorized";

    // 403 Forbidden JSON constants
    public static final String FORBIDDEN_ERROR_KEY = "error";
    public static final String FORBIDDEN_ERROR_VALUE = "Forbidden";
    public static final String FORBIDDEN_CODE_KEY = "code";
    public static final Integer FORBIDDEN_CODE_VALUE = 403;

    @GET
    @Path(ERROR_400_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(BAD_REQUEST_TEXT)
                .build();
    }

    @GET
    @Path(ERROR_404_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(NOT_FOUND_TEXT)
                .build();
    }

    @GET
    @Path(ERROR_500_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response internalError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(INTERNAL_SERVER_ERROR_TEXT)
                .build();
    }

    @GET
    @Path(ERROR_403_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of(FORBIDDEN_ERROR_KEY, FORBIDDEN_ERROR_VALUE, FORBIDDEN_CODE_KEY, FORBIDDEN_CODE_VALUE))
                .build();
    }

    @GET
    @Path(ERROR_401_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(UNAUTHORIZED_TEXT)
                .build();
    }
}
