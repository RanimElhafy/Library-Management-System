package common;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 public class DBLogger {
     public static void log(String level, String logger, String message, String username) {
         String sql = "INSERT INTO logging (event_date, level, logger, message, username) VALUES (NOW(), ?, ?, ?, ?)";
         try (Connection con = DBUtils.setUser("admin"); PreparedStatement stmt = con.prepareStatement(sql)) {
             stmt.setString(1, level);
             stmt.setString(2, logger);
             stmt.setString(3, message);
             if (username != null) {
                 stmt.setString(4, username);
             } else {
                 stmt.setNull(4, java.sql.Types.VARCHAR);
             }
             stmt.executeUpdate();
             DBUtils.closeConnection(con, null);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 }