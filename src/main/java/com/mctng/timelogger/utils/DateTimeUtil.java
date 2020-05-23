package com.mctng.timelogger.utils;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class DateTimeUtil {

    /**
     * Returns a String formatted a time in milliseconds into HH:mm:ss
     * @param millis time in milliseconds
     * @return formatted time string
     */
    public static String formatMillis(long millis) {
        long seconds = millis / 1000;
        Duration duration = Duration.ofSeconds(seconds);
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }


    /**
     * Returns an Instant object which is a specified unit of time before another Instant.
     * @param timeString    a string representing a unit of time, formatted like so '30m'.
     * @param endingInstant an Instant object representing the starting Instant to subtract from.
     * @return an Instant object which is x amount of time before the starting instant.
     * @throws IllegalArgumentException if the unit is formatted incorrectly.
     * @throws NumberFormatException    if the time is formatted incorrectly.
     */
    public static Instant calculateStartingInstant(String timeString, Instant endingInstant) throws IllegalArgumentException {
        long time;
        try {
            time = Long.parseLong(timeString.substring(0, timeString.length() - 1));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid amount of time");
        }

        String unit = timeString.substring(timeString.length() - 1);
        long timeInMillis;

        switch (unit) {
            case "s":
                timeInMillis = 1000 * time;
                break;
            case "m":
                timeInMillis = 60000 * time;
                break;
            case "h":
                timeInMillis = 3600000 * time;
                break;
            case "d":
                timeInMillis = 86400000 * time;
                break;
            default:
                throw new IllegalArgumentException("Invalid unit");
        }

        return endingInstant.minus(timeInMillis, ChronoUnit.MILLIS);

    }

    /**
     * Parses a timezone string and returns ZoneID, recognizes basic US timezones.
     *
     * @param timeZoneString A string representing a timezone, must be a recognized American timezone or UTC offset.
     * @return ZoneID representing parsed timezone.
     * @throws DateTimeException if the timezone is invalid.
     */
    public static ZoneId parseTimeZoneString(String timeZoneString) throws DateTimeException {
        HashMap<String, String> aliasMap = new HashMap<>();
        aliasMap.put("HST", "Pacific/Honolulu");
        aliasMap.put("PST", "America/Los_Angeles");
        aliasMap.put("MST", "America/Denver");
        aliasMap.put("AZ", "America/Phoenix ");
        aliasMap.put("CST", "America/Chicago");
        aliasMap.put("EST", "America/New_York");

        return ZoneId.of(timeZoneString, aliasMap);
    }

    public static boolean isInstantAfterOrEquals(Instant instant1, Instant instant2) {
        return (instant1.isAfter(instant2) || instant1.equals(instant2));
    }

    public static boolean isInstantBeforeOrEquals(Instant instant1, Instant instant2) {
        return (instant1.isBefore(instant2) || instant1.equals(instant2));
    }

}
