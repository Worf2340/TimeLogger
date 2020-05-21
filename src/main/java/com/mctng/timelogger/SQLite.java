package com.mctng.timelogger;

import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SQLite {

    private String fileName;
    private TimeLogger plugin;

    SQLite(TimeLogger plugin, String fileName) {
        this.fileName = fileName;
        this.plugin = plugin;
    }

    private Connection connect() throws ClassNotFoundException, SQLException {
        // SQLite connection string
        String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/" + fileName;
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(url);
    }

    void createTableIfNotExists() {
        try (Connection c = connect(); Statement pstmt = c.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS time_logger (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " uuid text NOT NULL,\n"
                    + " play_time integer NOT NULL,\n"
                    + " starting_time char NOT NULL,\n"
                    + " ending_time char NOT NULL\n"
                    + ");";
            pstmt.execute(sql);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void insertPlayer(Player player, long playTime, String startingTime, String endingTime) {
        String sql = "INSERT INTO time_logger(uuid,play_time,starting_time,ending_time) VALUES (?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setLong(2, playTime);
            pstmt.setString(3, startingTime);
            pstmt.setString(4, endingTime);
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    long getPlaytime(String uuid) {

        String sql = "SELECT play_time FROM time_logger WHERE uuid = ?";
        long timePlayed = 0;

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);

            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            timePlayed = 0;
            while (rs.next()) {
                timePlayed += rs.getLong("play_time");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return timePlayed;
    }

    long getPlaytimeBetweenTimes(String uuid, String time1, String time2) {

        String sql = "SELECT play_time, starting_time, ending_time FROM time_logger \n" +
                "WHERE ((starting_time BETWEEN ? AND ?) \n" +
                "OR (ending_time BETWEEN  ? AND ?)) \n" +
                "AND uuid = ?";

        long timePlayed = 0;
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, time1);
            pstmt.setString(2, time2);
            pstmt.setString(3, time1);
            pstmt.setString(4, time2);
            pstmt.setString(5, uuid);

            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
            Instant time1Instant = Instant.from(formatter.parse(time1));
            Instant time2Instant = Instant.from(formatter.parse(time2));
            while (rs.next()) {
                // Convert ending and starting time strings to instants for easy time calculations
                Instant resultEndingTime = Instant.from(formatter.parse(rs.getString("ending_time")));
                Instant resultStartingTime = Instant.from(formatter.parse(rs.getString("starting_time")));

                if ((resultStartingTime.isAfter(time1Instant)) && (resultEndingTime.isBefore(time2Instant))) {
                    timePlayed += rs.getLong("play_time");
                } else if (resultStartingTime.isBefore(time1Instant)) {
                    timePlayed += Duration.between(time1Instant, resultEndingTime).toMillis();
                } else if (resultEndingTime.isAfter(time2Instant)) {
                    timePlayed += Duration.between(resultStartingTime, time2Instant).toMillis();
                }
            }
            return timePlayed;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }


    ArrayList<String> getPlayers() {
        String sql = "SELECT DISTINCT uuid FROM time_logger";
        ArrayList<String> playerList = new ArrayList<>();

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            // loop through the result set

            TimeLoggerPlayer currentPlayer;
            String uuid;
            playerList = new ArrayList<>();
            while (rs.next()) {
                uuid = rs.getString("uuid");
                playerList.add(uuid);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return playerList;
    }

}
