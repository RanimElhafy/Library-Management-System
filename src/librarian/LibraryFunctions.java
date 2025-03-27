package librarian;

import common.DBLogger;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibraryFunctions {
    private Connection con;
    private String username;

    public LibraryFunctions(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public String getMembershipType(int memberID) throws SQLException {
        String membershipType = "";
        try {
            String query = "SELECT MembershipType FROM library_members WHERE MemberID = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, memberID);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                DBLogger.log("INFO", "LibraryFunctions", "Membership type fetched.", username);
                membershipType = rs.getString("MembershipType");
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            DBLogger.log("ERROR", "LibraryFunctions", "Error fetching membership type.", username);
            e.printStackTrace();
        }
        return membershipType;
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
