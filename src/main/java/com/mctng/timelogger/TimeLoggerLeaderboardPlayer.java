package com.mctng.timelogger;

import java.util.UUID;

public class TimeLoggerLeaderboardPlayer extends TimeLoggerPlayer implements Comparable<TimeLoggerLeaderboardPlayer> {
    private final String uuidString;
    private Long playTime;

    public TimeLoggerLeaderboardPlayer(String uuidString, TimeLogger plugin, Long playTime) {
        super(UUID.fromString(uuidString), plugin);
        this.uuidString = uuidString;
        this.playTime = playTime;
    }

    public String getUuidString() {
        return uuidString;
    }

    public Long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(Long playTime) {
        this.playTime = playTime;
    }

    @Override
    public int compareTo(TimeLoggerLeaderboardPlayer o) {
        return playTime.compareTo(o.getPlayTime());
    }

}
