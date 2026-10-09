package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.controllers.GetTestController.GET_CONTROLLER_PATH;

/**
 * JAX-RS контроллер для тестирования GET запросов mock-сервера.
 */
@Path(GET_CONTROLLER_PATH)
public class GetTestController {

    /** Путь контроллера. */
    public static final String GET_CONTROLLER_PATH = "/getTest";

    /** Ключ параметра UUID. */
    public static final String UUID_PARAM_KEY = "uuid";

    /** Эндпоинт для простого приветствия. */
    public static final String GET_HELLO_ENDPOINT = "/getHelloWorld";
    /** Эндпоинт для запроса с query параметром. */
    public static final String GET_QUERY_ENDPOINT = "/getQuery";
    /** Эндпоинт для запроса с несколькими query параметрами. */
    public static final String GET_MANY_QUERY_ENDPOINT = "/getManyQuery";
    /** Эндпоинт для запроса с path параметром. */
    public static final String GET_PATH_ENDPOINT = "/getQuery";
    /** Эндпоинт для запроса с path и query параметрами. */
    public static final String GET_PATH_AND_QUERY_ENDPOINT = "/getPathNQuery";
    /** Эндпоинт для получения заголовков. */
    public static final String GET_HEADERS_ENDPOINT = "/getHeaders";

    /** Текст приветствия. */
    public static final String HELLO_TEXT = "Hello, World!";

    /**
     * Возвращает простое приветствие.
     * @return ответ с текстом приветствия
     */
    @GET
    @Path(GET_HELLO_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response getHello() {
        return Response.ok().entity(HELLO_TEXT).build();
    }

    /**
     * Возвращает переданный query параметр uuid.
     * @param uuid значение параметра uuid
     * @return ответ с переданным значением
     */
    @GET
    @Path(GET_QUERY_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response getQuery(@QueryParam(UUID_PARAM_KEY) String uuid) {
        return Response.ok().entity(uuid).build();
    }

    /**
     * Возвращает все query параметры в виде JSON.
     * @param uriInfo информация о URI
     * @return JSON с query параметрами
     */
    @GET
    @Path(GET_MANY_QUERY_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response getManyQuery(@Context UriInfo uriInfo) {
        Map<String, String> params =
                uriInfo.getQueryParameters()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().getFirst()
                        ));
        return Response.ok().entity(params).build();
    }

    /**
     * Возвращает path параметр.
     * @param uuid значение path параметра
     * @return ответ с переданным значением
     */
    @GET
    @Produces(TEXT_PLAIN)
    @Path(GET_PATH_ENDPOINT + "/{" + UUID_PARAM_KEY + "}")
    public Response getPath(@PathParam(UUID_PARAM_KEY) String uuid) {
        return Response.ok().entity(uuid).build();
    }

    /**
     * Возвращает path и query параметры в виде JSON.
     * @param key значение path параметра
     * @param value значение query параметра
     * @return JSON с параметрами
     */
    @GET
    @Path(GET_PATH_AND_QUERY_ENDPOINT + "/{" + UUID_PARAM_KEY + "}")
    @Produces(APPLICATION_JSON)
    public Response getPathAndQuery(@PathParam(UUID_PARAM_KEY) String key, @QueryParam(UUID_PARAM_KEY) String value) {
        return Response.ok().entity(Map.of(key, value)).build();
    }

    /**
     * Возвращает все заголовки запроса в виде JSON.
     * @param httpHeaders заголовки запроса
     * @return JSON с заголовками
     */
    @GET
    @Path(GET_HEADERS_ENDPOINT)
    @Produces(APPLICATION_JSON)
    public Response getHeaders(@Context HttpHeaders httpHeaders) {
        Map<String, List<String>> headers = httpHeaders.getRequestHeaders();
        return Response.ok().entity(headers).build();
    }
}
