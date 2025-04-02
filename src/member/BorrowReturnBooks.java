package member;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BorrowReturnBooks {

    public void start(Stage stage, String contactEmail, Connection connection) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label label = new Label("Enter Book ID:");
        TextField bookIdField = new TextField();
        Button borrowBtn = new Button("Borrow Book");
        Button returnBtn = new Button("Return Book");
        Label statusLabel = new Label();

        borrowBtn.setOnAction(e -> {
            int bookId = Integer.parseInt(bookIdField.getText());
            try {
                // Get member ID using username
                int memberId = getMemberIdByUsername(connection, contactEmail);
                if (memberId == -1) {
                    statusLabel.setText("User not found.");
                    return;
                }

                // Check availability
                PreparedStatement checkStmt = connection.prepareStatement("SELECT Availability FROM books WHERE BookId = ?");
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getBoolean("Availability")) {
                    // Insert borrow record
                    PreparedStatement borrowStmt = connection.prepareStatement(
                        "INSERT INTO borrowingrecords (MemberID, BookID, BorrowDate) VALUES (?, ?, CURRENT_DATE)"
                    );

                    borrowStmt.setInt(1, memberId);
                    borrowStmt.setInt(2, bookId);
                    borrowStmt.executeUpdate();

                    // Update book availability
                    PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE books SET Availability = FALSE WHERE BookId = ?"
                    );
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    statusLabel.setText("Book borrowed successfully.");
                } else {
                    statusLabel.setText("Book is not available.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error occurred while borrowing.");
            }
        });

        returnBtn.setOnAction(e -> {
            int bookId = Integer.parseInt(bookIdField.getText());
            try {
                int memberId = getMemberIdByUsername(connection, contactEmail);

                PreparedStatement returnStmt = connection.prepareStatement(
                    "UPDATE borrowingrecords SET ReturnDate = CURRENT_DATE " +
                    "WHERE MemberID = ? AND BookID = ? AND ReturnDate IS NULL"
                );

                returnStmt.setInt(1, memberId);
                returnStmt.setInt(2, bookId);
                int rows = returnStmt.executeUpdate();

                if (rows > 0) {
                    PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE books SET Availability = TRUE WHERE BookID = ?"
                    );
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    statusLabel.setText("Book returned successfully.");
                } else {
                    statusLabel.setText("No active borrow record found.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error occurred while returning.");
            }
        });

        layout.getChildren().addAll(label, bookIdField, borrowBtn, returnBtn, statusLabel);
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Borrow / Return Book");
        stage.show();
    }

    private int getMemberIdByUsername(Connection connection, String username) throws Exception {
        PreparedStatement stmt = connection.prepareStatement(
            "SELECT m.MemberID " +
            "FROM members m JOIN users u ON m.ContactInfo = CONCAT(u.Username, '@email.com') " +
            "WHERE u.Username = ?"
        );
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("MemberID");
        return -1;
    }
}
