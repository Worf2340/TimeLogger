package com.mctng.timelogger;

import com.mctng.timelogger.listeners.LoginListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TimeLogger extends JavaPlugin {

    public SQLite SQLHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("playtime").setExecutor(new Command(this));
        this.getServer().getPluginManager().registerEvents(new LoginListener(this), this);

        if (Files.notExists(Paths.get(getDataFolder().getPath()))){
            File dir = new File(getDataFolder().getPath());
            dir.mkdir();
        }

        SQLHandler = new SQLite(this, "timelogger.db");
        SQLHandler.createNewTable();
        SQLHandler.addHourColumns();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
