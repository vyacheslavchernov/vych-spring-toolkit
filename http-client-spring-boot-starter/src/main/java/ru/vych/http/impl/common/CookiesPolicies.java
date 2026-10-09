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
    /** Принимать все cookies. */
    ACCEPT_ALL,
    /** Не принимать ни одного cookie. */
    ACCEPT_NONE,
    /** Принимать cookies только от исходного сервера. */
    ACCEPT_ORIGINAL_SERVER;
}
