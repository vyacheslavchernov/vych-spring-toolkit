package ru.vych.http.config;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.awt.SystemColor.text;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        Pattern pattern = Pattern.compile("HTTP\\s+(\\d{3})");
        Matcher matcher = pattern.matcher(exception.getMessage());
        int status = 500;
        if (matcher.find()) {
            status = Integer.parseInt(matcher.group(1));
        }
        return Response
                .status(status)
                .entity(exception.getClass().getName() + ": " + exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
