package com.mctng.timelogger;

import com.mctng.timelogger.commands.PlayTimeCommand;
import com.mctng.timelogger.commands.PlayTimeLeaderboardCommand;
import com.mctng.timelogger.listeners.LoginListener;
import com.mctng.timelogger.listeners.LogoutListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class TimeLogger extends JavaPlugin {

    private SQLite SQLHandler;
    public HashMap<UUID, Instant> startingTimes;

    @Override
    public void onEnable() {
        startingTimes = new HashMap<>();

        this.getCommand("playtime").setExecutor(new PlayTimeCommand(this));
        this.getCommand("playtimelb").setExecutor(new PlayTimeLeaderboardCommand(this));
        this.getCommand("playtime")
                .setPermissionMessage(ChatColor.RED + "You don't have permission to perform that command!");
        this.getCommand("playtimelb")
                .setPermissionMessage(ChatColor.RED + "You don't have permission to perform that command!");
        this.getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        this.getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        if (Files.notExists(Paths.get(getDataFolder().getPath()))){
            File dir = new File(getDataFolder().getPath());
            dir.mkdir();
        }

        try {
            SQLHandler = new SQLite(this, "time_logger.db");
            SQLHandler.createTimeLoggerTableIfNotExists();
            SQLHandler.createAutoSaveTableIfNotExists();
            SQLHandler.moveFromAutoSaveToTimeLogger();
            this.getLogger().info("Initialized connection to SQLite.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


        AutoSaveRunnable autoSaveRunnable = new AutoSaveRunnable(this);
        autoSaveRunnable.begin();
    }


    public SQLite getSQLHandler() {
        return SQLHandler;
    }
}
