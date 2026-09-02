package ru.vych.http.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Spring Auto-configuration для автоматической настройки HTTP-клиента.
 * Регистрирует {@link HttpClientBuilder} как Spring Bean, который используется
 * для создания экземпляров {@link ru.vych.http.impl.HttpClient} на основе
 * {@link HttpClientConfig} и списка перехватчиков.
 *
 * <p>Для кастомной настройки клиента можно добавить собственные {@code RequestInterceptor}
 * и {@code ResponseInterceptor} в контекст Spring — они будут автоматически переданы
 * в билдер.</p>
 *
 * @see HttpClientBuilder
 * @see HttpClientConfig
 */
@AutoConfiguration
public class HttpClientAutoConfiguration {

    /**
     * Создаёт и регистрирует в контексте Spring bean билдера HTTP-клиента.
     *
     * @return новый экземпляр {@link HttpClientBuilder}
     */
    @Bean
    public HttpClientBuilder httpClientBuilder() {
        return new HttpClientBuilder();
    }
}
