package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.controllers.GetTestController.GET_CONTROLLER_PATH;

@Path(GET_CONTROLLER_PATH)
public class GetTestController {
    public static final String GET_CONTROLLER_PATH = "/getTest";

    public static final String UUID_PARAM_KEY = "uuid";

    public static final String GET_HELLO_ENDPOINT = "/getHelloWorld";
    public static final String GET_QUERY_ENDPOINT = "/getQuery";
    public static final String GET_MANY_QUERY_ENDPOINT = "/getManyQuery";
    public static final String GET_PATH_ENDPOINT = "/getQuery";
    public static final String GET_PATH_AND_QUERY_ENDPOINT = "/getPathNQuery";

    public static final String HELLO_TEXT = "Hello, World!";

    @GET
    @Path(GET_HELLO_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response getHello() {
        return Response.ok().entity(HELLO_TEXT).build();
    }

    @GET
    @Path(GET_QUERY_ENDPOINT)
    @Produces(TEXT_PLAIN)
    public Response getQuery(@QueryParam(UUID_PARAM_KEY) String uuid) {
        return Response.ok().entity(uuid).build();
    }

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
                                e -> e.getValue().get(0)
                        ));
        return Response.ok().entity(params).build();
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path(GET_PATH_ENDPOINT + "/{" + UUID_PARAM_KEY + "}")
    public Response getPath(@PathParam(UUID_PARAM_KEY) String uuid) {
        return Response.ok().entity(uuid).build();
    }

    @GET
    @Path(GET_PATH_AND_QUERY_ENDPOINT + "/{" + UUID_PARAM_KEY + "}")
    @Produces(APPLICATION_JSON)
    public Response getPathAndQuery(@PathParam(UUID_PARAM_KEY) String key, @QueryParam(UUID_PARAM_KEY) String value) {
        return Response.ok().entity(Map.of(key, value)).build();
    }
}
