package administrator;

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

import common.hashing;


public class AdminInterface {
    private Stage stage;
    private String adminUsername;
    private Connection connection;
    private List<User> users;


    public AdminInterface(Stage stage, String adminUsername, Connection connection, List<User> users) {
        this.stage = stage;
        this.adminUsername = adminUsername;
        this.connection = connection;
        this.users = users;
    }
            
    private List<User> getUsers() {
        return users;
    }
    
    public void initializeComponents() {
        Label welcomeLabel = new Label("Welcome, Admin: " + adminUsername);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button manageUsersBtn = new Button("Manage Users");
        Button viewLogsBtn = new Button("View System Logs");
        Button enforcePolicyBtn = new Button("Enforce Security Policy");
        Button logoutBtn = new Button("Logout");

        manageUsersBtn.setOnAction(e -> showManageUsers(getUsers()));
        viewLogsBtn.setOnAction(e -> showSystemLogs(getSystemLogs()));
        enforcePolicyBtn.setOnAction(e -> showSecurityPolicy(new SecurityPolicy("Minimum password length is 12")));
        logoutBtn.setOnAction(e -> stage.close());
        logoutBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(15, welcomeLabel, manageUsersBtn, viewLogsBtn, enforcePolicyBtn, logoutBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    

    private void showSystemLogs(List<String> logs) {
        Stage logStage = new Stage();
        logStage.setTitle("System Logs");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setText(String.join("\n", logs));

        VBox layout = new VBox(10, new Label("System Logs:"), logArea);
        layout.setPadding(new Insets(15));
        Scene scene = new Scene(layout, 400, 300);
        logStage.setScene(scene);
        logStage.show();
    }
    private void showManageUsers(List<User> users) {
        Stage userStage = new Stage();
        userStage.setTitle("Manage Users");
    
        // Display area
        ListView<User> userList = new ListView<>();
        userList.getItems().addAll(users);
    
        // Input for adding user
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Enter new username");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Enter password");

    
        // Buttons
        Button addUserBtn = new Button("Add User");
        Button lockUserBtn = new Button("Lock/Unlock");
        Button deleteUserBtn = new Button("Delete");
    
        // Button actions
        addUserBtn.setOnAction(e -> {
            String newUsername = usernameInput.getText().trim();
            String password = passwordInput.getText();

            if (!newUsername.isEmpty() && !password.isEmpty()) {
                try {
                    hashing hasher = new hashing(password);
                    String[] hashed = hasher.generateHash(); // returns [hash, salt]
                    String hash = hashed[0];
                    String salt = hashed[1];

                    String sql = "INSERT INTO users (Username, PasswordHash, Salt, Role, IsLocked) VALUES (?, ?, ?, 'member', false)";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, newUsername);
                    stmt.setString(2, hash);
                    stmt.setString(3, salt);
                    stmt.executeUpdate();

                    User newUser = new User(newUsername);
                    users.add(newUser);
                    userList.getItems().add(newUser);

                    usernameInput.clear();
                    passwordInput.clear();
                    showInfo("User Added", newUsername + " was added.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Could not add user.");
                }
            } else {
                showAlert("Input Error", "Please enter username and password.");
            }
        });

        
    
        lockUserBtn.setOnAction(e -> {
            User selected = userList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean newLockState = !selected.isLocked(); // toggle
                try {
                    String sql = "UPDATE users SET IsLocked = ? WHERE Username = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setBoolean(1, newLockState);
                    stmt.setString(2, selected.getUsername());
                    stmt.executeUpdate();
        
                    selected.setLocked(newLockState);
                    userList.refresh(); // refresh UI display
                    showInfo("User Updated", selected.getUsername() + " is now " + (newLockState ? "locked" : "unlocked") + ".");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Could not update user.");
                }
            }
        });
        
    
        deleteUserBtn.setOnAction(e -> {
            User selected = userList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    String sql = "DELETE FROM users WHERE Username = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, selected.getUsername());
                    stmt.executeUpdate();
        
                    users.remove(selected);
                    userList.getItems().remove(selected);
                    showInfo("User Deleted", selected.getUsername() + " was deleted.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Could not delete user.");
                }
            }
        });
        
    
        // Layout
        HBox buttons = new HBox(10, addUserBtn, lockUserBtn, deleteUserBtn);
        VBox layout = new VBox(10,
            new Label("Registered Users:"),
            userList,
            new Label("New Username:"),
            usernameInput,
            new Label("New Password:"),
            passwordInput,
            buttons
        );

        layout.setPadding(new Insets(15));
    
        Scene scene = new Scene(layout, 450, 500);
        userStage.setScene(scene);
        userStage.show();
    }
    

    private void showSecurityPolicy(SecurityPolicy policy) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Enforced Security Policy");
        alert.setHeaderText("Current Policy Applied:");
        alert.setContentText(policy.getDescription());
        alert.showAndWait();
    }

    private List<String> getSystemLogs() {
        List<String> logs = new ArrayList<>();
    
        String query = "SELECT Timestamp, Action FROM logs ORDER BY Timestamp DESC";
    
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String time = rs.getTimestamp("Timestamp").toString();
                String action = rs.getString("Action");
                logs.add(time + " - " + action);
            }
    
        } catch (SQLException e) {
            logs.add("Error retrieving logs: " + e.getMessage());
            e.printStackTrace();
        }
    
        return logs;
    }    


}
