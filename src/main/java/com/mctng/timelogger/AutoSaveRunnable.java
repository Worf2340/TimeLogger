package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AutoSaveRunnable {

    private TimeLogger plugin;

    public AutoSaveRunnable(TimeLogger plugin) {
        this.plugin = plugin;
    }

    public void begin() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            Instant startingTime = plugin.startingTimes.get(player);
                            Instant currentTime = Instant.now();
                            long timeElapsed = Duration.between(startingTime, currentTime).toMillis();

                            plugin.getSQLHandler().insertPlayerAutoSave(player.getUniqueId().toString(), timeElapsed,
                                    formatter.format(startingTime), formatter.format(currentTime));

                            System.out.println("Added player");
                        }
                    }
                }.runTaskAsynchronously(plugin);
            }
        }.runTaskTimer(plugin, 1L, 100L);
    }

}
