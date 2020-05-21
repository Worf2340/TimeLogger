//package com.mctng.timelogger.commands;
//
//import com.mctng.timelogger.TimeLogger;
//import com.mctng.timelogger.TimeLoggerPlayer;
//import com.mctng.timelogger.time.TimeLoggerDuration;
//import com.mctng.timelogger.utils.DateTimeUtil;
//import com.mctng.timelogger.utils.TabText;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.time.DateTimeException;
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.Collections;
//
//public class PlaytimeRankingsCommand implements CommandExecutor {
//
//    private TimeLogger plugin;
//
//
//    public PlaytimeRankingsCommand(TimeLogger plugin) {
//        this.plugin = plugin;
//    }
//
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        BukkitRunnable r = new BukkitRunnable() {
//            @SuppressWarnings("Duplicates")
//            public void run(){
//                if ((args.length == 1) || (args.length == 2)){
//
//                    ArrayList<String> playerList = plugin.SQLHandler.getPlayers();
//                    ArrayList<TimeLoggerPlayer> playerRankings = new ArrayList<>();
//
//                    for (String uuidString : playerList) {
//
//                        TimeLoggerDuration timeLoggerDuration = DateTimeUtil.parseTime(args[1]);
//
//                        if (timeLoggerDuration == null){
//                            sender.sendMessage(ChatColor.RED + "Please enter a valid unit of time!");
//                            return;
//                        }
//
//                        Instant startingInstant;
//                        try {
//                            startingInstant = Instant.now().minus(timeLoggerDuration.getTimeInMillis(), ChronoUnit.MILLIS);
//                        }
//                        catch (DateTimeException e){
//                            System.out.println("That number is too big!");
//                            return;
//                        }
//
//
//                        long playTimeInMillis = PlayTime.getPlaytimeBetween(args[0], startingInstant, Instant.now(), plugin);
//
//
//                        if (playTimeInMillis > 0) {
//                            playerRankings.add(new TimeLoggerPlayer(uuidString, playTimeInMillis));
//                        }
//                    }
//
//
//                    playerRankings.sort(Collections.reverseOrder());
//
//                    int numberOfPlayers;
//
//                    if (args.length == 2){
//                        numberOfPlayers = Integer.parseInt(args[1]);
//                    }
//                    else {
//                        numberOfPlayers = 10;
//                    }
//
//                    if (numberOfPlayers > playerRankings.size()) {
//                        numberOfPlayers = playerRankings.size();
//                    }
//
//
//
//                    StringBuilder multilineString = new StringBuilder("RANK`NAME`PLAYTIME\n");
//                    multilineString.append("---------------`------------------`----------------\n");
//
//
//                    for (int i = 0; i < numberOfPlayers; i++) {
//
//                        multilineString.append(i+1).append("`").append(playerRankings.get(i).getPlayer().getName())
//                                .append("`").append(DateTimeUtil.formatMillis(playerRankings.get(i).getPlayTime())).append("\n");
//                    }
//
//                    TabText tt = new TabText(multilineString.toString());
//                    tt.setPageHeight(1000);
//                    tt.setTabs(10, 25);
//                    String printedText = tt.getPage(1, false); // get your formatted page, for console or chat area
//                    sender.sendMessage("--------------------------------");
//                    sender.sendMessage("PlayTime leaderboard for the past " + args[0] + ":");
//                    sender.sendMessage("--------------------------------");
//                    sender.sendMessage(printedText);
//
//
//                }
//            }
//        };
//
//        r.runTaskAsynchronously(plugin);
//
//        return true;
//
//    }
//}
