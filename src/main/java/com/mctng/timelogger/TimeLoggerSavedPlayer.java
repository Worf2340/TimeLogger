package com.mctng.timelogger;

public class TimeLoggerSavedPlayer {

    private String uuid;
    private long playTime;
    private String startingTime;
    private String endingTime;

    public TimeLoggerSavedPlayer(String uuid, long playTime, String startingTime, String endingTime) {
        this.uuid = uuid;
        this.playTime = playTime;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public String getUuid() {
        return uuid;
    }

    public long getPlayTime() {
        return playTime;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }
}
