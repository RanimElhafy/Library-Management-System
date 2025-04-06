package common;

import administrator.AdminInterface;
import administrator.User;
import librarian.LibrarianInterface;
import common.DBUtils;
import common.hashing;
import librarian_assistant.LibrarianAssistantInterface;


import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loginButton.setOnAction((ActionEvent event) -> authenticate());

        loginLayout.add(welcomeLabel, 1, 0);
        loginLayout.add(usernameLabel, 0, 1);
        loginLayout.add(usernameField, 1, 1);
        loginLayout.add(passwordLabel, 0, 2);
        loginLayout.add(passwordField, 1, 2);
        loginLayout.add(loginButton, 1, 3);

        loginScene = new Scene(loginLayout, 600, 250);
        stage.setTitle("User Login");
        stage.setScene(loginScene);
        stage.show();
    }

    private void authenticate() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter both username and password.");
            return;
        }

        validateUserLogin(username, password);
    }

    private void validateUserLogin(String username, String enteredPassword) {
        Connection con = DBUtils.establishConnection(); // Uses root
        try {
            String query = "SELECT PasswordHash, Salt, Role FROM users WHERE Username = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                String storedSalt = rs.getString("Salt");
                String role = rs.getString("Role");

                hashing hasher = new hashing(enteredPassword);
                String generatedHash = hasher.generateHashWithSalt(enteredPassword, storedSalt);

                if (storedHash.equals(generatedHash)) {
                    navigateToRoleInterface(role.toLowerCase(), username, con);
                    return;
                } else {
                    showAlert("Login Failed", "Incorrect username or password.");
                }
            } else {
                showAlert("Login Failed", "Incorrect username or password.");
            }

            rs.close();
            stmt.close();
            DBUtils.closeConnection(con, stmt);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while connecting.");
        }
    }

    private void navigateToRoleInterface(String role, String username, Connection con) {
        switch (role) {
            case "admin":
                List<User> userList = loadUserList(con);
                new AdminInterface(stage, username, con, userList).initializeComponents();
                break;
            case "librarian":
                new LibrarianInterface(stage, username, con).initializeComponents();
                break;
            case "assistant":
                new LibrarianAssistantInterface(stage, username, con).initializeComponents();
                break;
            default:
                showAlert("Access Error", "Unrecognized role: " + role);
                DBUtils.closeConnection(con, null);
        }
    }
    private List<User> loadUserList(Connection con) {
        List<User> users = new ArrayList<>();
        String query = "SELECT Username, IsLocked FROM users";
    
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String username = rs.getString("Username");
                boolean isLocked = rs.getBoolean("IsLocked");
    
                User user = new User(username);
                user.setLocked(isLocked);
                users.add(user);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return users;
    }    
    

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
