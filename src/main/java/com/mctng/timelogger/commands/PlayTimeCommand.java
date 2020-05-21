package com.mctng.timelogger.commands;

import com.mctng.timelogger.TimeLogger;
import com.mctng.timelogger.TimeLoggerPlayer;
import com.mctng.timelogger.utils.DateTimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.*;
import java.time.format.DateTimeParseException;

public class PlayTimeCommand implements CommandExecutor {

    private TimeLogger plugin;


    public PlayTimeCommand(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        // /playtime [player]
        if (args.length == 1) {
            TimeLoggerPlayer player = new TimeLoggerPlayer(args[0], plugin);

            // Get playtime async
            TimeLogger.newChain()
                    .asyncFirst(player::getTotalPlayTimeInMillis)
                    .syncLast((playTime) -> {
                        if (playTime == 0) {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " has never played on this server.");
                        } else {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " has played for " + DateTimeUtil.formatMillis(playTime) + " on this server");
                        }
                    })
                    .execute();
        }

        // /playtime [player] <time>
        else if (args.length == 2) {

            Instant startingInstant;
            try {
                startingInstant = DateTimeUtil.calculateStartingInstant(args[1], Instant.now());
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid amount of time.");
                return true;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Please enter a valid unit of time.");
                return true;
            }


            TimeLoggerPlayer player = new TimeLoggerPlayer(args[0], plugin);
            // Get playtime async
            TimeLogger.newChain()
                    .asyncFirst(() -> player.getPlayTimeInMillisBetweenInstants(startingInstant, Instant.now()))
                    .syncLast((playTime) -> {
                        if (playTime == 0) {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " has not played in the last " + args[1]);
                        } else {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " has played for " + DateTimeUtil.formatMillis(playTime) + " in the last " + args[1] + ".");
                        }
                    })
                    .execute();

        }

        // /playtime [player] on [date] <timezone>
        else if ((args.length == 3) || (args.length == 4)) {

            if (!(args[1].equalsIgnoreCase("on"))) {
                displayUsage(sender);
                return true;
            }

            String timezoneString;
            if (args.length == 4) {
                if (args[3].startsWith("#tz:")) {
                    timezoneString = args[3].substring(4).toUpperCase();
                } else {
                    displayUsage(sender);
                    return true;
                }
            } else {
                timezoneString = "UTC";
            }

            LocalDate date;
            try {
                date = LocalDate.parse(args[2]);
            } catch (DateTimeParseException e) {
                sender.sendMessage(ChatColor.RED + "Please format your date in the yyyy-MM-dd format.");
                return true;
            }

            ZoneId timeZone;
            try {
                timeZone = DateTimeUtil.parseTimeZoneString(timezoneString);
            } catch (DateTimeException e) {
                sender.sendMessage(ChatColor.RED + "Invalid timezone.");
                return true;
            }

            Instant beginningOfDay = date.atStartOfDay(timeZone).toInstant();
            Instant endOfDay = date.atStartOfDay(timeZone).toInstant();

            TimeLoggerPlayer player = new TimeLoggerPlayer(args[0], plugin);

            // Get playtime async
            TimeLogger.newChain()
                    .asyncFirst(() -> player.getPlayTimeInMillisBetweenInstants(beginningOfDay, endOfDay))
                    .syncLast((playTime) -> {
                        if (playTime == 0) {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " did not play on " +
                                    args[2] + " in timezone " + timezoneString.toUpperCase() + ".");
                        } else {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " played for " + DateTimeUtil.formatMillis(playTime) + " on " +
                                    args[2] + " in timezone " + timezoneString.toUpperCase() + ".");
                        }
                    })
                    .execute();
        }

        // /playtime [player] from [date] [time] to [date] [time] <timezone>
        else if ((args.length == 8) || args.length == 7) {

            if ((!(args[1].equalsIgnoreCase("from"))) || (!(args[4]).equalsIgnoreCase("to"))) {
                displayUsage(sender);
                return true;
            }

            // Parse timezone
            String timezoneString;
            if (args.length == 8) {
                if (args[7].startsWith("#tz:")) {
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

            LocalDateTime startingDateTime;
            LocalDateTime endingDateTime;
            try {
                startingDateTime = LocalDateTime.parse(args[2] + "T" + args[3]);
                endingDateTime = LocalDateTime.parse(args[5] + "T" + args[6]);
            } catch (DateTimeParseException e) {
                sender.sendMessage(ChatColor.RED + "Please format your datetime in the yyyy-mm-dd HH:mm:ss format.");
                return true;
            }

            Instant startingInstant = startingDateTime.atZone(timeZone).toInstant();
            Instant endingInstant = endingDateTime.atZone(timeZone).toInstant();

            TimeLoggerPlayer player = new TimeLoggerPlayer(args[0], plugin);

            // Get playtime async
            TimeLogger.newChain()
                    .asyncFirst(() -> player.getPlayTimeInMillisBetweenInstants(startingInstant, endingInstant))
                    .syncLast((playTime) -> {
                        if (playTime == 0) {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " did not play in the specified time interval.");
                        } else {
                            sender.sendMessage(player.getGetColoredName() + ChatColor.GRAY +
                                    " played for " + ChatColor.GRAY + DateTimeUtil.formatMillis(playTime) +
                                    " in the specified time interval.");
                        }
                    })
                    .execute();

        } else {
            displayUsage(sender);
        }
        return true;

    }


    private void displayUsage(CommandSender player) {
        player.sendMessage(ChatColor.YELLOW + "USAGES:");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] <time>");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] on [date] <#tz:timezone>");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] from [date] [time] to [date] [time] <#tz:timezone>");
    }


}
