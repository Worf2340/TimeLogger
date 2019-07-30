package com.mctng.timelogger;

import org.bukkit.Statistic;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {

    private TimeLogger plugin;
    Command (TimeLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (args.length != 1) {
            return false;
        }

        if (plugin.getServer().getPlayerExact(args[0]) == null){
            return false;
        }

        Player player = plugin.getServer().getPlayerExact(args[0]);
        int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        sender.sendMessage(Util.ticksToTime(ticksPlayed));
        return true;
    }
}
