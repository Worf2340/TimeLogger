package com.mctng.timelogger;

import com.mctng.timelogger.utils.DateTimeUtil;
import com.mctng.timelogger.utils.TabText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class TimeLoggerLeaderboard {

    private int rankingListSize;
    private Instant startingInstant;
    private Instant endingInstant;
    private TimeLogger plugin;
    private ArrayList<TimeLoggerRankedPlayer> leaderboard;
    private ArrayList<String> onlinePlayers;

    public TimeLoggerLeaderboard(int leaderboardSize, Instant startingInstant, Instant endingInstant, TimeLogger plugin) {
        this.startingInstant = startingInstant;
        this.endingInstant = endingInstant;
        this.rankingListSize = leaderboardSize;
        this.plugin = plugin;
        this.onlinePlayers = new ArrayList<>();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            onlinePlayers.add(p.getUniqueId().toString());
        }

    }

    public String getFormattedLeaderboard(String timeString) {

        if (leaderboard.size() == 0) {
            return ChatColor.YELLOW + "No players played during that time period.";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

        String date = formatter.format(startingInstant);
        String header = ChatColor.YELLOW + "PlayTime Leaderboard " + timeString + ":\n";
        StringBuilder tabTextBuilder = new StringBuilder("RANK`NAME`PLAYTIME\n");
        for (int i = 0; i < this.rankingListSize; i++) {
            tabTextBuilder
                    .append(i + 1).append("`")
                    .append(leaderboard.get(i).getPlayer().getName())
                    .append("`")
                    .append(DateTimeUtil.formatMillis(leaderboard.get(i).getPlayTimeInMillis()))
                    .append("\n");
        }

        TabText tt = new TabText(tabTextBuilder.toString());
        tt.setPageHeight(1000);
        tt.setTabs(10, 25);

        return header + tt.getPage(1, false);
    }

    public void initializeLeaderboard() {
        ArrayList<String> playerList = plugin.getSQLHandler().getPlayers();

        for (String uuid : onlinePlayers) {
            if (!(playerList.contains(uuid))) {
                playerList.add(uuid);
            }
        }

        leaderboard = new ArrayList<>();
        for (String uuidString : playerList) {

            TimeLoggerRankedPlayer player = new TimeLoggerRankedPlayer(UUID.fromString(uuidString), plugin);
            player.savePlayTimeInMillisBetweenInstants(startingInstant, endingInstant);

            if (player.getPlayTimeInMillis() > 0) {
                leaderboard.add(player);
            }
        }

        leaderboard.sort(Collections.reverseOrder());

        if (this.rankingListSize > leaderboard.size()) {
            this.rankingListSize = leaderboard.size();
        }
    }

}
