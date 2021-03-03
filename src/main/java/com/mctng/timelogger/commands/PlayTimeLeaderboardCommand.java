package com.mctng.timelogger.commands;

import com.mctng.timelogger.TimeLogger;
import com.mctng.timelogger.TimeLoggerLeaderboard;
import com.mctng.timelogger.utils.DateTimeUtil;
import net.minecraft.server.v1_7_R4.ExceptionInvalidNumber;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PlayTimeLeaderboardCommand implements CommandExecutor {

    private TimeLogger plugin;


    public PlayTimeLeaderboardCommand(TimeLogger plugin) {
        this.plugin = plugin;
    }


    @SuppressWarnings("Duplicates")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int leaderboardSize;
        Instant startingInstant;
        Instant endingInstant;
        String timeString;
        TimeLoggerLeaderboard leaderboard;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

        // /playtimelb
        if (args.length == 0) {
            LocalDate localDate = YearMonth.now().atDay(1);
            startingInstant = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
            endingInstant = Instant.now();
            leaderboardSize = 10;
            timeString = "since " + formatter.format(startingInstant);
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
            timeString = "since " + formatter.format(startingInstant);

        }

        // /playtimelb [size] [time]
        else if (args.length == 2) {
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

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
            try {
                startingInstant = DateTimeUtil.calculateStartingInstant(args[1], endingInstant);
            } catch (NumberFormatException | ExceptionInvalidNumber e) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid amount of time.");
                return true;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid unit of time.");
                return true;
            }

            timeString = "for the past " + args[1] + " (since " + formatter2.format(startingInstant) + " UTC)";

        }

        // /playtimelb [size] since [date]
        else if (args.length == 3) {

            if (!(sender.hasPermission("timelogger.playtimelb.custom"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command!");
                return true;
            }

            if (!(args[1].equalsIgnoreCase("since"))) {
                displayUsage(sender);
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
            timeString = "since " + formatter.format(startingInstant);

        }

        // /playtimelb [size] from [date] [time] to [date] [time] <tz>
        else if ((args.length == 8) || (args.length == 7)) {
            if (!(sender.hasPermission("timelogger.playtimelb.custom"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to perform that command!");
                return true;
            }

            if (!(args[1].equalsIgnoreCase("from")) || !(args[4]).equalsIgnoreCase("to")) {
                displayUsage(sender);
                return true;
            }

            try {
                leaderboardSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
                return true;
            }

            LocalDateTime startingDateTime;
            LocalDateTime endingDateTime;

            try {
                startingDateTime = LocalDateTime.parse(args[2] + "T" + args[3]);
                endingDateTime = LocalDateTime.parse(args[5] + "T" + args[6]);
            } catch (DateTimeParseException e) {
                sender.sendMessage(ChatColor.RED + "Please format your datetimes in the yyyy-MM-dd HH mm ss format.");
                return true;
            }

            String timezoneString;
            if (args.length == 8) {
                if (args[7].toLowerCase().startsWith("#tz:")) {
                    timezoneString = args[7].substring(4).toUpperCase();
                } else {
                    displayUsage(sender);
                    return true;
                }
            } else {
                timezoneString = "UTC";
            }

            ZoneId timeZone;
            try {
                timeZone = DateTimeUtil.parseTimeZoneString(timezoneString);
            } catch (DateTimeException e) {
                sender.sendMessage(ChatColor.RED + "Invalid timezone.");
                return true;
            }

            if (endingDateTime.isBefore(startingDateTime)) {
                sender.sendMessage(ChatColor.RED + "The ending time cannot be before the starting time!");
                return true;
            }

            startingInstant = startingDateTime.atZone(timeZone).toInstant();
            endingInstant = endingDateTime.atZone(timeZone).toInstant();

            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(timeZone);
            timeString = "from " + formatter2.format(startingInstant) + " to " + formatter2.format(endingInstant)
                    + " in timezone " + timezoneString.toUpperCase();

        } else {
            displayUsage(sender);
            return true;
        }
        if (startingInstant.isBefore(LocalDateTime.parse("1970-01-01T00:00:00").atZone(ZoneId.of("UTC"))
                .toInstant())) {
            sender.sendMessage(ChatColor.RED + "That date is too far in the past!");
            return true;
        }


        if (leaderboardSize < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid leaderboard size.");
            return true;
        }
        leaderboard = new TimeLoggerLeaderboard
                (leaderboardSize, startingInstant, endingInstant, plugin);

        // Get leaderboard async
        new BukkitRunnable() {
            @Override
            public void run() {
                leaderboard.initializeLeaderboard();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(leaderboard.getFormattedLeaderboard(timeString));
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
