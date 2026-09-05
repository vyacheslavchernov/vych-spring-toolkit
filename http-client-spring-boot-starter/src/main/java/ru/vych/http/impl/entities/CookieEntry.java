package ru.vych.http.impl.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.HttpCookie;
import java.net.URI;

/**
 * Хранит пару URI и связанного с ним {@link HttpCookie}.
 * Используется для добавления дефолтных cookie в cookie-хранилище HTTP-клиента.
 *
 * @see java.net.CookieHandler
 */
@Getter
@Setter
@Accessors(chain = true)
public class CookieEntry {
    /**
     * URI, с которым связан cookie.
     */
    private URI uri;

    /**
     * HTTP-cookie.
     */
    private HttpCookie cookie;
}
