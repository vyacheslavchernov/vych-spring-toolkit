package ru.vych.http.impl.common;

/**
 * Утилитный класс для работы с URL.
 */
public final class UrlUtils {

    private UrlUtils() {
        // Утилитный класс
    }

    /**
     * Извлекает путь URL без query-параметров.
     *
     * @param url полный URL
     * @return путь URL без query-параметров
     */
    public static String extractUrlPath(String url) {
        int queryIndex = url.indexOf('?');
        return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
    }

    /**
     * Извлекает родительский путь URL.
     *
     * @param url полный URL
     * @return родительский путь URL
     */
    public static String extractParentUrlPath(String url) {
        String urlPath = extractUrlPath(url);
        int lastSlash = urlPath.lastIndexOf('/');
        return lastSlash > 0 ? urlPath.substring(0, lastSlash) : urlPath;
    }
}
