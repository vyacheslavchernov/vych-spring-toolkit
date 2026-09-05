package ru.vych.http.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.vych.http.impl.common.CookiesPolicies;
import ru.vych.http.impl.entities.CookieEntry;

import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Конфигурация экземпляра HTTP-клиента.
 * <p>
 * Содержит все параметры, необходимые для создания и настройки клиента:
 * корневой URL, тайм-ауты, дефолтные заголовки, параметры cookie, политику
 * редиректов и версию протокола. Передаётся в {@link HttpClientBuilder} при
 * создании клиента.
 * </p>
 *
 * @see HttpClientBuilder#build(HttpClientConfig, ru.vych.logger.impl.LogService, java.util.List, java.util.List)
 * @see ru.vych.http.impl.HttpClient
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class HttpClientConfig {

    /**
     * Уникальный код клиента.
     * Применяется в логировании для идентификации источника событий.
     */
    private final String serviceCode;

    /**
     * Корневой URL (base URL) для данного клиента.
     * <p>
     * К этому URL добавляются пути из {@link ru.vych.http.impl.entities.Request},
     * path-параметры и query-параметры при формировании полного URI запроса.
     * </p>
     * <p>По умолчанию — пустая строка (все запросы идут на корень).</p>
     */
    private String root = "";

    /**
     * Тайм-аут установления соединения и ожидания ответа.
     * <p>
     * Применяется как к соединению, так и к ожиданию ответа от сервера.
     * </p>
     * <p>По умолчанию — 15 секунд.</p>
     */
    private Duration timeout = Duration.ofSeconds(15);

    /**
     * Дефолтные HTTP-заголовки, которые автоматически добавляются к каждому запросу.
     * <p>
     * Заголовки из этого мапа добавляются первыми; заголовки, установленные
     * в самом {@link ru.vych.http.impl.entities.Request}, добавляются поверх
     * и могут переопределять значения из этого мапа.
     * </p>
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * Дефолтные cookie, которые добавляются в cookie-хранилище при инициализации клиента.
     */
    private List<CookieEntry> cookies = new ArrayList<>();

    /**
     * Политика принятия cookie.
     * <p>
     * {@code ACCEPT_ALL} — принимать все cookie.
     * {@code ACCEPT_NONE} — отклонять все cookie.
     * {@code ACCEPT_ORIGINAL_SERVER} — принимать только cookie оригинального сервера.
     * </p>
     * <p>По умолчанию — {@link CookiesPolicies#ACCEPT_ALL}.</p>
     *
     * @see CookiePolicy
     * @see CookiesPolicies
     */
    private CookiesPolicies cookiePolicy = CookiesPolicies.ACCEPT_ALL;

    /**
     * Политика автоматического следования за редиректами (3xx статусы).
     * <p>
     * {@code NORMAL} — следует за редиректами с методами GET и HEAD, но не с POST.
     * {@code ALWAYS} — следует за редиректами любого метода.
     * {@code NEVER} — не следует за редиректами, возвращает исходный ответ.
     * </p>
     * <p>По умолчанию — {@link HttpClient.Redirect#NORMAL}.</p>
     *
     * @see HttpClient.Redirect
     */
    private HttpClient.Redirect redirectPolicy = HttpClient.Redirect.NORMAL;

    /**
     * Версия HTTP-протокола, которая будет использоваться при отправке запросов.
     * <p>По умолчанию — HTTP/1.1.</p>
     *
     * @see java.net.http.HttpClient.Version
     */
    private HttpClient.Version version = HttpClient.Version.HTTP_1_1;

    /**
     * Включает логирование запросов и ответов, отправляемых клиентом.
     * <p>
     * Если {@code true}, запросы и ответы логируются через {@code LogService}
     * из {@code logger-spring-boot-starter}. По умолчанию логирование выключено.
     * </p>
     */
    private boolean logRequests = true;
}
