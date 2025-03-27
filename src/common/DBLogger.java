package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DBLogger {

    private static Connection connection;

    //inject connection externally
    public static void setConnection(Connection con) {
        connection = con;
    }


    public static void log(String level, String source, String message, String username, Integer recordID) {
        if (connection == null) {
            System.err.println("[DBLogger] Database connection not set. Logging skipped.");
            return;
        }

        String fullMessage = String.format("[%s] [%s] %s (User: %s)", level, source, message, username);

        try {
            String query = "INSERT INTO logs (Action, Timestamp, RecordID) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, fullMessage);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            if (recordID != null) {
                stmt.setInt(3, recordID);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DBLogger] Failed to insert log into DB: " + e.getMessage());
        }
    }

    /**
     * Overloaded logger when no `recordID` is available (standard usage).
     */
    public static void log(String level, String source, String message, String username) {
        log(level, source, message, username, null);
    }
}
