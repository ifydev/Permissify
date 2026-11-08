package me.innectic.permissify.spigot.utils;

public class DurationUtil {

    public static long expiresAt(String seconds) {
        long duration = Long.parseLong(seconds);
        if (duration <= 0) throw new IllegalArgumentException("Duration must be positive");
        return Math.addExact(System.currentTimeMillis(), Math.multiplyExact(duration, 1000L));
    }
}
