package ru.vych.http.impl.common;

/**
 * Политика приёма cookie HTTP-клиентом.
 * <p>
 * Определяет, какие cookie клиент готов принимать от сервера:
 * все, ни одного или только от исходного сервера.
 * </p>
 *
 * @see java.net.CookiePolicy
 */
public enum CookiesPolicies {
    ACCEPT_ALL,
    ACCEPT_NONE,
    ACCEPT_ORIGINAL_SERVER;
}
