package ru.vych.http.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.common.HttpMethod;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.exceptions.HttpClientException;
import ru.vych.http.impl.exceptions.HttpClientInvalidRequestException;
import ru.vych.http.impl.interceptors.RequestInterceptor;
import ru.vych.logger.impl.LogService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты для проверки обработки исключений интерсепторов.
 * <p>
 * Проверяет, что исключения из RequestInterceptor и ResponseInterceptor
 * пробрасываются через execute() без оборачивания в HttpClientException,
 * как указано в http-error-handling-spec.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для обработки исключений интерсепторов")
class HttpClientInterceptorExceptionTests {

    @Mock
    private LogService logService;

    private HttpClientConfig config;

    /**
     * Создаёт конфигурацию с базовым URL и включённым логированием.
     */
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        config = new HttpClientConfig("TestService")
                .setRoot("http://localhost:8080")
                .setLogRequests(true);
    }

    /**
     * Создаёт клиент с интерсепторами, обрабатывая checked exception.
     */
    private HttpClientImpl createClientWithInterceptors(List<RequestInterceptor> requestInterceptors) {
        try {
            return new HttpClientImpl(config, logService, requestInterceptors, List.of());
        } catch (HttpClientException e) {
            throw new RuntimeException("Failed to create HttpClient", e);
        }
    }

    /**
     * Проверяет, что исключение из RequestInterceptor пробрасывается
     * без оборачивания в HttpClientException.
     * <p>
     * Согласно spec: "Исключения интерсепторов не оборачиваются в HttpClientException".
     * </p>
     */
    @Test
    @DisplayName("Исключение из RequestInterceptor пробрасывается без оборачивания")
    void requestInterceptorExceptionNotWrapped() {
        var client = createClientWithInterceptors(
                List.of((httpClient, request) -> {
                    throw new RuntimeException("Interceptor error");
                })
        );

        try {
            var request = Request.builder()
                    .setUrl("/test")
                    .setMethod(HttpMethod.GET)
                    .build();

            // Исключение из интерсептора должно проброситься как есть (RuntimeException),
            // а не быть обернутым в HttpClientException
            assertThatThrownBy(() -> client.execute(request))
                    .describedAs("Исключение интерсептора не должно быть обернуто в HttpClientException")
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Interceptor error")
                    .isNotInstanceOf(HttpClientException.class);
        } catch (HttpClientInvalidRequestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяет, что HttpClientInvalidRequestException из RequestInterceptor
     * пробрасывается без оборачивания.
     */
    @Test
    @DisplayName("HttpClientInvalidRequestException из RequestInterceptor пробрасывается без оборачивания")
    void requestInterceptorThrowsInvalidRequestException() {
        var client = createClientWithInterceptors(
                List.of((httpClient, request) -> {
                    try {
                        throw new HttpClientInvalidRequestException("Invalid request in interceptor");
                    } catch (HttpClientInvalidRequestException e) {
                        throw new RuntimeException(e);
                    }
                })
        );

        try {
            var request = Request.builder()
                    .setUrl("/test")
                    .setMethod(HttpMethod.GET)
                    .build();

            assertThatThrownBy(() -> client.execute(request))
                    .describedAs("HttpClientInvalidRequestException из интерсептора не должен быть обернут")
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(HttpClientInvalidRequestException.class);
        } catch (HttpClientInvalidRequestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяет, что исключение из первого интерсептора останавливает
     * выполнение всех последующих интерсепторов.
     */
    @Test
    @DisplayName("Исключение в первом интерсепторе останавливает выполнение второго")
    void firstInterceptorExceptionStopsSecondInterceptor() {
        int[] executedCount = {0};

        var client = createClientWithInterceptors(
                List.of(
                        (httpClient, request) -> {
                            executedCount[0]++;
                            throw new RuntimeException("First interceptor fails");
                        },
                        (httpClient, request) -> {
                            executedCount[0]++;
                            // Этот интерсептор не должен быть выполнен
                        }
                )
        );

        try {
            var request = Request.builder()
                    .setUrl("/test")
                    .setMethod(HttpMethod.GET)
                    .build();

            assertThatThrownBy(() -> client.execute(request))
                    .isInstanceOf(RuntimeException.class);
        } catch (HttpClientInvalidRequestException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяет, что interceptor exception не имеет HttpClientException в cause chain.
     */
    @Test
    @DisplayName("Исключение интерсептора не имеет HttpClientException в cause chain")
    void interceptorExceptionNotInHttpClientExceptionCauseChain() {
        var client = createClientWithInterceptors(
                List.of((httpClient, request) -> {
                    throw new IllegalArgumentException("Custom interceptor error");
                })
        );

        try {
            var request = Request.builder()
                    .setUrl("/test")
                    .setMethod(HttpMethod.GET)
                    .build();

            assertThatThrownBy(() -> client.execute(request))
                    .describedAs("Исключение интерсептора не должно быть обернуто в HttpClientException")
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Custom interceptor error");
        } catch (HttpClientInvalidRequestException e) {
            throw new RuntimeException(e);
        }
    }
}
