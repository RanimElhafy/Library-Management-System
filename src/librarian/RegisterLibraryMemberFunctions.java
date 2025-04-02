package librarian;

import common.DBLogger;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterLibraryMemberFunctions {
    private Connection con;
    private String username;

    public RegisterLibraryMemberFunctions(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public boolean validateInputs(String name, String contact, String type) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        boolean isValid = !name.isBlank() && !contact.isBlank() && type != null && pattern.matcher(contact).matches();
        DBLogger.log("INFO", "RegisterLibraryMemberFunctions", "Input validation result: " + isValid, username);
        return isValid;
    }

    public int fetchNextMemberID() throws SQLException {
        String query = "SELECT MAX(MemberID) AS MaxID FROM members";
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int nextID = rs.getInt("MaxID") + 1;
                DBLogger.log("INFO", "RegisterLibraryMemberFunctions", "Fetched next MemberID: " + nextID, username);
                return nextID;
            }
        } catch (SQLException e) {
            DBLogger.log("ERROR", "RegisterLibraryMemberFunctions", "Failed to fetch next MemberID.", username);
            e.printStackTrace();
        }
        return -1;
    }

    public String getMembershipType(int memberID) throws SQLException {
        String membershipType = "";
        try {
            String query = "SELECT MembershipType FROM members WHERE MemberID = ?";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, memberID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                membershipType = rs.getString("MembershipType");
                DBLogger.log("INFO", "RegisterLibraryMemberFunctions", "Fetched MembershipType: " + membershipType, username);
            }
        } catch (SQLException e) {
            DBLogger.log("ERROR", "RegisterLibraryMemberFunctions", "Failed to fetch MembershipType.", username);
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
