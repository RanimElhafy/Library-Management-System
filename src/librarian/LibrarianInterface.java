package librarian;

import librarian_assistant.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibrarianInterface {
    private final Stage stage;
    private final String librarianUsername;
    private final Connection connection;

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
            RegisterLibraryMemberUI registerUI = new RegisterLibraryMemberUI(connection, librarianUsername);
            registerUI.display();
        });

        renewMembershipBtn.setOnAction(e -> {
            RenewLibraryMembership renewUI = new RenewLibraryMembership(connection, librarianUsername);
            Stage renewStage = new Stage();
            renewStage.setScene(new Scene(renewUI.layout, 400, 250));
            renewStage.setTitle("Renew Membership");
            renewStage.show();
        });

        borrowBookBtn.setOnAction(e -> {
            BorrowingPlanUI borrowUI = new BorrowingPlanUI(connection, librarianUsername);
            Stage borrowStage = new Stage();
            borrowStage.setScene(new Scene(borrowUI.layout, 400, 250));
            borrowStage.setTitle("Record Borrowing");
            borrowStage.show();
        });

        calculateFineBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Calculate Fine");
            dialog.setHeaderText("Enter Member ID to calculate overdue fines:");
            dialog.setContentText("Member ID:");

            dialog.showAndWait().ifPresent(input -> {
                int memberId = Integer.parseInt(input);
                FineCalculator calculator = new FineCalculator(connection, librarianUsername);
                calculator.calculateFineForMember(memberId);
            });
        });

        updateMemberBtn.setOnAction(e -> {
            UpdateMemberInfoUI updateUI = new UpdateMemberInfoUI(connection, librarianUsername);
            updateUI.display();
        });

        manageBooksBtn.setOnAction(e -> {
            BookInventoryUI bookUI = new BookInventoryUI(connection, librarianUsername);
            bookUI.display();
        });

        scheduleMaintenanceBtn.setOnAction(e -> {
            ScheduleMaintenanceUI maintenanceUI = new ScheduleMaintenanceUI(connection, librarianUsername);
            maintenanceUI.display();
        });

        viewLogsBtn.setOnAction(e -> {
            showSystemLogs(getSystemLogs());
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
