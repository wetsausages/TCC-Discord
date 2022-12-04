package cc.tankers.discord.utils.data;

import cc.tankers.discord.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQL {

    private static Connection conn = null;

    public void Load () {
        Connect();
        try {
            CreateTables();
            Logger.log("[+] Tables validated.", 1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Logger.log("[-] Unable to validate tables!.", 1);
        }

        Logger.log("[+] SQL loaded.", 1);
    }

    public void Connect () {
        try {
            String url = "jdbc:sqlite:./data/rb.db";
            conn = DriverManager.getConnection(url);
            Logger.log("[+] Connection to [rb.db] has been established.", 1);
        } catch (SQLException e) {
            Logger.log("[-] Failed to connect to rb.db!\n" + e.getMessage(), 1);
        }
    }

    public void CreateTables () throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = """
                CREATE TABLE IF NOT EXISTS mutes (
                    id integer PRIMARY KEY,
                    user text NOT NULL,
                    reason text NOT NULL,
                    start long NOT NULL,
                    duration long NOT NULL
                );""";
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS reminders (
                    id integer PRIMARY KEY,
                    userId text NOT NULL,
                    content text NOT NULL,
                    start long NOT NULL,
                    duration long NOT NULL
                );""";
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS members (
                    id integer PRIMARY KEY,
                    member text NOT NULL,
                    points integer DEFAULT 0
                );""";
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS bosses (
                    id integer PRIMARY KEY,
                    boss text NOT NULL,
                    embed_id text DEFAULT none
                );""";
        stmt.execute(sql);

        sql = """
                CREATE TABLE IF NOT EXISTS items (
                    id integer PRIMARY KEY,
                    item text NOT NULL,
                    count integer DEFAULT 0,
                    boss text NOT NULL
                );""";
        stmt.execute(sql);
    }

    public static Connection getConnectionObj () {
        return conn;
    }
}
