package com.mctng.timelogger;

import com.mctng.timelogger.utils.DateTimeUtil;
import com.mctng.timelogger.utils.TabText;

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

    public TimeLoggerLeaderboard(int leaderboardSize, Instant startingInstant, Instant endingInstant, TimeLogger plugin) {
        this.startingInstant = startingInstant;
        this.endingInstant = endingInstant;
        this.rankingListSize = leaderboardSize;
        this.plugin = plugin;
        initializeLeaderboard();
    }

    public String getFormattedLeaderboard() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

        String date = formatter.format(startingInstant);
        String header = "--------------------------\nPlayTime leaderboard since " + date + ":\n------------------------\n";
        StringBuilder tabTextBuilder = new StringBuilder("RANK`NAME`PLAYTIME\n");

        for (int i = 0; i < this.rankingListSize; i++) {
            tabTextBuilder.append(i + 1).append("`").append(leaderboard.get(i).getPlayer().getName())
                    .append("`").append(DateTimeUtil.formatMillis(leaderboard.get(i).getPlayTimeInMillis())).append("\n");
        }

        TabText tt = new TabText(tabTextBuilder.toString());
        tt.setPageHeight(1000);
        tt.setTabs(10, 25);

        return header + tt.getPage(1, false);
    }

    private void initializeLeaderboard() {
        ArrayList<String> playerList = plugin.getSQLHandler().getPlayers();
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
