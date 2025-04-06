package librarian_assistant;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Allows librarian assistants to renew memberships
 * Cannot modify other member data or create new members
 */
public class MembershipRenewalAssistant {
    private Connection con;
    private String username;

    public MembershipRenewalAssistant(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Membership Renewal");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setHgap(10);
        layout.setVgap(10);

        // Member selection
        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>();
        memberIdSpinner.setEditable(true);
        memberIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, getMaxMemberID()));

        // Display current member info
        Label currentInfoLabel = new Label("Current Info:");
        TextArea currentInfoArea = new TextArea();
        currentInfoArea.setEditable(false);
        currentInfoArea.setPrefRowCount(4);

        // Load button
        Button loadButton = new Button("Load Member Info");
        loadButton.setOnAction(e -> {
            int memberId = memberIdSpinner.getValue();
            String memberInfo = getMemberInfo(memberId);
            currentInfoArea.setText(memberInfo);
        });

        // Membership type
        Label typeLabel = new Label("New Membership Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Regular", "Premium", "Student");

        // Duration selection
        Label durationLabel = new Label("Renewal Duration:");
        ComboBox<String> durationComboBox = new ComboBox<>();
        durationComboBox.getItems().addAll("6 Months", "1 Year", "2 Years");
        durationComboBox.setValue("6 Months");

        // Renew button
        Button renewButton = new Button("Renew Membership");
        renewButton.setOnAction(e -> {
            try {
                int memberId = memberIdSpinner.getValue();
                String type = typeComboBox.getValue();
                String duration = durationComboBox.getValue();
                
                if (currentInfoArea.getText().isEmpty()) {
                    showAlert("Error", "Please load member information first");
                    return;
                }
                
                if (type == null || type.isEmpty()) {
                    showAlert("Input Error", "Please select a membership type");
                    return;
                }
                
                renewMembership(memberId, type, duration);
                
                // Clear selections
                typeComboBox.getSelectionModel().clearSelection();
                currentInfoArea.clear();
                
            } catch (Exception ex) {
                showAlert("Error", "Failed to renew membership: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Add components to layout
        layout.add(memberIdLabel, 0, 0);
        layout.add(memberIdSpinner, 1, 0);
        layout.add(loadButton, 2, 0);
        
        layout.add(currentInfoLabel, 0, 1);
        layout.add(currentInfoArea, 1, 1, 2, 1);
        
        layout.add(typeLabel, 0, 2);
        layout.add(typeComboBox, 1, 2, 2, 1);
        
        layout.add(durationLabel, 0, 3);
        layout.add(durationComboBox, 1, 3, 2, 1);
        
        layout.add(renewButton, 1, 4);

        // Create scene
        Scene scene = new Scene(layout, 500, 350);
        stage.setScene(scene);
        stage.show();
    }

    private String getMemberInfo(int memberId) {
        StringBuilder info = new StringBuilder();
        
        try {
            String query = "SELECT Name, ContactInfo, MembershipType, MembershipExpiry FROM members WHERE MemberID = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                info.append("Name: ").append(rs.getString("Name")).append("\n");
                info.append("Contact: ").append(rs.getString("ContactInfo")).append("\n");
                info.append("Current Type: ").append(rs.getString("MembershipType")).append("\n");
                info.append("Expires: ").append(rs.getString("MembershipExpiry"));
                
                DBLogger.log("INFO", "MembershipRenewalAssistant", "Retrieved info for MemberID: " + memberId, username);
            } else {
                info.append("Member not found");
                DBLogger.log("WARN", "MembershipRenewalAssistant", "Member not found for ID: " + memberId, username);
            }
            
        } catch (SQLException e) {
            info.append("Error loading member information");
            DBLogger.log("ERROR", "MembershipRenewalAssistant", "Error loading member data: " + e.getMessage(), username);
            e.printStackTrace();
        }
        
        return info.toString();
    }

    private void renewMembership(int memberId, String type, String duration) throws SQLException {
        try {
            // Calculate new expiry date based on selected duration
            LocalDate newExpiry;
            LocalDate today = LocalDate.now();
            
            switch(duration) {
                case "1 Year":
                    newExpiry = today.plusYears(1);
                    break;
                case "2 Years":
                    newExpiry = today.plusYears(2);
                    break;
                default: // 6 Months
                    newExpiry = today.plusMonths(6);
            }
            
            String expiryStr = newExpiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // Update the membership
            String updateQuery = "UPDATE members SET MembershipType = ?, MembershipExpiry = ? WHERE MemberID = ?";
            PreparedStatement stmt = con.prepareStatement(updateQuery);
            stmt.setString(1, type);
            stmt.setString(2, expiryStr);
            stmt.setInt(3, memberId);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                DBLogger.log("INFO", "MembershipRenewalAssistant", "Membership renewed for MemberID: " + memberId, username);
                showInfo("Success", "Membership renewed successfully.\nNew expiry date: " + expiryStr);
            } else {
                showAlert("Error", "Member not found or update failed");
            }
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "MembershipRenewalAssistant", "Failed to renew membership: " + e.getMessage(), username);
            throw e;
        }
    }

    private int getMaxMemberID() {
        try {
            String query = "SELECT MAX(MemberID) as MaxID FROM members";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("MaxID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 100; // Default fallback
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}