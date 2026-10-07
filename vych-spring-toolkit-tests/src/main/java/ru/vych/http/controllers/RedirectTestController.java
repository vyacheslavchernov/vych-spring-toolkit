package ru.vych.http.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.net.URI;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;
import static ru.vych.http.controllers.GetTestController.GET_HELLO_ENDPOINT;
import static ru.vych.http.controllers.RedirectTestController.REDIRECT_CONTROLLER_PATH;

@Path(REDIRECT_CONTROLLER_PATH)
public class RedirectTestController {
    public static final String REDIRECT_CONTROLLER_PATH = "/redirectTest";

    public static final String TO_HELLO_ENDPOINT = "/to-hello";
    public static final String TO_HELLO_301_ENDPOINT = "/to-hello-301";
    public static final String LOOP_ENDPOINT = "/loop";
    public static final String EXTERNAL_ENDPOINT = "/external";
    public static final String POST_REDIRECT_ENDPOINT = "/post-redirect";
    public static final String REDIRECT_TEST_PAYLOAD = "test-payload";

    public static final String HELLO_TARGET_PATH = "/getTest" + GET_HELLO_ENDPOINT;
    public static final String EXTERNAL_REDIRECT_URL = "https://example.com";

    public static final String HELLO_TARGET_LOCATION = TEST_SERVER_URI + HELLO_TARGET_PATH;
    public static final String LOOP_TARGET_LOCATION = REDIRECT_CONTROLLER_PATH + LOOP_ENDPOINT;

    @GET
    @Path(TO_HELLO_ENDPOINT)
    public Response redirectToHello() {
        return Response.status(302)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }

    @GET
    @Path(TO_HELLO_301_ENDPOINT)
    public Response permanentRedirectToHello() {
        return Response.status(301)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }

    @GET
    @Path(LOOP_ENDPOINT)
    public Response redirectLoop() {
        return Response.status(302)
                .location(URI.create(LOOP_TARGET_LOCATION))
                .build();
    }

    @GET
    @Path(EXTERNAL_ENDPOINT)
    public Response externalRedirect() {
        return Response.status(302)
                .location(URI.create(EXTERNAL_REDIRECT_URL))
                .build();
    }

    @POST
    @Path(POST_REDIRECT_ENDPOINT)
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response postRedirect(String body) {
        return Response.status(302)
                .location(URI.create(HELLO_TARGET_PATH))
                .build();
    }
}
