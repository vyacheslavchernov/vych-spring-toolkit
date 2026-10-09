package ru.vych.http.impl.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link HttpMethod}, проверяющие наличие всех поддерживаемых методов.
 */
@DisplayName("Тесты для HttpMethod enum")
public class HttpMethodTests {

    /**
     * Проверяет, что все семь HTTP-методов определены в enum.
     */
    @Test
    @DisplayName("allHttpMethodsAreDefined")
    public void allHttpMethodsAreDefined() {
        assertThat(HttpMethod.values())
                .describedAs("HttpMethod должен содержать все 7 методов")
                .hasSize(7)
                .containsExactly(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE,
                        HttpMethod.PATCH,
                        HttpMethod.HEAD,
                        HttpMethod.OPTIONS
                );
    }

    /**
     * Проверяет, что GET доступен.
     */
    @Test
    @DisplayName("getIsAvailable")
    public void getIsAvailable() {
        assertThat(HttpMethod.GET)
                .describedAs("GET должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что POST доступен.
     */
    @Test
    @DisplayName("postIsAvailable")
    public void postIsAvailable() {
        assertThat(HttpMethod.POST)
                .describedAs("POST должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что PUT доступен.
     */
    @Test
    @DisplayName("putIsAvailable")
    public void putIsAvailable() {
        assertThat(HttpMethod.PUT)
                .describedAs("PUT должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что DELETE доступен.
     */
    @Test
    @DisplayName("deleteIsAvailable")
    public void deleteIsAvailable() {
        assertThat(HttpMethod.DELETE)
                .describedAs("DELETE должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что PATCH доступен.
     */
    @Test
    @DisplayName("patchIsAvailable")
    public void patchIsAvailable() {
        assertThat(HttpMethod.PATCH)
                .describedAs("PATCH должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что HEAD доступен.
     */
    @Test
    @DisplayName("headIsAvailable")
    public void headIsAvailable() {
        assertThat(HttpMethod.HEAD)
                .describedAs("HEAD должен быть определён")
                .isNotNull();
    }

    /**
     * Проверяет, что OPTIONS доступен.
     */
    @Test
    @DisplayName("optionsIsAvailable")
    public void optionsIsAvailable() {
        assertThat(HttpMethod.OPTIONS)
                .describedAs("OPTIONS должен быть определён")
                .isNotNull();
    }
}
