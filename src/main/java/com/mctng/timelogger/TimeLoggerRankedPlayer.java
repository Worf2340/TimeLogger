package com.mctng.timelogger;

import java.time.Instant;
import java.util.UUID;

class TimeLoggerRankedPlayer extends TimeLoggerPlayer implements Comparable<TimeLoggerRankedPlayer> {

    private Long playTimeInMillis;

    public TimeLoggerRankedPlayer(UUID uuid, TimeLogger plugin) {
        super(uuid, plugin);
    }

    public void savePlayTimeInMillisBetweenInstants(Instant startingInstant, Instant endingInstant) {
        this.playTimeInMillis = getPlayTimeInMillisBetweenInstants(startingInstant, endingInstant);
    }

    @Override
    public int compareTo(TimeLoggerRankedPlayer o) {
        return getPlayTimeInMillis().compareTo(o.playTimeInMillis);
    }

    public Long getPlayTimeInMillis() {
        return playTimeInMillis;
    }
}
