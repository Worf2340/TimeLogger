package com.mctng.timelogger;

class TimeLoggerSavedPlayer {

    private String uuid;
    private long playTime;
    private String startingTime;
    private String endingTime;

    TimeLoggerSavedPlayer(String uuid, long playTime, String startingTime, String endingTime) {
        this.uuid = uuid;
        this.playTime = playTime;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    String getUuid() {
        return uuid;
    }

    long getPlayTime() {
        return playTime;
    }

    String getStartingTime() {
        return startingTime;
    }

    String getEndingTime() {
        return endingTime;
    }
}
