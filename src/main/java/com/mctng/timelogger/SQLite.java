package com.mctng.timelogger;

import java.sql.*;
import java.util.ArrayList;

@SuppressWarnings("Duplicates")
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


    void createTimeLoggerTableIfNotExists() throws SQLException, ClassNotFoundException {
        try (Connection c = connect(); Statement pstmt = c.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS time_logger (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " uuid text NOT NULL,\n"
                    + " play_time integer NOT NULL,\n"
                    + " starting_time char NOT NULL,\n"
                    + " ending_time char NOT NULL\n"
                    + ");";
            pstmt.execute(sql);
        }
    }

    void createAutoSaveTableIfNotExists() throws SQLException, ClassNotFoundException {
        try (Connection c = connect(); Statement pstmt = c.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS auto_save (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " uuid text NOT NULL,\n"
                    + " play_time integer NOT NULL,\n"
                    + " starting_time char NOT NULL,\n"
                    + " ending_time char NOT NULL\n"
                    + ");";
            pstmt.execute(sql);
        }
    }

    public void insertPlayerTimeLogger(String uuid, long playTime, String startingTime, String endingTime) {
        String sql = "INSERT INTO time_logger (uuid,play_time,starting_time,ending_time) VALUES (?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setLong(2, playTime);
            pstmt.setString(3, startingTime);
            pstmt.setString(4, endingTime);
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void insertPlayerAutoSave(String uuid, long playTime, String startingTime, String endingTime) {
        String sql = "INSERT INTO auto_save (uuid,play_time,starting_time,ending_time) VALUES (?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setLong(2, playTime);
            pstmt.setString(3, startingTime);
            pstmt.setString(4, endingTime);
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    ArrayList<TimeLoggerRecord> getPlaytimeRecords(String uuidString) {
        String sql = "SELECT * FROM time_logger WHERE uuid = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ArrayList<TimeLoggerRecord> records = new ArrayList<>();
            pstmt.setString(1, uuidString);
            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                records.add(new TimeLoggerRecord(
                        rs.getString("uuid"),
                        rs.getLong("play_time"),
                        rs.getString("starting_time"),
                        rs.getString("ending_time")));
            }

            return records;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<TimeLoggerRecord> getPlaytimeRecords(String queryStartingTime, String queryEndingTime) {
        String sql = "SELECT * FROM time_logger \n" +
                "WHERE (((? BETWEEN starting_time AND ending_time) \n" +
                "OR (? BETWEEN starting_time AND ending_time)) \n" +
                "OR (((ending_time BETWEEN  ? AND ?)) \n" +
                "OR ((starting_time BETWEEN ? AND ?)))) \n";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, queryStartingTime);
            pstmt.setString(2, queryEndingTime);
            pstmt.setString(3, queryStartingTime);
            pstmt.setString(4, queryEndingTime);
            pstmt.setString(5, queryStartingTime);
            pstmt.setString(6, queryEndingTime);

            ResultSet rs = pstmt.executeQuery();

            ArrayList<TimeLoggerRecord> records = new ArrayList<>();

            while (rs.next()) {
                records.add(new TimeLoggerRecord(
                        rs.getString("uuid"),
                        rs.getLong("play_time"),
                        rs.getString("starting_time"),
                        rs.getString("ending_time")));
            }

            return records;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<TimeLoggerRecord> getPlaytimeRecords(String uuidString, String queryStartingTime, String queryEndingTime) {
        String sql = "SELECT * FROM time_logger \n" +
                "WHERE (((? BETWEEN starting_time AND ending_time) \n" +
                "OR (? BETWEEN starting_time AND ending_time)) \n" +
                "OR (((ending_time BETWEEN  ? AND ?)) \n" +
                "OR ((starting_time BETWEEN ? AND ?)))) \n" +
                "AND uuid = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, queryStartingTime);
            pstmt.setString(2, queryEndingTime);
            pstmt.setString(3, queryStartingTime);
            pstmt.setString(4, queryEndingTime);
            pstmt.setString(5, queryStartingTime);
            pstmt.setString(6, queryEndingTime);
            pstmt.setString(7, uuidString);

            ResultSet rs = pstmt.executeQuery();

            ArrayList<TimeLoggerRecord> records = new ArrayList<>();

            while (rs.next()) {
                records.add(new TimeLoggerRecord(
                        rs.getString("uuid"),
                        rs.getLong("play_time"),
                        rs.getString("starting_time"),
                        rs.getString("ending_time")));
            }

            return records;

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void deletePlayerFromAutoSave(String uuid) {
        String sql = "DELETE FROM auto_save WHERE uuid = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void moveFromAutoSaveToTimeLogger() throws SQLException, ClassNotFoundException {
        ArrayList<TimeLoggerSavedPlayer> savedPlayers = new ArrayList<>();

        try (Connection c = connect(); Statement pstmt = c.createStatement()) {
            String sql = "SELECT * FROM auto_save";
            ResultSet rs = pstmt.executeQuery(sql);

            while (rs.next()) {
                savedPlayers.add(new TimeLoggerSavedPlayer(
                        rs.getString("uuid"),
                        rs.getLong("play_time"),
                        rs.getString("starting_time"),
                        rs.getString("ending_time")));
            }

        }

        for (TimeLoggerSavedPlayer player : savedPlayers) {
            insertPlayerTimeLogger(player.getUuidString(), player.getPlayTime(), player.getStartingTime(), player.getEndingTime());
            deletePlayerFromAutoSave(player.getUuidString());
        }
    }

    private class TimeLoggerSavedPlayer {

        private final String uuidString;
        private final long playTime;
        private final String startingTime;
        private final String endingTime;

        TimeLoggerSavedPlayer(String uuid, long playTime, String startingTime, String endingTime) {
            this.uuidString = uuid;
            this.playTime = playTime;
            this.startingTime = startingTime;
            this.endingTime = endingTime;
        }

        String getUuidString() {
            return uuidString;
        }

        long getPlayTime() {
            return playTime;
        }

        String getStartingTime() {
            return startingTime;
        }

        String getEndingTime() {
            return endingTime;
        }
    }


}


