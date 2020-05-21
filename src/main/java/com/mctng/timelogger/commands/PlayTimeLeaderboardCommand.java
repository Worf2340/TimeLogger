package com.mctng.timelogger.commands;

import com.mctng.timelogger.TimeLogger;
import com.mctng.timelogger.TimeLoggerLeaderboard;
import com.mctng.timelogger.utils.DateTimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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

        // /playtimelb [size] [time]
        else if (args.length == 2) {

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
        TimeLogger.newChain()
                .asyncFirst(() -> new TimeLoggerLeaderboard(leaderboardSize, startingInstant, endingInstant, plugin))
                .syncLast((leaderboard) -> sender.sendMessage(leaderboard.getFormattedLeaderboard()))
                .execute();

        return true;

    }

    private void displayUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "USAGES:");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb <size> <time>");
        sender.sendMessage(ChatColor.YELLOW + "/playtimelb [size] since [date]");
    }
}
