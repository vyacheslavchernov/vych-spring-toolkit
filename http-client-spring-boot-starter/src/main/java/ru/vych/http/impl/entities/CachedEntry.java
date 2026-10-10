package ru.vych.http.impl.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Запись в кеше HTTP-клиента.
 * <p>
 * Содержит результат HTTP-запроса и время его истечения.
 * Используется внутренним кешем {@link ru.vych.http.impl.HttpClientCache}
 * для хранения и управления TTL записей.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClientCache
 */
@Getter
@ToString
@EqualsAndHashCode
public class CachedEntry {

    /**
     * Результат HTTP-запроса, помещённый в кеш.
     */
    private final Response response;

    /**
     * Время помещения записи в кеш.
     */
    private final Instant cachedAt;

    /**
     * Время истечения записи в кеше.
     * <p>
     * После этого времени запись считается невалидной
     * и должна быть удалена из кеша.
     * </p>
     */
    private final Instant expiresAt;

    /**
     * Создаёт новую запись кеша.
     *
     * @param response  результат HTTP-запроса для кеширования
     * @param cachedAt  время помещения в кеш
     * @param expiresAt время истечения записи
     */
    public CachedEntry(Response response, Instant cachedAt, Instant expiresAt) {
        this.response = response;
        this.cachedAt = cachedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Проверяет, истек ли TTL записи.
     *
     * @return {@code true} если текущее время больше {@link #expiresAt}
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
