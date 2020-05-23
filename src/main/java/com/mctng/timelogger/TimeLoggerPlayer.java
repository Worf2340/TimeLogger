package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.mctng.timelogger.utils.DateTimeUtil.isInstantAfterOrEquals;
import static com.mctng.timelogger.utils.DateTimeUtil.isInstantBeforeOrEquals;

public class TimeLoggerPlayer {

    private final OfflinePlayer player;
    private final TimeLogger plugin;
    private final String uuidString;

    TimeLoggerPlayer(UUID uuid, TimeLogger plugin) {
        if (Bukkit.getPlayer(uuid) != null) {
            this.player = Bukkit.getPlayer(uuid);
        } else {
            this.player = Bukkit.getOfflinePlayer(uuid);
        }

        this.plugin = plugin;
        this.uuidString = uuid.toString();
    }

    @SuppressWarnings("deprecation")
    public TimeLoggerPlayer(String playerName, TimeLogger plugin) {

        if (Bukkit.getPlayer(playerName) != null) {
            this.player = Bukkit.getPlayer(playerName);
        } else {
            this.player = Bukkit.getOfflinePlayer(playerName);
        }

        this.plugin = plugin;
        this.uuidString = this.player.getUniqueId().toString();
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

    public long getTotalPlayTimeInMillis() {
        long currentPlaytime;
        if (this.isOnline()) {
            Instant playerJoinTime = plugin.startingTimes.get(player);
            currentPlaytime = Duration.between(playerJoinTime, Instant.now()).toMillis();
        } else {
            currentPlaytime = 0;
        }

        return plugin.getSQLHandler().getPlaytime(uuidString) + currentPlaytime;
    }

    public long getPlayTimeInMillisBetweenInstants(Instant startingInstant, Instant endingInstant) {
        long currentPlaytime;
        Instant now = Instant.now();

        if (this.isOnline()) {
            Instant playerJoinTime = plugin.startingTimes.get(player);
            if ((startingInstant.isBefore(playerJoinTime)) && endingInstant.isBefore(playerJoinTime)) {
                currentPlaytime = 0;
            } else if (isInstantAfterOrEquals(startingInstant, playerJoinTime) && isInstantBeforeOrEquals(endingInstant, now)) {
                currentPlaytime = Duration.between(startingInstant, endingInstant).toMillis();
            } else if (isInstantAfterOrEquals(startingInstant, playerJoinTime) && isInstantAfterOrEquals(endingInstant, now)) {
                currentPlaytime = Duration.between(startingInstant, now).toMillis();
            } else if (isInstantBeforeOrEquals(startingInstant, playerJoinTime) && isInstantBeforeOrEquals(endingInstant, now)) {
                currentPlaytime = Duration.between(playerJoinTime, endingInstant).toMillis();
            } else if (isInstantBeforeOrEquals(startingInstant, playerJoinTime) && isInstantAfterOrEquals(endingInstant, now)) {
                currentPlaytime = Duration.between(playerJoinTime, now).toMillis();
            } else {
                currentPlaytime = 0;
            }
        } else {
            currentPlaytime = 0;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        String endingInstantString = formatter.format(endingInstant);
        String startingInstantString = formatter.format(startingInstant);

        return plugin.getSQLHandler().getPlaytimeBetweenTimes(uuidString, startingInstantString, endingInstantString) + currentPlaytime;
    }

    private boolean isOnline() {
        return player instanceof Player;
    }


    public OfflinePlayer getPlayer() {
        return player;
    }
}
