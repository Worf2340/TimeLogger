package com.mctng.timelogger;

import com.mctng.timelogger.listeners.LoginListener;
import com.mctng.timelogger.listeners.LogoutListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public final class TimeLogger extends JavaPlugin {

    public SQLite SQLHandler;
    public int startingHour;
    public HashMap<Player, Instant> startingTimes;

    @Override
    public void onEnable() {
        startingTimes = new HashMap<>();
        // Plugin startup logic
        this.getCommand("playtime").setExecutor(new Commands(this));
        this.getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        this.getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        if (Files.notExists(Paths.get(getDataFolder().getPath()))){
            File dir = new File(getDataFolder().getPath());
            dir.mkdir();
        }

        SQLHandler = new SQLite(this, "time_logger.db");
        SQLHandler.createNewTable();

        startingHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
