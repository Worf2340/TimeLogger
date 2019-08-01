package com.mctng.timelogger;

import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class UpdatePlaytimeTask extends BukkitRunnable {

    TimeLogger plugin;

    public UpdatePlaytimeTask(TimeLogger plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}
