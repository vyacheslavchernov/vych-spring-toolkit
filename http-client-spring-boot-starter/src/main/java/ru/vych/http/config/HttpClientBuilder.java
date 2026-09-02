package ru.vych.http.config;

import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.HttpClientImpl;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.util.List;

/**
 * Фабрика для создания экземпляров {@link ru.vych.http.impl.HttpClient}.
 * <p>
 * Получает полную конфигурацию ({@link HttpClientConfig}), сервис логирования
 * ({@link ru.vych.logger.impl.LogService}) и списки перехватчиков запросов
 * и ответов, после чего создаёт полностью настроенный HTTP-клиент.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClient
 * @see HttpClientConfig
 */
public class HttpClientBuilder {

    /**
     * Создаёт и настраивает экземпляр HTTP-клиента на основе переданных параметров.
     *
     * @param config               конфигурация клиента; не должен быть {@code null}
     * @param logService           сервис логирования для записи событий работы клиента; не должен быть {@code null}
     * @param requestInterceptors  список перехватчиков запросов, вызываемых перед отправкой каждого запроса; может быть пустым
     * @param responseInterceptors список перехватчиков ответов, вызываемых после получения каждого ответа; может быть пустым
     * @return полностью настроенный экземпляр {@link ru.vych.http.impl.HttpClient}
     * @throws ru.vych.http.impl.exceptions.HttpClientException если не удалось создать клиент
     *                                                         (некорректная конфигурация, ошибка инициализации cookie-хранилища и т. п.)
     */
    public HttpClient build(
            HttpClientConfig config, LogService logService,
            List<RequestInterceptor> requestInterceptors, List<ResponseInterceptor> responseInterceptors
    ) throws HttpClientException {
        return new HttpClientImpl(config, logService, requestInterceptors, responseInterceptors);
    }
}
