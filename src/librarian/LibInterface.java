package librarian;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;

public class LibInterface {
    private Stage stage;
    private String username;
    private Connection connection;

    public LibInterface(Stage stage, String username, Connection connection) {
        this.stage = stage;
        this.username = username;
        this.connection = connection;
    }

    public void initializeComponents() {
        VBox layout = new VBox(20);
        Label welcome = new Label("Welcome Librarian: " + username);

        layout.getChildren().addAll(welcome);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Librarian Dashboard");
        stage.show();
    }
}
