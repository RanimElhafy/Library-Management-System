package member;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;

public class MemInterface {
    private Stage stage;
    private String contactEmail;
    private Connection connection;

    public MemInterface(Stage stage, String contactEmail, Connection connection) {
        this.stage = stage;
        this.contactEmail = contactEmail;
        this.connection = connection;
    }

    public void initializeComponents() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, Member: " + contactEmail);

        Button viewBooksBtn = new Button("📖 View Available Books");
        Button borrowReturnBtn = new Button("📚 Borrow / Return Books");
        Button historyBtn = new Button("📅 View Borrowing History");

        // Set button actions
        viewBooksBtn.setOnAction(e -> new ViewBooks().start(new Stage(), connection));
        borrowReturnBtn.setOnAction(e -> new BorrowReturnBooks().start(new Stage(), contactEmail, connection));
        historyBtn.setOnAction(e -> new BorrowingHistory().start(new Stage(), contactEmail, connection));

        layout.getChildren().addAll(welcome, viewBooksBtn, borrowReturnBtn, historyBtn);

        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Member Dashboard");
        stage.show();
    }
}
