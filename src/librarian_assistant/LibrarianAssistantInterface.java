package librarian_assistant;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;

/**
 * Main interface for the Librarian Assistant system
 * Provides limited features compared to full librarian access
 * Can only: renew membership, assign books, view fines, view member info, and monitor facilities
 */
public class LibrarianAssistantInterface {
    private Stage stage;
    private String assistantUsername;
    private Connection connection;

    public LibrarianAssistantInterface(Stage stage, String assistantUsername, Connection connection) {
        this.stage = stage;
        this.assistantUsername = assistantUsername;
        this.connection = connection;
    }

    public void initializeComponents() {
        Label welcomeLabel = new Label("Librarian Assistant Dashboard - " + assistantUsername);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button renewMembershipBtn = new Button("Renew Membership");
        Button assignBookBtn = new Button("Book Borrowing");
        Button viewFinesBtn = new Button("View Fines");
        Button viewMemberInfoBtn = new Button("View Member Information");
        Button facilityMonitorBtn = new Button("Facility Monitoring");
        Button exitBtn = new Button("Logout");

        // Set button actions
        renewMembershipBtn.setOnAction(e -> {
            try {
                MembershipRenewalAssistant renewal = new MembershipRenewalAssistant(connection, assistantUsername);
                renewal.display();
                DBLogger.log("INFO", "LibrarianAssistantInterface", "Accessed Membership Renewal", assistantUsername);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Membership Renewal: " + ex.getMessage());
            }
        });

        assignBookBtn.setOnAction(e -> {
            try {
                BookBorrowingAssistant borrowing = new BookBorrowingAssistant(connection, assistantUsername);
                borrowing.display();
                DBLogger.log("INFO", "LibrarianAssistantInterface", "Accessed Book Borrowing", assistantUsername);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Book Borrowing: " + ex.getMessage());
            }
        });

        viewFinesBtn.setOnAction(e -> {
            try {
                FineViewAssistant fineView = new FineViewAssistant(connection, assistantUsername);
                fineView.display();
                DBLogger.log("INFO", "LibrarianAssistantInterface", "Accessed Fine Viewer", assistantUsername);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Fine Viewer: " + ex.getMessage());
            }
        });

        viewMemberInfoBtn.setOnAction(e -> {
            try {
                MemberInfoViewer memberInfo = new MemberInfoViewer(connection, assistantUsername);
                memberInfo.display();
                DBLogger.log("INFO", "LibrarianAssistantInterface", "Accessed Member Info Viewer", assistantUsername);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Member Info Viewer: " + ex.getMessage());
            }
        });

        facilityMonitorBtn.setOnAction(e -> {
            try {
                FacilityMonitor facilityMonitor = new FacilityMonitor(connection, assistantUsername);
                facilityMonitor.display();
                DBLogger.log("INFO", "LibrarianAssistantInterface", "Accessed Facility Monitor", assistantUsername);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Error loading Facility Monitor: " + ex.getMessage());
            }
        });

        exitBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
            welcomeLabel,
            renewMembershipBtn,
            assignBookBtn,
            viewFinesBtn,
            viewMemberInfoBtn,
            facilityMonitorBtn,
            exitBtn
        );

        Scene scene = new Scene(layout, 420, 500);
        stage.setTitle("Librarian Assistant Dashboard");
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
}