package com.mctng.timelogger.listeners;

import com.mctng.timelogger.TimeLogger;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LoginListener implements Listener {

    TimeLogger plugin;

    public LoginListener(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (!plugin.SQLHandler.doesPlayerExist(event.getPlayer())){
            plugin.SQLHandler.insertPlayer(event.getPlayer(), event.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE));
        }
    }
}
