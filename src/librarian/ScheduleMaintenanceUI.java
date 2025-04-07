package librarian;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class ScheduleMaintenanceUI {
    private final Connection con;
    private final String username;

    public ScheduleMaintenanceUI(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Schedule Maintenance");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setHgap(10);
        layout.setVgap(10);

        TextField facilityIdField = new TextField();
        TextField descriptionField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button scheduleBtn = new Button("Schedule");

        layout.add(new Label("Facility ID:"), 0, 0);
        layout.add(facilityIdField, 1, 0);
        layout.add(new Label("Description:"), 0, 1);
        layout.add(descriptionField, 1, 1);
        layout.add(new Label("Scheduled Date:"), 0, 2);
        layout.add(datePicker, 1, 2);
        layout.add(scheduleBtn, 1, 3);

        scheduleBtn.setOnAction(e -> {
            try {
                int facilityId = Integer.parseInt(facilityIdField.getText());
                String desc = descriptionField.getText().trim();
                String date = datePicker.getValue().toString();

                if (desc.isEmpty()) {
                    showAlert("Input Error", "Description is required.");
                    return;
                }

                String query = "INSERT INTO maintenancerecords (FacilityID, Description, MaintenanceDate) VALUES (?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, facilityId);
                stmt.setString(2, desc);
                stmt.setString(3, date);
                stmt.executeUpdate();

                DBLogger.log("INFO", "ScheduleMaintenanceUI", "Maintenance scheduled for FacilityID " + facilityId, username);
                showInfo("Scheduled", "Maintenance scheduled successfully.");
            } catch (Exception ex) {
                showAlert("Error", "Failed to schedule maintenance.");
                ex.printStackTrace();
            }
        });

        stage.setScene(new Scene(layout, 450, 250));
        stage.show();
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
