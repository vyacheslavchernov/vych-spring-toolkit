package ru.vych.http.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.HashMap;
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
     * Тайм-аут установления соединения и ожидания ответа в миллисекундах.
     * <p>По умолчанию — 15 000 мс (15 секунд).</p>
     *
     * @see java.time.Duration
     */
    private Integer timeout = 15000;

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
     * <p>
     * Работает только если {@link #storeCookies} равно {@code true}.
     * </p>
     */
    private Map<String, String> cookies = new HashMap<>();

    /**
     * Флаг, указывающий, следует ли сохранять cookie из полученных HTTP-ответов.
     * <p>
     * При {@code true} создаётся экземпляр {@link #cookieHandlerClass} для
     * управления cookie-хранилищем. При {@code false} cookie не сохраняются.
     * </p>
     * <p>По умолчанию — {@code false}.</p>
     *
     * @see #cookieHandlerClass
     */
    private Boolean storeCookies = false;

    /**
     * Класс обработчика cookie, который будет использоваться клиентом.
     * <p>
     * Должен иметь конструктор по умолчанию. По умолчанию — {@link java.net.CookieManager}.
     * </p>
     *
     * @see java.net.CookieHandler
     * @see #storeCookies
     */
    private Class<? extends CookieHandler> cookieHandlerClass = CookieManager.class;

    /**
     * Флаг автоматического следования за редиректами (3xx статусы).
     * <p>При {@code true} клиент будет автоматически переходить по Location-заголовку.</p>
     * <p>По умолчанию — {@code false}.</p>
     */
    private Boolean allowRedirects = false;

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
