package ru.vych;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основное приложение для запуска тестового сервера.
 */
@SpringBootApplication
public class App {

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
