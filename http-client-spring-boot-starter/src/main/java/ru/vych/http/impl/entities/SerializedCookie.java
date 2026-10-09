package ru.vych.http.impl.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * DTO для сериализации/десериализации HTTP-cookie в JSON-формат файла persistent storage.
 * <p>
 * Хранит все атрибуты cookie плюс {@code createdAt} для проверки TTL при загрузке из файла.
 * </p>
 *
 * @see java.net.HttpCookie
 */
public final class SerializedCookie implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;
    private final String domain;
    private final String path;
    private final boolean secure;
    private final boolean httpOnly;
    private final Integer maxAge;
    private final long createdAt;
    private final Long expires;

    /**
     * Создаёт сериализованный cookie.
     *
     * @param name      имя cookie
     * @param value     значение cookie
     * @param domain    домен cookie
     * @param path      путь cookie
     * @param secure    флаг secure
     * @param httpOnly  флаг httpOnly
     * @param maxAge    max-age в секундах (null для session cookies)
     * @param createdAt timestamp создания в миллисекундах
     * @param expires   timestamp истечения в миллисекундах (null если не указан)
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    @JsonCreator
    public SerializedCookie(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value,
            @JsonProperty("domain") String domain,
            @JsonProperty("path") String path,
            @JsonProperty("secure") boolean secure,
            @JsonProperty("httpOnly") boolean httpOnly,
            @JsonProperty("maxAge") Integer maxAge,
            @JsonProperty("createdAt") long createdAt,
            @JsonProperty("expires") Long expires
    ) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.maxAge = maxAge;
        this.createdAt = createdAt;
        this.expires = expires;
    }

    /**
     * Создаёт {@link SerializedCookie} из {@link java.net.HttpCookie}.
     * <p>
     * Использует текущий timestamp как {@code createdAt}.
     * {@code maxAge} устанавливается только если {@code HttpCookie.getMaxAge()} != null.
     * {@code expires} вычисляется как {@code createdAt + maxAge * 1000}.
     * </p>
     *
     * @param cookie исходний cookie
     * @return сериализованный cookie
     */
    public static SerializedCookie fromHttpCookie(java.net.HttpCookie cookie) {
        long createdAt = System.currentTimeMillis();
        Long maxAgeObj = cookie.getMaxAge();
        Integer maxAge = maxAgeObj != null ? maxAgeObj.intValue() : null;
        Long expires = null;

        // Вычисляем expires из maxAge
        if (maxAge != null && maxAge > 0) {
            expires = createdAt + maxAge * 1000L;
        }

        return new SerializedCookie(
                cookie.getName(),
                cookie.getValue(),
                cookie.getDomain(),
                cookie.getPath(),
                cookie.getSecure(),
                cookie.isHttpOnly(),
                maxAge,
                createdAt,
                expires
        );
    }

    /**
     * Восстанавливает {@link java.net.HttpCookie} из сериализованного представления.
     * <p>
     * Устанавливает {@code maxAge} и {@code expires} для проверки TTL.
     * </p>
     *
     * @return восстановленный cookie
     */
    public java.net.HttpCookie toHttpCookie() {
        java.net.HttpCookie cookie = new java.net.HttpCookie(name, value);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        if (path != null) {
            cookie.setPath(path);
        }
        cookie.setSecure(secure);
        if (httpOnly) {
            cookie.setHttpOnly(true);
        }
        if (maxAge != null) {
            cookie.setMaxAge(maxAge.longValue());
        }
        if (expires != null) {
            // Устанавливаем maxAge из expires и createdAt
            long ageMillis = expires - createdAt;
            if (ageMillis > 0) {
                cookie.setMaxAge(ageMillis / 1000);
            }
        }
        return cookie;
    }

    /**
     * Проверяет, что cookie имеет TTL (maxAge > 0 или expires указан).
     *
     * @return true если cookie является persistent (не session)
     */
    public boolean hasTtl() {
        return maxAge != null && maxAge > 0 || expires != null && expires > 0;
    }

    /**
     * Проверяет, что cookie не истёк по TTL.
     * <p>
     * TTL проверяется по приоритету:
     * <ol>
     *   <li>Если {@code expires} указан — проверяется {@code expires > currentMillis}</li>
     *   <li>Иначе если {@code maxAge} указан — проверяется {@code createdAt + maxAge * 1000 > currentMillis}</li>
     * </ol>
     * </p>
     *
     * @return true если cookie ещё валиден по времени
     */
    @JsonIgnore
    public boolean isNotExpired() {
        // Проверяем expires (приоритет выше)
        if (expires != null && expires > 0) {
            return expires > System.currentTimeMillis();
        }

        // Проверяем maxAge
        if (!hasTtl()) {
            return false;
        }
        return createdAt + (long) maxAge * 1000L > System.currentTimeMillis();
    }

    /**
     * Возвращает имя cookie.
     *
     * @return имя
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает значение cookie.
     *
     * @return значение
     */
    public String getValue() {
        return value;
    }

    /**
     * Возвращает домен cookie.
     *
     * @return домен
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Возвращает путь cookie.
     *
     * @return путь
     */
    public String getPath() {
        return path;
    }

    /**
     * Возвращает флаг secure.
     *
     * @return true если secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Возвращает флаг httpOnly.
     *
     * @return true если httpOnly
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Возвращает max-age в секундах.
     *
     * @return max-age или null
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Возвращает timestamp создания в миллисекундах.
     *
     * @return timestamp создания
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Возвращает timestamp истечения в миллисекундах.
     *
     * @return timestamp истечения или null
     */
    public Long getExpires() {
        return expires;
    }
}
