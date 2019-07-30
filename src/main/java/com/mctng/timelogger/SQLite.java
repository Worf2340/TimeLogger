package com.mctng.timelogger;

import org.bukkit.entity.Player;

import java.sql.*;

public class SQLite {

    String fileName;
    TimeLogger plugin;

    public SQLite (TimeLogger plugin, String fileName){
        this.fileName = fileName;
        this.plugin = plugin;
    }

    private Connection connect(){
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

    public void createNewTable(){

        // Create new table
        String sql = "CREATE TABLE IF NOT EXISTS time_logger (\n"
                + " id integer PRIMARY KEY,\n"
                + " uuid text NOT NULL,\n"
                + " username text NOT NULL,\n"
                + " starting_time integer NOT NULL\n"
                + ");";


        try{
            Connection conn = connect();
            Statement statement = conn.createStatement();
            statement.execute(sql);
            this.plugin.getLogger().info("Connected to SQLite database.");
            this.plugin.getLogger().info("Initialized SQLite table.");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean doesPlayerExist(Player player){
        String sql = "SELECT uuid "
                + "FROM time_logger WHERE uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            // set the value
            pstmt.setString(1,player.getUniqueId().toString());
            //
            ResultSet rs  = pstmt.executeQuery();

            // loop through the result set
            String s;
            if(rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void insertPlayer(Player player, int starting_time){
        String sql = "INSERT INTO time_logger(uuid,username,starting_time) VALUES (?,?,?)";
        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName().toUpperCase());
            pstmt.setInt(3,starting_time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addHourColumns() {
        try{
            Connection conn = connect();
            String columnName;
            for (int i = 1; i < 169; i++){
                columnName = "hour_" + i;
                String sql = "ALTER TABLE time_logger\n" +
                        "  ADD " + columnName + " int NOT_NULL;";
                Statement statement = conn.createStatement();
                statement.execute(sql);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
