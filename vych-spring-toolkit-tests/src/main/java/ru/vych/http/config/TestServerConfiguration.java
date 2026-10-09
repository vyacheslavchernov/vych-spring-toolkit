package ru.vych.http.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.controllers.*;

import java.net.URI;

/**
 * Конфигурация тестового HTTP сервера.
 */
@Configuration
public class TestServerConfiguration {

    /** URI тестового сервера. */
    public static String TEST_SERVER_URI = "http://localhost:9090";

    /**
     * Создает и настраивает тестовый HTTP сервер.
     * @return настроенный HttpServer
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public HttpServer testServer() {

        ResourceConfig config =
                new ResourceConfig()
                        .register(CookieTestController.class)
                        .register(DeleteTestController.class)
                        .register(ErrorTestController.class)
                        .register(GetTestController.class)
                        .register(HeadTestController.class)
                        .register(OptionsTestController.class)
                        .register(PatchTestController.class)
                        .register(PostTestController.class)
                        .register(PutTestController.class)
                        .register(RedirectTestController.class)
                        .register(JacksonFeature.class)
                        .register(ExceptionHandler.class);


        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(TEST_SERVER_URI),
                config,
                false
        );
    }
}
