package ru.vych.common;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Утилиты для генерации случайных данных.
 */
public class RandomUtils {

    /**
     * Генерирует случайное число в заданном диапазоне.
     * @param start начальная граница (включительно)
     * @param end конечная граница (исключительно)
     * @return случайное число в диапазоне [start, end)
     */
    public static int inRange(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end);
    }

    /**
     * Генерирует случайную Map с указанным максимальным размером.
     * @param maxSize максимальный размер карты
     * @return случайная Map с ключами и значениями в виде UUID
     */
    public static Map<String, String> randomMap(int maxSize) {
        var map = new HashMap<String, String>();
        for (var i = 0; i < RandomUtils.inRange(1, maxSize); i++) {
            map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        return map;
    }

    /**
     * Генерирует случайный List с указанным максимальным размером.
     * @param maxSize максимальный размер списка
     * @return случайный List с UUID значениями
     */
    public static List<String> randomList(int maxSize) {
        var list = new LinkedList<String>();
        for (var i = 0; i < RandomUtils.inRange(1, maxSize); i++) {
            list.add(UUID.randomUUID().toString());
        }
        return list;
    }
}
