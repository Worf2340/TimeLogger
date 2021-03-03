package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static com.mctng.timelogger.utils.DateTimeUtil.isInstantAfterOrEquals;
import static com.mctng.timelogger.utils.DateTimeUtil.isInstantBeforeOrEquals;

public class TimeLoggerPlayer {

    private final OfflinePlayer player;
    private final TimeLogger plugin;
    private UUID uuid;

    TimeLoggerPlayer(UUID uuid, TimeLogger plugin) {
        if (Bukkit.getPlayer(uuid) != null) {
            this.player = Bukkit.getPlayer(uuid);
        } else {
            this.player = Bukkit.getOfflinePlayer(uuid);
        }

        this.plugin = plugin;
        this.uuid = uuid;
    }

    @SuppressWarnings("deprecation")
    public TimeLoggerPlayer(String playerName, TimeLogger plugin) {

        if (Bukkit.getPlayer(playerName) != null) {
            this.player = Bukkit.getPlayer(playerName);
        } else {
            this.player = Bukkit.getOfflinePlayer(playerName);
        }

        this.uuid = player.getUniqueId();
        this.plugin = plugin;
    }

    public long getTotalPlaytime() {
        long playTime = 0;
        ArrayList<TimeLoggerRecord> records = plugin.getSQLHandler().getPlaytimeRecords(this.uuid.toString());

        for (TimeLoggerRecord record : records) {
            playTime += record.getPlayTime();
        }

        if (this.isOnline()) {
            Instant playerJoinTime = plugin.startingTimes.get(player.getUniqueId());
            playTime += Duration.between(playerJoinTime, Instant.now()).toMillis();
        }

        return playTime;
    }

    public long getPlaytimeBetweenInstants(Instant startingInstant, Instant endingInstant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        String endingInstantString = formatter.format(endingInstant);
        String startingInstantString = formatter.format(startingInstant);

        ArrayList<TimeLoggerRecord> records = plugin.getSQLHandler().getPlaytimeRecords(this.uuid.toString(),
                startingInstantString, endingInstantString);


        long playTime = 0;

        for (TimeLoggerRecord record : records) {
            playTime += record.getPlayTime();
        }

        if (this.isOnline()) {
            Instant playerJoinTime = plugin.startingTimes.get(player.getUniqueId());
            if (isInstantAfterOrEquals(startingInstant, playerJoinTime) && isInstantBeforeOrEquals(endingInstant, Instant.now())) {
                playTime += Duration.between(startingInstant, endingInstant).toMillis();
            } else if (isInstantAfterOrEquals(startingInstant, playerJoinTime) && isInstantAfterOrEquals(endingInstant, Instant.now())) {
                playTime += Duration.between(startingInstant, Instant.now()).toMillis();
            } else if (isInstantBeforeOrEquals(startingInstant, playerJoinTime) && isInstantBeforeOrEquals(endingInstant, Instant.now())) {
                playTime += Duration.between(playerJoinTime, endingInstant).toMillis();
            } else if (isInstantBeforeOrEquals(startingInstant, playerJoinTime) && isInstantAfterOrEquals(endingInstant, Instant.now())) {
                playTime += Duration.between(playerJoinTime, Instant.now()).toMillis();
            }
        }

        return playTime;

    }

    public String getGetColoredName() {
        ChatColor chatColor;
        String playerName = player.getName();

        if (this.isOnline()) {
            chatColor = ChatColor.GREEN;
        } else {
            chatColor = ChatColor.RED;
        }

        return chatColor + playerName;
    }

    private boolean isOnline() {
        return player instanceof Player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    UUID getUUID() {
        return uuid;
    }
}
