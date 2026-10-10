package http;

import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import ru.vych.App;
import ru.vych.http.config.HttpClientBuilder;
import ru.vych.http.config.HttpClientConfig;
import ru.vych.http.impl.HttpClient;
import ru.vych.http.impl.entities.Header;
import ru.vych.http.impl.entities.Request;
import ru.vych.http.impl.entities.Response;
import ru.vych.logger.impl.LogService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.vych.http.config.TestServerDefaultClientConfiguration.DEFAULT_CLIENT_SERVICE_CODE;

/**
 * Базовый класс для HTTP тестов.
 */
@SpringBootTest(classes = App.class)
public abstract class BaseHttpTest {

    /** HTTP клиент по умолчанию. */
    @Autowired
    @Qualifier(DEFAULT_CLIENT_SERVICE_CODE)
    protected HttpClient defaultClient;

    /** Билдер HTTP клиента. */
    @Autowired
    protected HttpClientBuilder httpClientBuilder;

    /** Log service для создания тестовых клиентов. */
    @Autowired
    protected LogService logService;

    /**
     * Проверка статус-кода ответа.
     * @param response ответ
     * @param expected ожидаемые статус-коды
     */
    @Step("Проверка статус-кода ответа")
    protected void checkResponseStatus(Response response, Integer... expected) {
        assertThat(response.getStatus()).isIn((Object[]) expected).describedAs("Invalid status code");
    }

    /**
     * Отправка запроса.
     * @param request запрос
     * @return ответ
     */
    @Step("Отправка запроса")
    @SneakyThrows
    protected Response sendRequest(Request request) {
        return defaultClient.execute(request);
    }

    /**
     * Отправка запроса через указанный клиент.
     * @param request запрос
     * @param client HTTP клиент
     * @return ответ
     */
    @Step("Отправка запроса через указанный клиент")
    @SneakyThrows
    protected Response sendRequest(Request request, HttpClient client) {
        return client.execute(request);
    }

    /**
     * Проверяем, что тело ответа содержит только ожидаемое значение.
     * @param <T> тип данных
     * @param body тело ответа
     * @param expected ожидаемое значение
     */
    @Step("Проверяем, что тело ответа содержит только ожидаемое значение")
    protected <T> void bodyEqualsTo(T body, T expected) {
        assertThat(body)
                .describedAs("Тело запроса не соответствует ожидаемому значению")
                .isEqualTo(expected);
    }

    /**
     * Проверяем, что тело ответа содержит только ожидаемые байты.
     * @param body тело ответа
     * @param expected ожидаемые байты
     */
    @Step("Проверяем, что тело ответа содержит только ожидаемые байты")
    protected void bodyContainsExactlyBytes(byte[] body, byte[] expected) {
        assertThat(body)
                .describedAs("Байты теле ответа не соответствуют ожидаемым")
                .containsExactly(expected);
    }

    /**
     * Проверяем, что тело ответа содержит только ожидаемые элементы.
     * @param <T> тип данных
     * @param body тело ответа
     * @param expected ожидаемая карта
     */
    @Step("Проверяем, что тело ответа содержит только ожидаемые элементы")
    protected <T> void bodyContainsExactlyEntriesOf(Map<T, T> body, Map<T, T> expected) {
        assertThat(body)
                .describedAs("Map теле ответа не соответствует ожидаемой")
                .containsExactlyEntriesOf(expected);
    }

    /**
     * Проверяем, что тело ответа содержит только ожидаемые элементы.
     * @param <T> тип данных
     * @param body тело ответа
     * @param expected ожидаемый список
     */
    @Step("Проверяем, что тело ответа содержит только ожидаемые элементы")
    protected <T> void bodyContainsExactlyElementsOf(List<T> body, List<T> expected) {
        assertThat(body)
                .describedAs("Map теле ответа не соответствует ожидаемой")
                .containsExactlyElementsOf(expected);
    }

    /**
     * Проверяем, что тело ответа содержит ожидаемый элемент.
     * @param <K> тип ключа
     * @param <V> тип значения
     * @param body тело ответа
     * @param key ключ
     * @param value значение
     */
    @Step("Проверяем, что тело ответа содержит ожидаемый элемент")
    protected <K, V> void bodyContainsEntry(Map<K, V> body, K key, V value) {
        assertThat(body)
                .describedAs("Map теле ответа не соответствует ожидаемой")
                .containsEntry(key, value);
    }

    /**
     * Проверяем, что тело ответа содержит только ожидаемый элемент.
     * @param <K> тип ключа
     * @param <V> тип значения
     * @param body тело ответа
     * @param key ключ
     * @param value значение
     */
    @Step("Проверяем, что тело ответа содержит только ожидаемый элемент")
    protected <K, V> void bodyContainsExactlyEntry(Map<K, V> body, K key, V value) {
        assertThat(body)
                .describedAs("Map теле ответа не соответствует ожидаемой")
                .containsExactly(Map.entry(key, value));
    }

    /**
     * Проверяем, что запрос содержит заголовок с ожидаемым значением.
     * @param request запрос
     * @param name имя заголовка
     * @param value значение заголовка
     */
    @Step("Проверяем, что запрос содержит заголовок с ожидаемым значением")
    protected void requestContainsHeader(Request request, String name, String value) {
        assertThat(request.getHeaders())
                .describedAs("Запрос не содержит ожидаемого заголовка")
                .contains(new Header(name, value));
    }

    /**
     * Проверяем, что ответ содержит заголовок с ожидаемым значением.
     * @param response ответ
     * @param name имя заголовка
     * @param value значение заголовка
     */
    @Step("Проверяем, что ответ содержит заголовок с ожидаемым значением")
    protected void responseContainsHeader(Response response, String name, String value) {
        assertThat(response.getHeaders())
                .describedAs("Ответ не содержит ожидаемого заголовка")
                .contains(new Header(name, value));
    }

    /**
     * Создаёт конфигурацию HTTP клиента с выключенным кешированием.
     *
     * @return конфигурация без кеширования
     */
    @Step("Создание конфигурации HTTP клиента с выключенным кешированием")
    protected HttpClientConfig createConfigWithoutCache() {
        return new HttpClientConfig("TestClient-" + System.currentTimeMillis())
                .setRoot(getServerUrl())
                .setCacheEnabled(false);
    }

    /**
     * Создаёт конфигурацию HTTP клиента с включённым кешированием.
     *
     * @return конфигурация с кешированием
     */
    @Step("Создание конфигурации HTTP клиента с включённым кешированием")
    protected HttpClientConfig createConfigWithCache() {
        return new HttpClientConfig("TestClient-" + System.currentTimeMillis())
                .setRoot(getServerUrl())
                .setCacheEnabled(true)
                .setDefaultCacheTtlSeconds(300)
                .setCacheMaxSize(100);
    }

    /**
     * Создаёт HTTP клиент из конфигурации.
     *
     * @param config конфигурация клиента
     * @return HTTP клиент
     */
    @Step("Создание HTTP клиента из конфигурации")
    @SneakyThrows
    protected HttpClient createClient(HttpClientConfig config) {
        return httpClientBuilder.build(config, logService, List.of(), List.of());
    }

    /**
     * Закрывает HTTP клиент.
     *
     * @param client HTTP клиент
     */
    @Step("Закрытие HTTP клиента")
    protected void closeClient(HttpClient client) {
        // HttpClient не требует явного закрытия в текущей реализации
    }

    /**
     * Возвращает URL сервера для тестов.
     *
     * @return URL сервера
     */
    protected String getServerUrl() {
        return "http://localhost:9090";
    }
}
