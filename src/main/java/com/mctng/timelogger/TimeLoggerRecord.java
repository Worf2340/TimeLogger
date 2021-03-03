package com.mctng.timelogger;

public class TimeLoggerRecord {

    private String uuid;
    private String startingTime;
    private String endingTime;

    public TimeLoggerRecord(String uuid, String startingTime, String endingTime) {
        this.uuid = uuid;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

    public String getUuid() {
        return uuid;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }
}
