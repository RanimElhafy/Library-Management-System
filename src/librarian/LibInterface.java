package librarian;

import javafx.stage.Stage;
import java.sql.Connection;

public class LibInterface {
    public LibInterface(Stage stage, String username, Connection con) {
        System.out.println("[LIBRARIAN] Interface initialized for: " + username);
        // TODO: Use stage and con if needed for further functionality
    }

    public void initializeComponents() {
        System.out.println("Librarian components initialized.");
    }
}
