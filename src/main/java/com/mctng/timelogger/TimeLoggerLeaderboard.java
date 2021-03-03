package com.mctng.timelogger;

import com.mctng.timelogger.utils.DateTimeUtil;
import com.mctng.timelogger.utils.TabText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.mctng.timelogger.utils.DateTimeUtil.isInstantAfterOrEquals;
import static com.mctng.timelogger.utils.DateTimeUtil.isInstantBeforeOrEquals;

public class TimeLoggerLeaderboard {

    private int rankingListSize;
    private Instant queryStartingInstant;
    private Instant queryEndingInstant;
    private TimeLogger plugin;
    private ArrayList<TimeLoggerLeaderboardPlayer> leaderboard;

    public TimeLoggerLeaderboard(int leaderboardSize, Instant queryStartingInstant, Instant queryEndingInstant, TimeLogger plugin) {
        this.queryStartingInstant = queryStartingInstant;
        this.queryEndingInstant = queryEndingInstant;
        this.rankingListSize = leaderboardSize;
        this.plugin = plugin;

    }

    public String getFormattedLeaderboard(String timeString) {

        if (leaderboard.size() == 0) {
            return ChatColor.YELLOW + "No players played during that time period.";
        }

        String header = ChatColor.YELLOW + "PlayTime Leaderboard " + timeString + ":\n";
        StringBuilder tabTextBuilder = new StringBuilder("RANK`NAME`PLAYTIME\n");
        for (int i = 0; i < this.rankingListSize; i++) {
            tabTextBuilder
                    .append(i + 1).append("`")
                    .append(leaderboard.get(i).getPlayer().getName())
                    .append("`")
                    .append(DateTimeUtil.formatMillis(leaderboard.get(i).getPlayTime()))
                    .append("\n");
        }

        TabText tt = new TabText(tabTextBuilder.toString());
        tt.setPageHeight(1000);
        tt.setTabs(10, 25);

        return header + tt.getPage(1, false);
    }

    public void initializeLeaderboard() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        String endingInstantString = formatter.format(queryEndingInstant);
        String startingInstantString = formatter.format(queryStartingInstant);

        ArrayList<TimeLoggerRecord> timeRecords = plugin.getSQLHandler().getPlaytimeRecords(startingInstantString, endingInstantString);
        leaderboard = new ArrayList<>();


        for (TimeLoggerRecord record : timeRecords) {
            long playTime = 0;

            Instant resultEndingInstant = Instant.from(formatter.parse(record.getEndingTime()));
            Instant resultStartingInstant = Instant.from(formatter.parse(record.getStartingTime()));

            if (isInstantAfterOrEquals(resultStartingInstant, queryStartingInstant)
                    && isInstantBeforeOrEquals(resultEndingInstant, queryEndingInstant)) {
                playTime = Duration.between(resultStartingInstant, resultEndingInstant).toMillis();
            } else if (isInstantAfterOrEquals(resultStartingInstant, queryStartingInstant)
                    && isInstantAfterOrEquals(resultEndingInstant, queryEndingInstant)) {
                playTime = Duration.between(resultStartingInstant, queryEndingInstant).toMillis();
            } else if (isInstantBeforeOrEquals(resultStartingInstant, queryStartingInstant)
                    && isInstantBeforeOrEquals(resultEndingInstant, queryEndingInstant)) {
                playTime = Duration.between(queryStartingInstant, resultEndingInstant).toMillis();
            } else if (isInstantBeforeOrEquals(resultStartingInstant, queryStartingInstant)
                    && isInstantAfterOrEquals(resultEndingInstant, queryEndingInstant)) {
                playTime = Duration.between(queryStartingInstant, queryEndingInstant).toMillis();
            }


            if (containsPlayer(leaderboard, record.getUUID())) {
                for (TimeLoggerLeaderboardPlayer player : leaderboard) {
                    if (player.getUUID().equals(record.getUUID())) {
                        player.setPlayTime(player.getPlayTime() + playTime);
                    }
                }
            } else {
                leaderboard.add(new TimeLoggerLeaderboardPlayer(record.getUUID(), this.plugin, playTime));
            }

        }


        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            long currentPlaytime = 0;
            Instant playerJoinTime = plugin.startingTimes.get(player.getUniqueId());
            if (isInstantAfterOrEquals(queryStartingInstant, playerJoinTime) && isInstantBeforeOrEquals(queryEndingInstant, Instant.now())) {
                currentPlaytime = Duration.between(queryStartingInstant, queryEndingInstant).toMillis();
            } else if (isInstantAfterOrEquals(queryStartingInstant, playerJoinTime) && isInstantAfterOrEquals(queryEndingInstant, Instant.now())) {
                currentPlaytime = Duration.between(queryStartingInstant, Instant.now()).toMillis();
            } else if (isInstantBeforeOrEquals(queryStartingInstant, playerJoinTime) && isInstantBeforeOrEquals(queryEndingInstant, Instant.now())) {
                currentPlaytime = Duration.between(playerJoinTime, queryEndingInstant).toMillis();
            } else if (isInstantBeforeOrEquals(queryStartingInstant, playerJoinTime) && isInstantAfterOrEquals(queryEndingInstant, Instant.now())) {
                currentPlaytime = Duration.between(playerJoinTime, Instant.now()).toMillis();
            }

            if (containsPlayer(leaderboard, player.getUniqueId())) {
                for (TimeLoggerLeaderboardPlayer leaderboardPlayer : leaderboard) {
                    if (leaderboardPlayer.getUUID().toString().equals(player.getUniqueId().toString())) {
                        leaderboardPlayer.setPlayTime(leaderboardPlayer
                                .getPlayTime() + currentPlaytime);
                    }
                }
            } else {
                leaderboard.add(new TimeLoggerLeaderboardPlayer(player.getUniqueId().toString(), this.plugin, currentPlaytime));
            }


        }


        leaderboard.sort(Collections.reverseOrder());

        if (this.rankingListSize > leaderboard.size()) {
            this.rankingListSize = leaderboard.size();
        }
    }

    private boolean containsPlayer(final List<TimeLoggerLeaderboardPlayer> list, final UUID uuid) {
        return list.stream().map(TimeLoggerLeaderboardPlayer::getUUID).anyMatch(uuid::equals);
    }

}
