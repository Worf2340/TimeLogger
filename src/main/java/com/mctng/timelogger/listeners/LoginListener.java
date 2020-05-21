package com.mctng.timelogger.listeners;

import com.mctng.timelogger.TimeLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;

public class LoginListener implements Listener {

    private TimeLogger plugin;

    public LoginListener(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        plugin.startingTimes.put(event.getPlayer(), Instant.now());
    }
}
