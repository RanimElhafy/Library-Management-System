package librarian_assistant;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Login interface for librarian assistants
 * Validates credentials and opens the appropriate dashboard
 */
public class LibrarianAssistantLogin {
    private Stage stage;
    private Connection connection;
    
    public LibrarianAssistantLogin(Stage stage, Connection connection) {
        this.stage = stage;
        this.connection = connection;
    }
    
    public void display() {
        stage.setTitle("Librarian Assistant Login");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text sceneTitle = new Text("Librarian Assistant Login");
        sceneTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        grid.add(sceneTitle, 0, 0, 2, 1);
        
        Label usernameLabel = new Label("Username:");
        grid.add(usernameLabel, 0, 1);
        
        TextField usernameField = new TextField();
        grid.add(usernameField, 1, 1);
        
        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 2);
        
        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);
        
        Button loginBtn = new Button("Login");
        Button cancelBtn = new Button("Cancel");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(loginBtn, cancelBtn);
        grid.add(buttonBox, 1, 4);
        
        Text messageText = new Text();
        messageText.setStyle("-fx-fill: red;");
        grid.add(messageText, 1, 6);
        
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageText.setText("Please enter username and password");
                return;
            }
            
            try {
                boolean authenticated = authenticateAssistant(username, password);
                
                if (authenticated) {
                    DBLogger.log("INFO", "LibrarianAssistantLogin", "Login successful for: " + username, username);
                    
                    // Close login window
                    Stage assistantStage = new Stage();
                    LibrarianAssistantInterface dashboard = new LibrarianAssistantInterface(assistantStage, username, connection);
                    dashboard.initializeComponents();
                    
                    stage.close();
                } else {
                    messageText.setText("Invalid username or password");
                    DBLogger.log("WARN", "LibrarianAssistantLogin", "Failed login attempt for: " + username, "system");
                }
            } catch (Exception ex) {
                messageText.setText("Login error: " + ex.getMessage());
                DBLogger.log("ERROR", "LibrarianAssistantLogin", "Login error: " + ex.getMessage(), "system");
                ex.printStackTrace();
            }
        });
        
        cancelBtn.setOnAction(e -> stage.close());
        
        Scene scene = new Scene(grid, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    
    private boolean authenticateAssistant(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE Username = ? AND Password = ? AND RoleID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password); // In a real system, you'd use proper password hashing
            statement.setInt(3, 4); // Assuming RoleID 4 is for Librarian Assistants
            
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // If there's a record, authentication passed
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}