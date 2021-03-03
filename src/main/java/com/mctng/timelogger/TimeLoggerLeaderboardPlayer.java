package com.mctng.timelogger;

import java.util.UUID;

public class TimeLoggerLeaderboardPlayer extends TimeLoggerPlayer implements Comparable<TimeLoggerLeaderboardPlayer> {
    private final UUID uuid;
    private Long playTime;

    TimeLoggerLeaderboardPlayer(String uuidString, TimeLogger plugin, Long playTime) {
        super(UUID.fromString(uuidString), plugin);
        this.uuid = UUID.fromString(uuidString);
        this.playTime = playTime;
    }

    TimeLoggerLeaderboardPlayer(UUID uuid, TimeLogger plugin, Long playTime) {
        super(uuid, plugin);
        this.uuid = uuid;
        this.playTime = playTime;
    }

    Long getPlayTime() {
        return playTime;
    }

    void setPlayTime(Long playTime) {
        this.playTime = playTime;
    }

    @Override
    public int compareTo(TimeLoggerLeaderboardPlayer o) {
        return playTime.compareTo(o.getPlayTime());
    }

}
