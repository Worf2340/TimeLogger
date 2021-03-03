package com.mctng.timelogger;

import java.util.UUID;

class TimeLoggerRecord {

    private final UUID uuid;
    private final String startingTime;
    private final String endingTime;
    private final long playTime;

    TimeLoggerRecord(String uuidString, long playTime, String startingTime, String endingTime) {
        this.uuid = UUID.fromString(uuidString);
        this.startingTime = startingTime;
        this.endingTime = endingTime;
        this.playTime = playTime;
    }

    UUID getUUID() {
        return uuid;
    }

    String getStartingTime() {
        return startingTime;
    }

    String getEndingTime() {
        return endingTime;
    }

    long getPlayTime() {
        return playTime;
    }
}
