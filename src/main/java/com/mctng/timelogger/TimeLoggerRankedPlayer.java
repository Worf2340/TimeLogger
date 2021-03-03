package com.mctng.timelogger;

import java.time.Instant;
import java.util.UUID;

class TimeLoggerRankedPlayer extends TimeLoggerPlayer implements Comparable<TimeLoggerRankedPlayer> {

    private Long playTimeInMillis;

    TimeLoggerRankedPlayer(UUID uuid, TimeLogger plugin) {
        super(uuid, plugin);
    }

    void savePlayTimeInMillisBetweenInstants(Instant startingInstant, Instant endingInstant) {
        this.playTimeInMillis = getPlayTimeInMillisBetweenInstants(startingInstant, endingInstant);
    }


    Long getPlayTimeInMillis() {
        return playTimeInMillis;
    }

    @Override
    public int compareTo(TimeLoggerRankedPlayer o) {
        return playTimeInMillis.compareTo(o.getPlayTimeInMillis());
    }

}
