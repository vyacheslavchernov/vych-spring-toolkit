package ru.vych.http.impl.entities;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.impl.common.HttpMethod.GET;

/**
 * Юнит-тесты для {@link Response}.
 */
@DisplayName("Response should")
class ResponseTests {

    @Test
    @SneakyThrows
    @DisplayName("create response without cache info")
    void createResponseWithoutCacheInfo() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/users")
                .build();

        Response response = Response.of(
                "uuid-123",
                request,
                200,
                "{\"id\":1}",
                List.of(new Header("Content-Type", "application/json"))
        );

        assertThat(response.getUuid())
                .as("UUID должен совпадать")
                .isEqualTo("uuid-123");
        assertThat(response.getStatus())
                .as("Статус должен быть 200")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("Тело ответа должно совпадать")
                .isEqualTo("{\"id\":1}");
        assertThat(response.isCached())
                .as("isCached должен быть false")
                .isFalse();
        assertThat(response.getCachedAt())
                .as("cachedAt должен быть null")
                .isNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("create cached response")
    void createCachedResponse() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/users")
                .build();

        Response original = Response.of(
                "uuid-123",
                request,
                200,
                "{\"id\":1}",
                List.of(new Header("Content-Type", "application/json"))
        );

        Instant cachedAt = Instant.now();
        Response cached = Response.cached(original, "uuid-123", request, cachedAt);

        assertThat(cached.getUuid())
                .as("UUID должен совпадать")
                .isEqualTo("uuid-123");
        assertThat(cached.getStatus())
                .as("Статус должен совпадать")
                .isEqualTo(200);
        assertThat(cached.getBody())
                .as("Тело ответа должно совпадать")
                .isEqualTo("{\"id\":1}");
        assertThat(cached.isCached())
                .as("isCached должен быть true")
                .isTrue();
        assertThat(cached.getCachedAt())
                .as("cachedAt должен быть установлен")
                .isEqualTo(cachedAt);
    }

    @Test
    @SneakyThrows
    @DisplayName("copy all fields from original response")
    void copyAllFieldsFromOriginalResponse() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/data")
                .build();

        Response original = Response.of(
                "uuid-456",
                request,
                201,
                "created",
                List.of(
                        new Header("Content-Type", "text/plain"),
                        new Header("Location", "/api/data/1")
                )
        );

        Response cached = Response.cached(original, "uuid-456", request, Instant.now());

        assertThat(cached.getStatus())
                .as("Статус должен быть 201")
                .isEqualTo(201);
        assertThat(cached.getRawBody())
                .as("rawBody должен совпадать")
                .isEqualTo("created");
        assertThat(cached.getHeaders())
                .as("Заголовки должны совпадать")
                .hasSize(2);
    }

    @Test
    @SneakyThrows
    @DisplayName("return casted body correctly")
    void returnCastedBodyCorrectly() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/users")
                .setResponseClass(String.class)
                .build();

        Response response = Response.of(
                "uuid-789",
                request,
                200,
                "test body",
                List.of()
        );

        String body = response.getCastedBody();

        assertThat(body)
                .as("Типизированное тело должно совпадать")
                .isEqualTo("test body");
    }

    @Test
    @SneakyThrows
    @DisplayName("return null for byte response class")
    void returnNullForByteResponseClass() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/data")
                .setResponseClass(byte[].class)
                .build();

        Response response = Response.of(
                "uuid",
                request,
                200,
                "data",
                List.of()
        );

        assertThat((Object) response.getCastedBody())
                .describedAs("getCastedBody должен вернуть null для byte[].class")
                .isNull();
    }

    @Test
    @SneakyThrows
    @DisplayName("support status update")
    void supportStatusUpdate() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/test")
                .build();

        Response response = Response.of(
                "uuid",
                request,
                200,
                "ok",
                List.of()
        );

        response.setStatus(201);

        assertThat(response.getStatus())
                .as("Статус должен быть обновлён")
                .isEqualTo(201);
    }

    @Test
    @SneakyThrows
    @DisplayName("generate toString representation")
    void generateToStringRepresentation() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/test")
                .build();

        Response response = Response.of(
                "uuid",
                request,
                200,
                "data",
                List.of(new Header("Content-Type", "text/plain"))
        );

        String toString = response.toString();

        assertThat(toString)
                .as("toString должен содержать uuid")
                .contains("uuid");
    }

    @Test
    @SneakyThrows
    @DisplayName("support equals and hashCode")
    void supportEqualsAndHashCode() {
        Request request = Request.builder()
                .setMethod(GET)
                .setUrl("/api/test")
                .build();

        Response response1 = Response.of(
                "uuid",
                request,
                200,
                "data",
                List.of(new Header("Content-Type", "text/plain"))
        );

        Response response2 = Response.of(
                "uuid",
                request,
                200,
                "data",
                List.of(new Header("Content-Type", "text/plain"))
        );

        assertThat((Object) response1)
                .as("Ответы с одинаковыми полями должны быть равны")
                .isEqualTo(response2);
        assertThat(response1.hashCode())
                .describedAs("hashCode должны совпадать")
                .isEqualTo(response2.hashCode());
    }
}
