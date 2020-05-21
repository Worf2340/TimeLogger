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
        BukkitRunnable r = new BukkitRunnable() {
            @SuppressWarnings("Duplicates")
            public void run() {
                TimeLoggerLeaderboard leaderboard;

                // /playtimelb
                if (args.length == 0) {
                    LocalDate localDate = YearMonth.now().atDay(1);
                    Instant startingInstant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
                    leaderboard = new TimeLoggerLeaderboard(10, startingInstant, Instant.now(), plugin);
                }

                // /playtimelb [size]
                else if (args.length == 1) {
                    int leaderboardSize;

                    try {
                        leaderboardSize = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                        return;
                    }

                    LocalDate localDate = YearMonth.now().atDay(1);
                    Instant startingInstant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();

                    leaderboard = new TimeLoggerLeaderboard(leaderboardSize, startingInstant, Instant.now(), plugin);
                }

                // /playtimelb [size] [time]
                else if (args.length == 2) {
                    int leaderboardSize;

                    try {
                        leaderboardSize = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                        return;
                    }

                    Instant endingInstant = Instant.now();
                    Instant startingInstant = DateTimeUtil.calculateStartingInstant(args[1], endingInstant);
                    leaderboard = new TimeLoggerLeaderboard(leaderboardSize, startingInstant, endingInstant, plugin);
                }

                // /playtimelb [size] since [date]
                else if (args.length == 3) {
                    int leaderboardSize;
                    try {
                        leaderboardSize = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                        return;
                    }

                    LocalDate date;
                    try {
                        date = LocalDate.parse(args[2]);
                    } catch (DateTimeParseException e) {
                        sender.sendMessage(ChatColor.RED + "Please format your date in the yyyy-MM-dd format.");
                        return;
                    }

                    Instant beginningOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
                    leaderboard = new TimeLoggerLeaderboard(leaderboardSize, beginningOfDay, Instant.now(), plugin);
                } else {
                    displayUsage(sender);
                    return;
                }

                sender.sendMessage(leaderboard.getFormattedLeaderboard());

            }
        };

        r.runTaskAsynchronously(plugin);

        return true;

    }

    private void displayUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "USAGES:");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb <size> <time>");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb [size] since [date]");
    }
}
