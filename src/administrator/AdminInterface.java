package administrator;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;

public class AdminInterface {
    private Stage stage;
    private String adminUsername;
    private Connection connection;

    public AdminInterface(Stage stage, String adminUsername, Connection connection) {
        this.stage = stage;
        this.adminUsername = adminUsername;
        this.connection = connection;
    }

    public void initializeComponents() {
        Label welcomeLabel = new Label("Welcome, Admin: " + adminUsername);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button manageUsersBtn = new Button("Manage Users");
        Button viewLogsBtn = new Button("View System Logs");
        Button enforcePolicyBtn = new Button("Enforce Security Policy");
        Button logoutBtn = new Button("Logout");

        manageUsersBtn.setOnAction(e -> showInfo("Manage Users"));
        viewLogsBtn.setOnAction(e -> showInfo("Viewing logs..."));
        enforcePolicyBtn.setOnAction(e -> showInfo("Enforcing policies..."));
        logoutBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(15, welcomeLabel, manageUsersBtn, viewLogsBtn, enforcePolicyBtn, logoutBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
