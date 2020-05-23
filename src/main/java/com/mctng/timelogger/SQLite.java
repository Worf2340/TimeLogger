package com.mctng.timelogger;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.mctng.timelogger.utils.DateTimeUtil.isInstantAfterOrEquals;
import static com.mctng.timelogger.utils.DateTimeUtil.isInstantBeforeOrEquals;

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

    void createTableIfNotExistsTimeLogger() throws SQLException, ClassNotFoundException {
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

    void createTableIfNotExistsAutoSave() throws SQLException, ClassNotFoundException {
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

    public void insertPlayerAutoSave(String uuid, long playTime, String startingTime, String endingTime) {
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

    // TODO: Fix method

    long getPlaytimeBetweenTimes(String uuid, String queryStartingTime, String queryEndingTime) {
        String sql = "SELECT play_time, starting_time, ending_time FROM time_logger \n" +
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
            pstmt.setString(7, uuid);

            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            long playTime = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
            Instant queryStartingInstant = Instant.from(formatter.parse(queryStartingTime));
            Instant queryEndingInstant = Instant.from(formatter.parse(queryEndingTime));
            while (rs.next()) {
                // Convert ending and starting time strings to instants for easy time calculations
                Instant resultEndingInstant = Instant.from(formatter.parse(rs.getString("ending_time")));
                Instant resultStartingInstant = Instant.from(formatter.parse(rs.getString("starting_time")));


                if (isInstantAfterOrEquals(resultStartingInstant, queryStartingInstant)
                        && isInstantBeforeOrEquals(resultEndingInstant, queryEndingInstant)) {
                    playTime += Duration.between(resultStartingInstant, resultEndingInstant).toMillis();
                } else if (isInstantAfterOrEquals(resultStartingInstant, queryStartingInstant)
                        && isInstantAfterOrEquals(resultEndingInstant, queryEndingInstant)) {
                    playTime += Duration.between(resultStartingInstant, queryEndingInstant).toMillis();
                } else if (isInstantBeforeOrEquals(resultStartingInstant, queryStartingInstant)
                        && isInstantBeforeOrEquals(resultEndingInstant, queryEndingInstant)) {
                    playTime += Duration.between(queryStartingInstant, resultEndingInstant).toMillis();
                } else if (isInstantBeforeOrEquals(resultStartingInstant, queryStartingInstant)
                        && isInstantAfterOrEquals(resultEndingInstant, queryEndingInstant)) {
                    playTime += Duration.between(queryStartingInstant, queryEndingInstant).toMillis();
                }
            }
            return playTime;

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
                String uuid = rs.getString("uuid");
                String startingTime = rs.getString("starting_time");
                String endingTime = rs.getString("ending_time");
                long playTime = rs.getLong("play_time");

                savedPlayers.add(new TimeLoggerSavedPlayer(uuid, playTime, startingTime, endingTime));
            }

        }

        for (TimeLoggerSavedPlayer player : savedPlayers) {
            insertPlayerTimeLogger(player.getUuid(), player.getPlayTime(), player.getStartingTime(), player.getEndingTime());
            deletePlayerFromAutoSave(player.getUuid());
        }
    }

    // TEST METHOD
    void clearDB() {
        try (Connection c = connect(); Statement pstmt = c.createStatement()) {
            String sql = "DELETE FROM time_logger";
            pstmt.execute(sql);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
