package com.mctng.timelogger;

import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@SuppressWarnings("Duplicates")
public class SQLite {

    String fileName;
    TimeLogger plugin;

    public SQLite(TimeLogger plugin, String fileName) {
        this.fileName = fileName;
        this.plugin = plugin;
    }

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/" + fileName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createNewTable() {

        // Create new table
        String sql = "CREATE TABLE IF NOT EXISTS time_logger (\n"
                + " id integer PRIMARY KEY,\n"
                + " uuid text NOT NULL,\n"
                + " play_time integer NOT NULL,\n"
                + " starting_time char NOT NULL,\n"
                + " ending_time char NOT NULL\n"
                + ");";


        try {
            Connection conn = connect();
            Statement statement = conn.createStatement();
            statement.execute(sql);
            this.plugin.getLogger().info("Connected to SQLite database.");
            this.plugin.getLogger().info("Initialized SQLite table.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void insertPlayer(Player player, long playTime, String startingTime, String endingTime) {
        String sql = "INSERT INTO time_logger(uuid,play_time,starting_time,ending_time) VALUES (?,?,?,?)";
        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setLong(2, playTime);
            pstmt.setString(3, startingTime);
            pstmt.setString(4, endingTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public long getPlaytime(String uuid, String time1, String time2) {

        String sql;
        if ((time1 == null) && (time2 == null)){
            sql = "SELECT play_time FROM time_logger \n" +
                    "WHERE uuid = ?";
        }
        else {
            sql = "SELECT play_time FROM time_logger \n" +
                    "WHERE starting_time BETWEEN ? AND ? \n" +
                    "AND uuid = ?";
        }


        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if ((time1 != null) && (time2 != null)) {
                pstmt.setString(1, time1);
                pstmt.setString(2, time2);
                pstmt.setString(3, uuid);
            }
            else {
                pstmt.setString(1, uuid);
            }

            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            long timePlayed = 0;
            while (rs.next()) {
                timePlayed += rs.getLong("play_time");
            }

            if ((time1 == null) && (time2 == null)){
                return timePlayed;
            }
            else {
                return timePlayed + getLeftoverPlaytime(uuid, time1, time2);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Long getPlaytimeInDay (String uuid, String beginningOfDayString, String endOfDayString) {
        String sql = "SELECT play_time, starting_time, ending_time FROM time_logger \n" +
                "WHERE (starting_time BETWEEN ? AND ?) \n" +
                "OR (ending_time BETWEEN ? AND ?) \n" +
                "AND uuid = ?";
        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, beginningOfDayString);
            pstmt.setString(2, endOfDayString);
            pstmt.setString(3, beginningOfDayString);
            pstmt.setString(4, endOfDayString);
            pstmt.setString(5, uuid);

            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
            Instant beginningOfDay = Instant.from(formatter.parse(beginningOfDayString));
            Instant endOfDay = Instant.from(formatter.parse(endOfDayString));
            long playtime = 0;

            while (rs.next()) {

                String startingTimeString = rs.getString("starting_time");
                String endingTimeString = rs.getString("ending_time");

                Instant startingTime = Instant.from(formatter.parse(startingTimeString));
                Instant endingTime = Instant.from(formatter.parse(endingTimeString));

                if ((startingTime.isAfter(beginningOfDay)) && (endingTime.isBefore(endOfDay))){
                    playtime += rs.getLong("play_time");
                }
                else if (startingTime.isAfter(beginningOfDay)) {
                    playtime += Duration.between(startingTime, endOfDay).toMillis();
                }
                else if (endingTime.isBefore(endOfDay)) {
                    playtime += Duration.between(beginningOfDay, endingTime).toMillis();;
                }
            }
            return playtime;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


        public long getLeftoverPlaytime(String uuid, String time1, String time2) {
        String sql = "SELECT play_time, ending_time FROM time_logger \n" +
                "WHERE ? BETWEEN starting_time AND ending_time \n" +
                "AND uuid = ?";

        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, time1);
            pstmt.setString(2, uuid);
            System.out.println(pstmt);

            ResultSet rs = pstmt.executeQuery();

            // loop through the result set

            if (rs.next()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

                String endingTimeString = rs.getString("ending_time");

                TemporalAccessor temporalAccessor = formatter.parse(endingTimeString);
                Instant endingTime = Instant.from(temporalAccessor);

                TemporalAccessor temporalAccessor2 = formatter.parse(time1);
                Instant time1Instant = Instant.from(temporalAccessor2);

                return Duration.between(time1Instant, endingTime).toMillis();

            }
            else {
                return 0;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
