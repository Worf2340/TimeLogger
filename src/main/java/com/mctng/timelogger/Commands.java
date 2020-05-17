package com.mctng.timelogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class Commands implements CommandExecutor {

    private TimeLogger plugin;


    Commands(TimeLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        BukkitRunnable r = new BukkitRunnable() {
            @SuppressWarnings("Duplicates")
            @Override
            public void run() {
                // /playtime [player]
                if (args.length == 1){
                    String uuid;
                    String playerName;
                    ChatColor chatColor;
                    long currentPlaytime;

                    if (plugin.getServer().getPlayerExact(args[0]) != null){
                        Player player = plugin.getServer().getPlayerExact(args[0]);
                        uuid = player.getUniqueId().toString();
                        playerName = player.getName();
                        chatColor = ChatColor.GREEN;

                        Instant playerJoinTime = plugin.startingTimes.get(player);

                        currentPlaytime = Duration.between(playerJoinTime, Instant.now()).toMillis();

                    }
                    else {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                        uuid = offlinePlayer.getUniqueId().toString();
                        playerName = offlinePlayer.getName();
                        chatColor = ChatColor.RED;
                        currentPlaytime = 0;
                    }

                    long durationMillis = plugin.SQLHandler.getPlaytime(uuid, null, null) + currentPlaytime;

                    if (durationMillis == 0){
                        sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                " has never played on this server.");
                    }
                    else {
                        sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                " has played for " + formatMillis(durationMillis) + " on this server");
                    }

                }
                // /playtime [player] <time> OR
                else if (args.length == 2) {
                    Long time;
                    ChronoUnit units;
                    Integer multiple;

                    String stringUnits = args[1].substring(args[1].length() - 1);

                    try {
                        time = Long.parseLong(args[1].substring(0, args[1].length() - 1));
                    }
                    catch (NumberFormatException e){
                        sender.sendMessage(ChatColor.RED + "Please enter a valid time!");
                        return;
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
                            return;
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
                        return;
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
                        sender.sendMessage(chatColor + playerName    + ChatColor.GRAY +
                                " has played for " + formatMillis(durationMillis) + " in the last " +
                                formatMillis(multiple*time) + ".");
                    }

                }
                // /playtime [player] on [date] <timezone>
                else if ((args.length == 3) || (args.length == 4)) {

                    if (args[1].equalsIgnoreCase("on")){

                        String timezoneString;

                        if (args.length == 4) {
                            if (args[3].startsWith("#tz:")) {
                                timezoneString = args[3].substring(4).toUpperCase();
                            }
                            else {
                                displayUsage(sender);
                                return;
                            }
                        }
                        else {
                            timezoneString = "UTC";
                        }


                        DateTimeFormatter formatter = parseTimezoneString(timezoneString);

                        if (formatter == null) {
                            sender.sendMessage(ChatColor.RED + "Invalid timezone!");
                            return;
                        }

                        DateTimeFormatter formatterUTC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.of("UTC"));

                        // Convert from local timezone to UTC for lookup in db
                        String date = args[2];
                        String beginningOfDayString =  date + " 00:00:00";
                        String endOfDayString = date + " 23:59:59";

                        Instant beginningOfDay;
                        Instant endOfDay;
                        try {
                            beginningOfDay = Instant.from(formatter.parse(beginningOfDayString));
                            endOfDay = Instant.from(formatter.parse(endOfDayString));
                        }
                        catch (DateTimeException e) {
                            sender.sendMessage(ChatColor.RED + "Please format your date in the 'yyyy-mm-dd' format.");
                            return;
                        }


                        String beginningOfDayUtc = beginningOfDay.atZone(ZoneId.of("UTC")).format(formatterUTC);
                        String endOfDayUtc = endOfDay.atZone(ZoneId.of("UTC")).format(formatterUTC);

                        String uuid;
                        String playerName;
                        ChatColor chatColor;
                        long currentPlaytime;

                        if (plugin.getServer().getPlayerExact(args[0]) != null){
                            Player player = plugin.getServer().getPlayerExact(args[0]);
                            uuid = player.getUniqueId().toString();
                            playerName = player.getName();
                            Instant playerJoinTime = plugin.startingTimes.get(player);
                            Instant now = Instant.now();


                            // If the query date is today
                            if ((now.isAfter(beginningOfDay)) && (now.isBefore(endOfDay))) {
                                // If the player joined today
                                if ((playerJoinTime.isAfter(beginningOfDay)) && (playerJoinTime.isBefore(endOfDay))) {
                                    currentPlaytime = Duration.between(playerJoinTime, now).toMillis();
                                } else {
                                    currentPlaytime = Duration.between(playerJoinTime, beginningOfDay).toMillis();
                                }
                            } else {
                                if ((playerJoinTime.isAfter(beginningOfDay)) && (playerJoinTime.isBefore(endOfDay))) {
                                    currentPlaytime = Duration.between(playerJoinTime, endOfDay).toMillis();
                                } else {
                                    currentPlaytime = 0;
                                }
                            }

                            chatColor = ChatColor.GREEN;
                        } else {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                            uuid = offlinePlayer.getUniqueId().toString();
                            playerName = offlinePlayer.getName();

                            currentPlaytime = 0;
                            chatColor = ChatColor.RED;
                        }

                        long durationMillis = plugin.SQLHandler.getPlaytimeInDay(uuid, beginningOfDayUtc, endOfDayUtc) + currentPlaytime;


                        if (durationMillis == 0){
                            sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                    " did not play on " +
                                    date + " in timezone " + timezoneString.toUpperCase() + ".");
                        }
                        else {
                            sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                    " played for " + formatMillis(durationMillis) + " on " +
                                    date  + " in timezone " + timezoneString.toUpperCase() + ".");
                        }



                    }
                    else {
                        displayUsage(sender);
                    }

                }
                // /playtime [player] from [date] [time] to [date] [time] <timezone>
                else if ((args.length == 8) || args.length == 7) {
                    if ((!(args[1].equalsIgnoreCase("from"))) || (!(args[4]).equalsIgnoreCase("to"))) {
                        displayUsage(sender);
                        return;
                    }

                    // Parse timezone
                    String timezoneString;
                    if (args.length == 8) {
                        if (args[7].startsWith("#tz:")) {
                            timezoneString = args[7].substring(4).toUpperCase();
                        } else {
                            displayUsage(sender);
                            return;
                        }
                    }
                    else {
                        timezoneString = "UTC";
                    }

                    DateTimeFormatter formatter = parseTimezoneString(timezoneString);

                    if (formatter == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid timezone!");
                        return;
                    }

                    DateTimeFormatter formatterUTC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.of("UTC"));

                    // Parse datetimes
                    String beginningDateTimeString = args[2] + " " + args[3];
                    String endingDateTimeString = args[5] + " " + args[6];

                    Instant beginningDateTime;
                    Instant endingDateTime;

                    try {
                        beginningDateTime = Instant.from(formatter.parse(beginningDateTimeString));
                        endingDateTime = Instant.from(formatter.parse(endingDateTimeString));
                    }
                    catch (DateTimeException e) {
                        sender.sendMessage(ChatColor.RED + "Please format your datetimes in the 'yyyy-mm-dd HH:mm:ss' format (24h clock).");
                        return;
                    }


                    if (!(endingDateTime.isAfter(beginningDateTime))) {
                        sender.sendMessage(ChatColor.RED + "The ending datetime must be after the beginning datetime!");
                        return;
                    }

                    String beginningDateTimeStringUTC = beginningDateTime.atZone(ZoneId.of("UTC")).format(formatterUTC);
                    String endingDateTimeStringUTC = endingDateTime.atZone(ZoneId.of("UTC")).format(formatterUTC);

                    String uuid;
                    String playerName;
                    ChatColor chatColor;
                    long currentPlaytime;

                    // Check if the player is online
                    if (plugin.getServer().getPlayerExact(args[0]) != null){
                        Player player = plugin.getServer().getPlayerExact(args[0]);
                        uuid = player.getUniqueId().toString();
                        playerName = player.getName();
                        Instant playerJoinTime = plugin.startingTimes.get(player);
                        Instant now = Instant.now();

                        // If the query date range includes now
                        if ((now.isAfter(beginningDateTime)) && (now.isBefore(endingDateTime))) {
                            // If the player joined during the date range
                            if ((playerJoinTime.isAfter(beginningDateTime)) && (playerJoinTime.isBefore(endingDateTime))) {
                                currentPlaytime = Duration.between(playerJoinTime, now).toMillis();
                            } else {
                                currentPlaytime = Duration.between(playerJoinTime, beginningDateTime).toMillis();
                            }
                        } else {
                            if ((playerJoinTime.isAfter(beginningDateTime)) && (playerJoinTime.isBefore(endingDateTime))) {
                                currentPlaytime = Duration.between(playerJoinTime, endingDateTime).toMillis();
                            } else {
                                currentPlaytime = 0;
                            }
                        }

                        chatColor = ChatColor.GREEN;
                    } else {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                        uuid = offlinePlayer.getUniqueId().toString();
                        playerName = offlinePlayer.getName();

                        currentPlaytime = 0;
                        chatColor = ChatColor.RED;
                    }

                    long durationMillis = plugin.SQLHandler.getPlaytimeInDay(uuid, beginningDateTimeStringUTC, endingDateTimeStringUTC) + currentPlaytime;


                    ChatColor italics = ChatColor.ITALIC;
                    if (durationMillis == 0){
                        sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                " did not play in the specified time interval.");
                    }
                    else {
                        sender.sendMessage(chatColor + playerName + ChatColor.GRAY +
                                " played for " + ChatColor.GRAY + formatMillis(durationMillis) +
                                " in the specified time interval.");
                    }

                }

                else {
                    displayUsage(sender);
                }
            }
        };

        r.runTaskAsynchronously(plugin);

        return true;
    }

    static String formatMillis(Long millis) {
        long seconds = millis/1000;
        Duration duration = Duration.ofSeconds(seconds);
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    private void displayUsage(CommandSender player) {
        player.sendMessage(ChatColor.YELLOW + "USAGES:");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] <time>");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] on [date] <#tz:timezone>");
        player.sendMessage(ChatColor.YELLOW + "/playtime [player] from [date] [time] to [date] [time] <#tz:timezone>");
    }

    private DateTimeFormatter parseTimezoneString(String timezoneString) {
        HashMap<String, String> aliasMap = new HashMap<>();
        aliasMap.put("HST", "Pacific/Honolulu");
        aliasMap.put("PST", "America/Los_Angeles");
        aliasMap.put("MST", "America/Denver");
        aliasMap.put("AZ", "America/Phoenix ");
        aliasMap.put("CST", "America/Chicago");
        aliasMap.put("EST", "America/New_York");

        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of(timezoneString, aliasMap));
            return formatter;
        }
        catch (DateTimeException e) {
            return null;
        }
    }

    void getPlaytimeSince(){

    }

}
