package common;
import java.sql.*;

public class DBUtils {
    private static String url = "jdbc:mysql://127.0.0.1:3306/librarymanagement";
    private static String DBUsername = "aisha";
    private static String DBPassword = "A1sh@123";

    public static Connection establishConnection(String username, String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connection Successful");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return con;
    }

    public static Connection establishConnection() {
        return establishConnection("root", "");
    }

    public static void closeConnection(Connection con, Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            con.close();
            System.out.println("Connection is closed");
        } catch (SQLException e) {
            e.getMessage();
        }
    }
}
