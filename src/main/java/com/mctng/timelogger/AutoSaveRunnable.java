package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

class AutoSaveRunnable {

    private TimeLogger plugin;

    AutoSaveRunnable(TimeLogger plugin) {
        this.plugin = plugin;
    }

    void begin() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<UUID> onlinePlayers = new ArrayList<>();
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    onlinePlayers.add(p.getUniqueId());
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (UUID uuid : onlinePlayers) {
                            plugin.getSQLHandler().deletePlayerFromAutoSave(uuid.toString());
                            Instant startingTime = plugin.startingTimes.get(uuid);
                            Instant currentTime = Instant.now();
                            long timeElapsed = Duration.between(startingTime, currentTime).toMillis();

                            plugin.getSQLHandler().insertPlayerAutoSave(uuid.toString(), timeElapsed,
                                    formatter.format(startingTime), formatter.format(currentTime));
                            System.out.println("Done");
                        }
                    }
                }.runTaskAsynchronously(plugin);
            }
        }.runTaskTimer(plugin, 1L, 1200L);
    }

}
