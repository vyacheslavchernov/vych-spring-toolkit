package ru.vych.http.controllers;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

import static jakarta.ws.rs.core.MediaType.*;
import static ru.vych.http.controllers.PostTestController.POST_CONTROLLER_PATH;

@Path(POST_CONTROLLER_PATH)
public class PostTestController {
    public static final String POST_CONTROLLER_PATH = "/postTest";

    public static final String EMPTY_POST_ENDPOINT = "/emptyPost";
    public static final String STRING_POST_ENDPOINT = "/stringPost";
    public static final String BYTES_POST_ENDPOINT = "/bytesPost";
    public static final String JSON_POST_ENDPOINT = "/jsonPost";

    @POST
    @Path(EMPTY_POST_ENDPOINT)
    public Response emptyPost() {
        return Response.ok().build();
    }

    @POST
    @Path(STRING_POST_ENDPOINT)
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response stringPost(String body) {
        return Response.ok(body).build();
    }

    @POST
    @Path(BYTES_POST_ENDPOINT)
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response bytesPost(byte[] body) {
        return Response.ok(body).build();
    }

    @POST
    @Path(JSON_POST_ENDPOINT)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response bytesPost(String body) {
        return Response.ok(body).build();
    }
}
