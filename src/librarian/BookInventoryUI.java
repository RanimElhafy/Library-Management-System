package librarian;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class BookInventoryUI {
    private final Connection con;
    private final String username;

    public BookInventoryUI(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Book Inventory Management");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setVgap(10);
        layout.setHgap(10);

        TextField bookIdField = new TextField();
        TextField titleField = new TextField();
        TextField authorField = new TextField();

        Button addBtn = new Button("Add Book");
        Button removeBtn = new Button("Remove Book");

        layout.add(new Label("Book ID:"), 0, 0);
        layout.add(bookIdField, 1, 0);
        layout.add(new Label("Title:"), 0, 1);
        layout.add(titleField, 1, 1);
        layout.add(new Label("Author:"), 0, 2);
        layout.add(authorField, 1, 2);
        layout.add(addBtn, 0, 3);
        layout.add(removeBtn, 1, 3);

        addBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(bookIdField.getText());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                if (title.isEmpty() || author.isEmpty()) {
                    showAlert("Validation Error", "Title and Author cannot be empty.");
                    return;
                }

                PreparedStatement stmt = con.prepareStatement(
                        "INSERT INTO books (BookID, Title, Author, Availability) VALUES (?, ?, ?, 1)");
                stmt.setInt(1, id);
                stmt.setString(2, title);
                stmt.setString(3, author);
                stmt.executeUpdate();

                DBLogger.log("INFO", "BookInventoryUI", "Book added: " + title, username);
                showInfo("Success", "Book added to inventory.");
            } catch (Exception ex) {
                showAlert("Error", "Failed to add book.");
                ex.printStackTrace();
            }
        });

        removeBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(bookIdField.getText());
                PreparedStatement stmt = con.prepareStatement("DELETE FROM books WHERE BookID = ?");
                stmt.setInt(1, id);
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    DBLogger.log("INFO", "BookInventoryUI", "Book removed: ID " + id, username);
                    showInfo("Success", "Book removed.");
                } else {
                    showAlert("Not Found", "No book found with that ID.");
                }
            } catch (Exception ex) {
                showAlert("Error", "Failed to remove book.");
                ex.printStackTrace();
            }
        });

        stage.setScene(new Scene(layout, 400, 250));
        stage.show();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
