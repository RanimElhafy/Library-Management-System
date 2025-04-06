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
    
    // Load all users from database to ensure it's up to date
    refreshUserList(userList, users);

    // Input for adding user
    TextField usernameInput = new TextField();
    usernameInput.setPromptText("Enter new username");
    PasswordField passwordInput = new PasswordField();
    passwordInput.setPromptText("Enter password");

    // Add role selection dropdown
    Label roleLabel = new Label("Select Role:");
    ComboBox<String> roleComboBox = new ComboBox<>();
    roleComboBox.getItems().addAll("member", "librarian", "admin", "assistant");
    roleComboBox.setValue("member"); // Default selection

    // Add Refresh button for user list
    Button refreshBtn = new Button("Refresh List");
    refreshBtn.setOnAction(e -> refreshUserList(userList, users));
    
    // Buttons for user management
    Button addUserBtn = new Button("Add User");
    Button lockUserBtn = new Button("Lock/Unlock");
    Button deleteUserBtn = new Button("Delete");

    // Button actions
    addUserBtn.setOnAction(e -> {
        String newUsername = usernameInput.getText().trim();
        String password = passwordInput.getText();
        String selectedRole = roleComboBox.getValue();

        if (!newUsername.isEmpty() && !password.isEmpty()) {
            try {
                // Check if username already exists
                String checkUser = "SELECT Username FROM users WHERE Username = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkUser);
                checkStmt.setString(1, newUsername);
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (checkRs.next()) {
                    showAlert("User Exists", "A user with this username already exists.");
                    return;
                }
                
                hashing hasher = new hashing(password);
                String[] hashed = hasher.generateHash(); // returns [hash, salt]
                String hash = hashed[0];
                String salt = hashed[1];

                // Insert into users table with selected role
                String sql = "INSERT INTO users (Username, PasswordHash, Salt, Role, IsLocked) VALUES (?, ?, ?, ?, false)";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, newUsername);
                stmt.setString(2, hash);
                stmt.setString(3, salt);
                stmt.setString(4, selectedRole);
                stmt.executeUpdate();

                // Set a default RoleID based on selected role
                int roleId = 3; // Default to member (assuming RoleID 3 is member)
                
                if (selectedRole.equals("librarian")) {
                    roleId = 2; // Assuming RoleID 2 is librarian
                } else if (selectedRole.equals("admin")) {
                    roleId = 1; // Assuming RoleID 1 is admin
                } else if (selectedRole.equals("assistant")) {
                    roleId = 4; // Assuming RoleID 4 is assistant
                }
                
                // Insert into members table if role is 'member'
                if (selectedRole.equals("member")) {
                    try {
                        // For members, we now know the table structure from your screenshot
                        String insertMember = "INSERT INTO members (Name, ContactInfo, MembershipType, RegistrationDate, MembershipExpiry, RoleID) VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), ?)";
                        PreparedStatement memberStmt = connection.prepareStatement(insertMember);
                        memberStmt.setString(1, newUsername);
                        memberStmt.setString(2, newUsername + "@example.com"); // Default email format
                        memberStmt.setString(3, "Regular");
                        memberStmt.setInt(4, roleId);
                        memberStmt.executeUpdate();
                    } catch (SQLException memberEx) {
                        System.out.println("Error inserting member data: " + memberEx.getMessage());
                        // Continue without showing alert - user was created in users table
                    }
                }
                
                // Create user object and update UI
                User newUser = new User(newUsername, selectedRole, false);
                users.add(newUser);
                userList.getItems().add(newUser);

                usernameInput.clear();
                passwordInput.clear();
                
                // Show success message
                if ((selectedRole.equals("librarian") || selectedRole.equals("assistant")) ) {
                    showInfo("User Added with Warning", 
                        newUsername + " was added as a " + selectedRole + " to the users table");
                } else {
                    showInfo("User Added", newUsername + " was added as " + selectedRole + ".");
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Could not add user: " + ex.getMessage());
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
                // Check if it's safe to delete
                Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDelete.setTitle("Confirm Deletion");
                confirmDelete.setHeaderText("Delete User: " + selected.getUsername());
                confirmDelete.setContentText("This will permanently delete this user. Continue?");
                
                confirmDelete.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // Delete from the appropriate role-specific table first (foreign key constraints)
                            String role = selected.getRole();
                            if (role.equals("member")) {
                                String memberQuery = "DELETE FROM members WHERE Name = ?";
                                PreparedStatement memberStmt = connection.prepareStatement(memberQuery);
                                memberStmt.setString(1, selected.getUsername());
                                memberStmt.executeUpdate();
                            } else if (role.equals("librarian") || role.equals("assistant")) {
                                String librarianQuery = "DELETE FROM librarians WHERE Name = ?";
                                PreparedStatement librarianStmt = connection.prepareStatement(librarianQuery);
                                librarianStmt.setString(1, selected.getUsername());
                                librarianStmt.executeUpdate();
                            }
                            
                            // Then delete from users table
                            String sql = "DELETE FROM users WHERE Username = ?";
                            PreparedStatement stmt = connection.prepareStatement(sql);
                            stmt.setString(1, selected.getUsername());
                            int result = stmt.executeUpdate();
                            
                            if (result > 0) {
                                users.remove(selected);
                                userList.getItems().remove(selected);
                                showInfo("User Deleted", selected.getUsername() + " was deleted.");
                            } else {
                                showAlert("Error", "User not found in database.");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            showAlert("Error", "Could not delete user: " + ex.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Could not process delete request.");
            }
        } else {
            showAlert("Selection Error", "Please select a user to delete.");
        }
    });
    

    // Layout
    HBox buttons = new HBox(10, addUserBtn, lockUserBtn, deleteUserBtn, refreshBtn);
    
    VBox layout = new VBox(10,
        new Label("Registered Users:"),
        userList,
        new Label("New Username:"),
        usernameInput,
        new Label("New Password:"),
        passwordInput,
        roleLabel,
        roleComboBox,
        buttons
    );

    layout.setPadding(new Insets(15));

    Scene scene = new Scene(layout, 450, 550);
    userStage.setScene(scene);
    userStage.show();
}

// Add method to refresh user list from database
private void refreshUserList(ListView<User> userList, List<User> usersList) {
    userList.getItems().clear();
    usersList.clear();
    
    try {
        String query = "SELECT Username, Role, IsLocked FROM users ORDER BY Username";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String username = rs.getString("Username");
            String role = rs.getString("Role");
            boolean locked = rs.getBoolean("IsLocked");
            
            User user = new User(username, role, locked);
            usersList.add(user);
            userList.getItems().add(user);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Database Error", "Could not load users from database.");
    }
}

// Helper method to get role ID from role name
private int getRoleIdByName(String roleName) {
    int roleId = 3; // Default to member role ID (assuming 3 is member)
    
    try {
        String query = "SELECT RoleID FROM userroles WHERE RoleName = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, roleName);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            roleId = rs.getInt("RoleID");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return roleId;
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
};