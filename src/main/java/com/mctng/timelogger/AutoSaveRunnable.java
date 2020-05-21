package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AutoSaveRunnable extends BukkitRunnable {

    private TimeLogger plugin;

    public AutoSaveRunnable(TimeLogger plugin) {
        this.plugin = plugin;
    }

    public void begin() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                autoSave();
            }
        }, 0L, 100L);
    }

    private void autoSave() {
        plugin.getSQLHandler().clearAutoSave();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            Instant startingTime = plugin.startingTimes.get(player);
            Instant currentTime = Instant.now();
            long timeElapsed = Duration.between(startingTime, currentTime).toMillis();
            plugin.getSQLHandler().insertPlayerAutoSave(player.getUniqueId().toString(), timeElapsed,
                    formatter.format(startingTime), formatter.format(currentTime));
            System.out.println("Added player");
        }
    }

    @Override
    public void run() {

    }

//    public void mergeAutoSave() {
//        ArrayList<String> autoSavedPlayers = plugin.getSQLHandler().getAllAutoSavedPlayers();
//
//        for (String uuid : autoSavedPlayers) {
//            plugin.getSQLHandler().mergeAutoSavedPlayer(uuid);
//        }
//    }
}
