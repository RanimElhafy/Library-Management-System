package common;
import java.sql.*;

public class DBUtils {
    private static String url = "jdbc:mysql://127.0.0.1:3306/library_db";
    private static String DBUsername = "libraryAdmin";
    private static String DBPassword = "Lib@dmin123";

    static Connection setUser(String role) {
        String uname = "";
        String pword = "";
        if (role.equals("librarian")) {
            uname = "libUser";
            pword = "LibPass123!";
        } else if (role.equals("admin")) {
            uname = "adminUser";
            pword = "AdminPass456!";
        } else if (role.equals("member")) {
            uname = "memberUser";
            pword = "MemPass789!";
        }
        return establishConnection(uname, pword);
    }

    public static Connection establishConnection(String username, String password) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connection Successful");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return con;
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
