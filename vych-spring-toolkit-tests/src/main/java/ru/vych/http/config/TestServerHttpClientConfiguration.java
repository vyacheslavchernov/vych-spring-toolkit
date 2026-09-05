package ru.vych.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.http.impl.interceptors.ResponseInterceptor;
import ru.vych.logger.impl.LogService;

import java.time.Duration;
import java.util.List;

import static ru.vych.http.config.TestServerConfiguration.TEST_SERVER_URI;

@Configuration
public class TestServerHttpClientConfiguration {
    public static final String SERVICE_CODE = "TestServerHttpClient";

    @Bean(name = SERVICE_CODE)
    public HttpClient client(
            HttpClientBuilder builder, LogService logService,
            List<RequestInterceptor> requestInterceptors, List<ResponseInterceptor> responseInterceptors
    ) throws HttpClientException {
        HttpClientConfig config = new HttpClientConfig(SERVICE_CODE)
                .setRoot(TEST_SERVER_URI)
                .setTimeout(Duration.ofSeconds(2));
        return builder.build(config, logService, requestInterceptors, responseInterceptors);
    }
}
