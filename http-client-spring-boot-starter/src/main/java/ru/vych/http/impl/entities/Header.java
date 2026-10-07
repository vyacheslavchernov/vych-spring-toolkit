package ru.vych.http.impl.entities;

/**
 * HTTP-заголовок — пара "имя → значение".
 * <p>
 * Используется для представления заголовков как в {@link Request}, так и в {@link Response}.
 * </p>
 *
 * @see Request#getHeaders()
 * @see Response#getHeaders()
 */
public record Header(String name, String value) {
}
