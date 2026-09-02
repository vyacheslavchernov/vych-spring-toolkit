package ru.vych.common;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static int inRange(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end);
    }

    public static Map<String, String> randomMap(int maxSize) {
        var map = new HashMap<String, String>();
        for (var i = 0; i < RandomUtils.inRange(1, maxSize); i++) {
            map.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        return map;
    }

    public static List<String> randomList(int maxSize) {
        var list = new LinkedList<String>();
        for (var i = 0; i < RandomUtils.inRange(1, maxSize); i++) {
            list.add(UUID.randomUUID().toString());
        }
        return list;
    }
}
