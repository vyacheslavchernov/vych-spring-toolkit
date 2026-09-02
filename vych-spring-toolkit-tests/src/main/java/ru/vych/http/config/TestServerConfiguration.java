package ru.vych.http.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.controllers.GetTestController;
import ru.vych.http.controllers.PostTestController;

import java.net.URI;

@Configuration
public class TestServerConfiguration {
    public static String TEST_SERVER_URI = "http://localhost:9090";


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public HttpServer testServer() {

        ResourceConfig config =
                new ResourceConfig()
                        .register(GetTestController.class)
                        .register(PostTestController.class)
                        .register(JacksonFeature.class)
                        .register(ExceptionHandler.class);


        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(TEST_SERVER_URI),
                config,
                false
        );
    }
}
