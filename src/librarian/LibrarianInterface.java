package librarian;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibrarianInterface {
    private Stage stage;
    private String librarianUsername;
    private Connection connection;

    public LibrarianInterface(Stage stage, String librarianUsername, Connection connection) {
        this.stage = stage;
        this.librarianUsername = librarianUsername;
        this.connection = connection;
    }

    public void initializeComponents() {
        Label welcomeLabel = new Label("Welcome, Librarian: " + librarianUsername);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button registerMemberBtn = new Button("Register Member");
        Button renewMembershipBtn = new Button("Renew Membership");
        Button borrowBookBtn = new Button("Record Borrowing");
        Button calculateFineBtn = new Button("Calculate Fine");
        Button updateMemberBtn = new Button("Update Member Info");
        Button manageBooksBtn = new Button("Book Inventory");
        Button scheduleMaintenanceBtn = new Button("Schedule Maintenance");
        Button viewLogsBtn = new Button("View Logs");
        Button logoutBtn = new Button("Logout");

        registerMemberBtn.setOnAction(e -> {
            try {
                RegisterLibraryMemberUI registerUI = new RegisterLibraryMemberUI(connection, librarianUsername);
                registerUI.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Register Member interface: " + ex.getMessage());
            }
        });

        renewMembershipBtn.setOnAction(e -> {
            try {
                Stage renewStage = new Stage();
                RenewLibraryMembership renewUI = new RenewLibraryMembership(connection, librarianUsername);
                renewStage.setScene(new Scene(renewUI.layout, 400, 250));
                renewStage.setTitle("Renew Membership");
                renewStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Renew Membership interface: " + ex.getMessage());
            }
        });

        borrowBookBtn.setOnAction(e -> {
            try {
                Stage borrowStage = new Stage();
                BorrowingPlanUI borrowUI = new BorrowingPlanUI(connection, librarianUsername);
                borrowStage.setScene(new Scene(borrowUI.layout, 400, 250));
                borrowStage.setTitle("Record Borrowing");
                borrowStage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Record Borrowing interface: " + ex.getMessage());
            }
        });

        calculateFineBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Calculate Fine");
            dialog.setHeaderText("Enter Member ID to calculate overdue fines:");
            dialog.setContentText("Member ID:");

            dialog.showAndWait().ifPresent(input -> {
                try {
                    int memberId = Integer.parseInt(input);
                    FineCalculator calculator = new FineCalculator(connection, librarianUsername);
                    calculator.calculateFineForMember(memberId);
                } catch (NumberFormatException ex) {
                    showAlert("Input Error", "Please enter a valid numeric Member ID.");
                }
            });
        });

        updateMemberBtn.setOnAction(e -> {
            try {
                UpdateMemberInfoUI updateUI = new UpdateMemberInfoUI(connection, librarianUsername);
                updateUI.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Update Member Info interface.");
            }
        });

        manageBooksBtn.setOnAction(e -> {
            try {
                BookInventoryUI bookUI = new BookInventoryUI(connection, librarianUsername);
                bookUI.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Book Inventory interface.");
            }
        });

        scheduleMaintenanceBtn.setOnAction(e -> {
            try {
                ScheduleMaintenanceUI maintenanceUI = new ScheduleMaintenanceUI(connection, librarianUsername);
                maintenanceUI.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Schedule Maintenance interface.");
            }
        });

        viewLogsBtn.setOnAction(e -> {
            try {
                showSystemLogs(getSystemLogs());
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading logs: " + ex.getMessage());
            }
        });

        logoutBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
            welcomeLabel,
            registerMemberBtn,
            renewMembershipBtn,
            borrowBookBtn,
            calculateFineBtn,
            updateMemberBtn,
            manageBooksBtn,
            scheduleMaintenanceBtn,
            viewLogsBtn,
            logoutBtn
        );

        Scene scene = new Scene(layout, 420, 600);
        stage.setTitle("Librarian Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Display system logs clearly
    private void showSystemLogs(List<String> logs) {
        Stage logStage = new Stage();
        logStage.setTitle("System Logs");

        TextArea logArea = new TextArea(String.join("\n", logs));
        logArea.setEditable(false);
        logArea.setWrapText(true);

        VBox logLayout = new VBox(10, new Label("System Logs:"), logArea);
        logLayout.setPadding(new Insets(15));

        Scene logScene = new Scene(logLayout, 400, 300);
        logStage.setScene(logScene);
        logStage.show();
    }

    // Fetch system logs securely
    private List<String> getSystemLogs() {
        List<String> logs = new ArrayList<>();
        String query = "SELECT Timestamp, Action FROM logs ORDER BY Timestamp DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(rs.getTimestamp("Timestamp").toString() + " - " + rs.getString("Action"));
            }
        } catch (SQLException e) {
            logs.add("Error retrieving logs: " + e.getMessage());
        }
        return logs;
    }
}
