package net.rainbowcode.jpixelface;

import java.util.concurrent.ConcurrentHashMap;

public class Timer {
    private static ConcurrentHashMap<String, Long> timers = new ConcurrentHashMap<>();

    public static void start(String name) {
        timers.put(name, System.currentTimeMillis());
    }

    public static long stop(String name) {
        Long remove = timers.remove(name);
        return System.currentTimeMillis() - remove;
    }
}
