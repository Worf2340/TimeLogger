package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Commands implements CommandExecutor {

    private TimeLogger plugin;


    Commands(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Long time;
        ChronoUnit units;
        Integer multiple;

        if (args.length == 1){
            String uuid;
            String playerName;
            ChatColor chatColor;

            if (plugin.getServer().getPlayerExact(args[0]) != null){
                Player player = plugin.getServer().getPlayerExact(args[0]);
                uuid = player.getUniqueId().toString();
                playerName = player.getName();
                chatColor = ChatColor.GREEN;
            }
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                uuid = offlinePlayer.getUniqueId().toString();
                playerName = offlinePlayer.getName();
                chatColor = ChatColor.RED;
            }

            long durationMillis = plugin.SQLHandler.getPlaytime(uuid, null, null);

            if (durationMillis == 0){
                sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                        " has never played on this server.");
            }
            else {
                sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                        " has played for " + formatMillis(durationMillis) + " on this server");
            }
            return true;

        }
        else if (args.length == 2){
            String stringUnits = args[1].substring(args[1].length() - 1);

            try {
                time = Long.parseLong(args[1].substring(0, args[1].length() - 1));
            }
            catch (NumberFormatException e){
                sender.sendMessage(ChatColor.RED + "Please enter a valid time!");
                return true;
            }

            switch (stringUnits) {
                case "s":
                    units = ChronoUnit.SECONDS;
                    multiple = 1000;
                    break;
                case "m":
                    units = ChronoUnit.MINUTES;
                    multiple = 60000;
                    break;
                case "h":
                    units = ChronoUnit.HOURS;
                    multiple = 3600000;
                    break;
                case "d":
                    units = ChronoUnit.DAYS;
                    multiple = 86400000;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Please enter a valid unit of time!");
                    return true;
            }
            // If the player is online
            long currentPlaytime;
            Instant endingInstant = Instant.now();
            Instant startingInstant;

            try {
                startingInstant = endingInstant.minus(time, units);
            }
            catch (DateTimeException e){
                System.out.println("That number is too big!");
                return true;
            }
            ChatColor chatColor;

            String uuid;
            String playerName;
            if (plugin.getServer().getPlayerExact(args[0]) != null){
                Player player = plugin.getServer().getPlayerExact(args[0]);
                uuid = player.getUniqueId().toString();
                playerName = player.getName();
                Instant playerJoinTime = plugin.startingTimes.get(player);

                if ((startingInstant.isBefore(playerJoinTime)) || startingInstant.equals(playerJoinTime)) {
                    currentPlaytime = Duration.between(playerJoinTime, endingInstant).toMillis();
                }
                else {
                    currentPlaytime = Duration.between(startingInstant, endingInstant).toMillis();
                }
                chatColor = ChatColor.GREEN;
            }
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                uuid = offlinePlayer.getUniqueId().toString();
                playerName = offlinePlayer.getName();

                currentPlaytime = 0;
                chatColor = ChatColor.RED;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

            String endingInstantString = formatter.format(endingInstant);
            String startingInstantString = formatter.format(startingInstant);


            long durationMillis = plugin.SQLHandler.getPlaytime(uuid, startingInstantString, endingInstantString) + currentPlaytime;

            if (durationMillis == 0){
                sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                        " has not played in the last " +
                        formatMillis(multiple*time) + ".");
            }
            else {
                sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                        " has played for " + formatMillis(durationMillis) + " in the last " +
                        formatMillis(multiple*time) + ".");
            }


            return true;
        }
        else {
            return false;
        }
    }

    public static String formatMillis(Long millis) {
        long seconds = millis/1000;
        Duration duration = Duration.ofSeconds(seconds);
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
