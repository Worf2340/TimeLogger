package com.mctng.timelogger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
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
import java.util.ArrayList;
import java.util.Collections;

public class PlaytimeRankingsCommand implements CommandExecutor {

    private TimeLogger plugin;


    PlaytimeRankingsCommand(TimeLogger plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BukkitRunnable r = new BukkitRunnable() {
            @SuppressWarnings("Duplicates")
            public void run(){
                if ((args.length == 1) || (args.length == 2)){

                    ArrayList<String> playerList = plugin.SQLHandler.getPlayers();
                    ArrayList<TimeLoggerPlayer> playerRankings = new ArrayList<>();

                    for (String uuidString : playerList) {

                        Long time;
                        ChronoUnit units;
                        Integer multiple;

                        String stringUnits = args[0].substring(args[0].length() - 1);

                        try {
                            time = Long.parseLong(args[0].substring(0, args[0].length() - 1));
                        } catch (NumberFormatException e) {
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
                        } catch (DateTimeException e) {
                            System.out.println("That number is too big!");
                            return;
                        }

                        if (plugin.getServer().getPlayer(uuidString) != null) {
                            Player player = plugin.getServer().getPlayer(uuidString);
                            Instant playerJoinTime = plugin.startingTimes.get(player);

                            if ((startingInstant.isBefore(playerJoinTime)) || startingInstant.equals(playerJoinTime)) {
                                currentPlaytime = Duration.between(playerJoinTime, endingInstant).toMillis();
                            } else {
                                currentPlaytime = Duration.between(startingInstant, endingInstant).toMillis();
                            }
                        } else {
                            currentPlaytime = 0;
                        }

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

                        String endingInstantString = formatter.format(endingInstant);
                        String startingInstantString = formatter.format(startingInstant);


                        long durationMillis = plugin.SQLHandler.getPlaytime(uuidString, startingInstantString, endingInstantString) + currentPlaytime;

                        if (durationMillis > 0) {
                            playerRankings.add(new TimeLoggerPlayer(uuidString, durationMillis));
                        }
                    }


                    playerRankings.sort(Collections.reverseOrder());

                    int numberOfPlayers;

                    if (args.length == 2){
                        numberOfPlayers = Integer.parseInt(args[1]);
                    }
                    else {
                        numberOfPlayers = 10;
                    }

                    if (numberOfPlayers > playerRankings.size()) {
                        numberOfPlayers = playerRankings.size();
                    }



                    StringBuilder multilineString = new StringBuilder("RANK`NAME`PLAYTIME\n");
                    multilineString.append("---------------`------------------`----------------\n");


                    for (int i = 0; i < numberOfPlayers; i++) {

                        multilineString.append(i+1).append("`").append(playerRankings.get(i).getPlayer().getName())
                                .append("`").append(Commands.formatMillis(playerRankings.get(i).getPlayTime())).append("\n");
                    }

                    TabText tt = new TabText(multilineString.toString());
                    tt.setPageHeight(1000);
                    tt.setTabs(10, 25);
                    String printedText = tt.getPage(1, false); // get your formatted page, for console or chat area
                    sender.sendMessage("--------------------------------");
                    sender.sendMessage("PlayTime leaderboard for the past " + args[0] + ":");
                    sender.sendMessage("--------------------------------");
                    sender.sendMessage(printedText);


                }
            }
        };

        r.runTaskAsynchronously(plugin);

        return true;

    }
}
