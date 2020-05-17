package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class TimeLoggerPlayer implements Comparable<TimeLoggerPlayer>{

    private OfflinePlayer player;
    private long playTime;

    public TimeLoggerPlayer (String uuid, long playTime){

        if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
            this.player = Bukkit.getPlayer(UUID.fromString(uuid));
        }
        else {
            this.player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        }

        this.playTime = playTime;

    }

    public Long getPlayTime() {
        return playTime;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public int compareTo(TimeLoggerPlayer o) {
        return this.getPlayTime().compareTo(o.getPlayTime());
    }
}
