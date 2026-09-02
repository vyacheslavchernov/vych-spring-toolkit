package ru.vych.logger.impl.common;

/**
 * Константы ANSI-кодов для цветного вывода в консоли.
 *
 * <p>Используется {@code ConsoleAppender} для визуального выделения
 * сообщений разных уровней логирования.
 *
 * @see ru.vych.logger.impl.appenders.ConsoleAppender
 */
public final class AnsiColor {
    /**
     * Сброс стилей (возврат к цвету по умолчанию).
     */
    public static final String RESET = "\u001B[0m";

    /**
     * Чёрный цвет.
     */
    public static final String BLACK = "\u001B[30m";

    /**
     * Красный цвет.
     */
    public static final String RED = "\u001B[31m";

    /**
     * Зелёный цвет.
     */
    public static final String GREEN = "\u001B[32m";

    /**
     * Жёлтый цвет.
     */
    public static final String YELLOW = "\u001B[33m";

    /**
     * Синий цвет.
     */
    public static final String BLUE = "\u001B[34m";

    /**
     * Фиолетовый цвет.
     */
    public static final String PURPLE = "\u001B[35m";

    /**
     * Голубой цвет.
     */
    public static final String CYAN = "\u001B[36m";

    /**
     * Белый цвет.
     */
    public static final String WHITE = "\u001B[37m";
}
