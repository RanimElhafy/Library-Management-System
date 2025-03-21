package common;
import Librarian.LibInterface;
import Administrator.AdminInterface;
import Member.MemInterface;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.text.*;

public class LoginInterface {
    private Scene loginScene;
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Stage stage;

    public LoginInterface(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void initializeComponents() {
        GridPane loginLayout = new GridPane();
        loginLayout.setHgap(10);
        loginLayout.setVgap(10);
        loginLayout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Library Management System - Login");
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Button loginButton = new Button("Login");

        Font font = Font.font("Arial", FontWeight.BOLD, 14);
        welcomeLabel.setFont(font);

        loginButton.setOnAction((ActionEvent event) -> authenticate());

        loginLayout.add(welcomeLabel, 1, 0);
        loginLayout.add(usernameLabel, 0, 1);
        loginLayout.add(usernameField, 1, 1);
        loginLayout.add(passwordLabel, 0, 2);
        loginLayout.add(passwordField, 1, 2);
        loginLayout.add(loginButton, 1, 3);

        loginScene = new Scene(loginLayout, 600, 200);
        stage.setTitle("User Login");
        stage.setScene(loginScene);
        stage.show();
    }

    private void authenticate() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Connection con = DBUtils.establishConnection();
        String query = "SELECT * FROM users WHERE username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                DBUtils.closeConnection(con, statement);
                con = DBUtils.setUser(role);
                navigateToRoleInterface(role, username, con);
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to connect to the database.");
        }
    }

    private void navigateToRoleInterface(String role, String username, Connection con) {
        if (role.equals("librarian")) {
            LibInterface libInterface = new LibInterface(stage, username, con);
            libInterface.initializeComponents();
        } else if (role.equals("admin")) {
            AdminInterface adminInterface = new AdminInterface(stage, username, con);
            adminInterface.initializeComponents();
        } else if (role.equals("member")) {
            MemInterface memInterface = new MemInterface(stage, username, con);
            memInterface.initializeComponents();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
