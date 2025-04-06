package librarian;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UpdateMemberInfoUI {
    private Connection con;
    private String username;

    public UpdateMemberInfoUI(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Update Member Info");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setVgap(10);
        layout.setHgap(10);

        Label idLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>(1, getMaxMemberID(), 1);

        Label nameLabel = new Label("New Name:");
        TextField nameField = new TextField();

        Label contactLabel = new Label("New Email:");
        TextField contactField = new TextField();

        Label typeLabel = new Label("New Membership Type:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Regular", "Premium", "Student");

        Button updateBtn = new Button("Update Info");
        updateBtn.setOnAction(e -> {
            try {
                int memberId = memberIdSpinner.getValue();
                String newName = nameField.getText().trim();
                String newContact = contactField.getText().trim();
                String newType = typeCombo.getValue();

                if (newName.isEmpty() || newContact.isEmpty() || newType == null) {
                    showAlert("Validation Error", "All fields are required.");
                    return;
                }

                String query = "UPDATE members SET Name = ?, ContactInfo = ?, MembershipType = ? WHERE MemberID = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, newName);
                stmt.setString(2, newContact);
                stmt.setString(3, newType);
                stmt.setInt(4, memberId);
                stmt.executeUpdate();

                DBLogger.log("INFO", "UpdateMemberInfoUI", "Updated member info for ID: " + memberId, username);
                showInfo("Update Successful", "Member information has been updated.");
            } catch (Exception ex) {
                showAlert("Error", "Failed to update member info.");
                ex.printStackTrace();
            }
        });

        layout.add(idLabel, 0, 0);
        layout.add(memberIdSpinner, 1, 0);
        layout.add(nameLabel, 0, 1);
        layout.add(nameField, 1, 1);
        layout.add(contactLabel, 0, 2);
        layout.add(contactField, 1, 2);
        layout.add(typeLabel, 0, 3);
        layout.add(typeCombo, 1, 3);
        layout.add(updateBtn, 1, 4);

        stage.setScene(new Scene(layout, 400, 300));
        stage.show();
    }

    private int getMaxMemberID() {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT MAX(MemberID) AS MaxID FROM members");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("MaxID");
        } catch (Exception ignored) {}
        return 1;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
