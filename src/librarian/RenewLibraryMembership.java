package librarian;

import common.DBLogger;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class RenewLibraryMembership {
    public GridPane layout;
    private Connection con;
    private String username;

    public RenewLibraryMembership(Connection con, String username) {
        layout = new GridPane();
        this.con = con;
        this.username = username;
        addComponents();
    }

    private void addComponents() {
        layout.setVgap(10);
        layout.setHgap(10);

        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdField = new Spinner<>();
        memberIdField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, getMaxMemberID()));
        memberIdField.setEditable(true);

        Label typeLabel = new Label("New Membership Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Regular", "Premium");

        Button renewButton = new Button("Renew Membership");

        renewButton.setOnAction(e -> {
            try {
                renewMembership(memberIdField.getValue(), typeComboBox.getValue());
            } catch (SQLException ex) {
                DBLogger.log("ERROR", "RenewLibraryMembership", "Membership renewal failed (SQL)", username);
                ex.printStackTrace();
            }
        });

        layout.add(memberIdLabel, 0, 0);
        layout.add(memberIdField, 1, 0);
        layout.add(typeLabel, 0, 1);
        layout.add(typeComboBox, 1, 1);
        layout.add(renewButton, 0, 2);
    }

    private void renewMembership(int memberId, String type) throws SQLException {
        if (type == null || type.isEmpty() || !validateMemberExists(memberId)) {
            showAlert("Invalid Input", "Please provide a valid Member ID and select a membership type.");
            return;
        }

        LocalDate newExpiry;
        switch (type) {
            case "Regular": newExpiry = LocalDate.now().plusMonths(6); break;
            case "Premium": newExpiry = LocalDate.now().plusYears(1); break;
            default: showAlert("Invalid Type", "Unknown membership type."); return;
        }

        AfterRegistration after = new AfterRegistration(con, "", "", "", newExpiry, memberId, -1, 0, type, username);
        after.renewMembership();

        DBLogger.log("INFO", "RenewLibraryMembership", "Membership renewed for MemberID: " + memberId, username);
    }

    private boolean validateMemberExists(int memberId) {
        try {
            String query = "SELECT MemberID FROM members WHERE MemberID = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            DBLogger.log("ERROR", "RenewLibraryMembership", "Failed to validate MemberID.", username);
            e.printStackTrace();
            return false;
        }
    }

    private int getMaxMemberID() {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT MAX(MemberID) AS MaxID FROM members");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("MaxID");
        } catch (SQLException e) {
            DBLogger.log("ERROR", "RenewLibraryMembership", "Failed to fetch max MemberID.", username);
            e.printStackTrace();
        }
        return 1;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
