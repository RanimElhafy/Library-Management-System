package member;

import javafx.stage.Stage;
import java.sql.Connection;

public class MemInterface {
    public MemInterface(Stage stage, String username, Connection con) {
        System.out.println("[MEMBER] Interface initialized for: " + username);
        // TODO: Use stage and con if needed for further functionality
    }

    public void initializeComponents() {
        System.out.println("Member components initialized.");
    }
}
