package com.mctng.timelogger;

import java.time.Duration;

public class Util {

    public static String ticksToTime(int ticks){
        int seconds = ticks / 20;
        Duration duration = Duration.ofSeconds(seconds);
        return formatDuration(duration);
    }

    private static String formatDuration(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
