package com.mctng.timelogger.listeners;

import com.mctng.timelogger.TimeLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogoutListener implements Listener {

    private TimeLogger plugin;

    public LogoutListener(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
        Instant startingTime = plugin.startingTimes.get(event.getPlayer());
        Instant currentTime = Instant.now();
        long timeElapsed = Duration.between(startingTime, currentTime).toMillis();
        plugin.SQLHandler.insertPlayer(event.getPlayer(), timeElapsed, formatter.format(startingTime), formatter.format(currentTime));
    }
}
