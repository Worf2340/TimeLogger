package com.mctng.timelogger.commands;

import com.mctng.timelogger.TimeLogger;
import com.mctng.timelogger.TimeLoggerLeaderboard;
import com.mctng.timelogger.utils.DateTimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class PlayTimeLeaderboardCommand implements CommandExecutor {

    private TimeLogger plugin;


    public PlayTimeLeaderboardCommand(TimeLogger plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int leaderboardSize;
        Instant startingInstant;
        Instant endingInstant;

        // /playtimelb
        if (args.length == 0) {
            LocalDate localDate = YearMonth.now().atDay(1);
            startingInstant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
            endingInstant = Instant.now();
            leaderboardSize = 10;
        }

        // /playtimelb [size]
        else if (args.length == 1) {

            if (!(sender.hasPermission("timelogger.playtimelb.custom"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command!");
                return true;
            }

            if (args[0].equalsIgnoreCase("?")) {
                displayUsage(sender);
                return true;
            }

            try {
                leaderboardSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                return true;
            }

            LocalDate localDate = YearMonth.now().atDay(1);
            startingInstant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
            endingInstant = Instant.now();
        }

        //TODO validate starting date to ensure no earlier than 1970-01-01

        // /playtimelb [size] [time]
        else if (args.length == 2) {

            if (!(sender.hasPermission("timelogger.playtimelb.custom"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command!");
                return true;
            }

            try {
                leaderboardSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                return true;
            }

            endingInstant = Instant.now();
            startingInstant = DateTimeUtil.calculateStartingInstant(args[1], endingInstant);
        }

        // /playtimelb [size] since [date]
        else if (args.length == 3) {

            if (!(sender.hasPermission("timelogger.playtimelb.custom"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command!");
                return true;
            }

            try {
                leaderboardSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                return true;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(args[2]);
            } catch (DateTimeParseException e) {
                sender.sendMessage(ChatColor.RED + "Please format your date in the yyyy-MM-dd format.");
                return true;
            }

            startingInstant = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
            endingInstant = Instant.now();
        } else {
            displayUsage(sender);
            return true;
        }


        // Get leaderboard async
        new BukkitRunnable() {
            @Override
            public void run() {
                TimeLoggerLeaderboard leaderboard = new TimeLoggerLeaderboard
                        (leaderboardSize, startingInstant, endingInstant, plugin);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(leaderboard.getFormattedLeaderboard());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;

    }

    private void displayUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "USAGES:");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb (default size=10, date=beginning of month)");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb <size> <time>");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb [size] since [date]");
    }
}
