package librarian;

import common.DBLogger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBookUI {
    private final Connection con;
    private final String username;

    public ReturnBookUI(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Return Book");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setHgap(10);
        layout.setVgap(10);

        TextField borrowIdField = new TextField();
        borrowIdField.setPromptText("Enter Borrow ID");

        Button returnBtn = new Button("Return Book");

        layout.add(new Label("Borrow ID:"), 0, 0);
        layout.add(borrowIdField, 1, 0);
        layout.add(returnBtn, 1, 1);

        returnBtn.setOnAction(e -> {
            try {
                int borrowId = Integer.parseInt(borrowIdField.getText().trim());
                processReturn(borrowId);
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid numeric Borrow ID.");
            }
        });

        stage.setScene(new Scene(layout, 350, 150));
        stage.show();
    }

    private void processReturn(int borrowId) {
        String selectQuery = "SELECT BookID, DueDate, Overdue FROM borrowingrecords WHERE BorrowID = ?";
        String updateReturn = "UPDATE borrowingrecords SET ReturnDate = ?, Overdue = ?, FineAmount = ? WHERE BorrowID = ?";
        String markAvailable = "UPDATE books SET Availability = 1 WHERE BookID = ?";

        try (PreparedStatement selectStmt = con.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, borrowId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int bookId = rs.getInt("BookID");
                LocalDate dueDate = LocalDate.parse(rs.getString("DueDate"));
                LocalDate today = LocalDate.now();
                boolean isOverdue = today.isAfter(dueDate);
                double fine = isOverdue ? ChronoUnit.DAYS.between(dueDate, today) * 2.0 : 0.0;

                try (PreparedStatement updateStmt = con.prepareStatement(updateReturn);
                     PreparedStatement bookStmt = con.prepareStatement(markAvailable)) {

                    updateStmt.setString(1, today.toString());
                    updateStmt.setBoolean(2, isOverdue);
                    updateStmt.setDouble(3, fine);
                    updateStmt.setInt(4, borrowId);
                    updateStmt.executeUpdate();

                    bookStmt.setInt(1, bookId);
                    bookStmt.executeUpdate();

                    DBLogger.log("INFO", "ReturnBookUI", "Book returned for BorrowID: " + borrowId, username);
                    showInfo("Success", "Book returned successfully." + (isOverdue ? "\nFine: $" + fine : ""));
                }
            } else {
                showAlert("Not Found", "Borrow ID not found in records.");
            }

        } catch (Exception e) {
            DBLogger.log("ERROR", "ReturnBookUI", "Error processing return for BorrowID: " + borrowId, username);
            e.printStackTrace();
            showAlert("Error", "Could not process the return.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
