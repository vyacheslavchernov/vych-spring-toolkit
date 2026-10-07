package ru.vych.http.impl.checkdata;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.entities.Request;

import java.net.URI;

/**
 * Тестовые данные для параметризованного теста построения URI в {@link ru.vych.http.impl.HttpClientImpl}.
 * <p>
 * Содержит конфигурацию клиента, тестовый запрос и ожидаемый результат
 * для проверки корректности кодирования пути, path- и query-параметров.
 * </p>
 *
 * @see ru.vych.http.impl.HttpClientImpl
 */
@Getter
@Setter
@Accessors(chain = true)
public class HttpClientImplBuildUriCheckData {
    /**
     * Конфигурация HTTP-клиента для тестирования.
     */
    private HttpClientConfig config;
    /**
     * Тестовый запрос для построения URI.
     */
    private Request request;
    /**
     * Ожидаемый результат построения URI.
     */
    private URI expectedURI;
}
