package librarian;

import common.DBLogger;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineCalculator {
    private final Connection con;
    private final String username;
    private final double DAILY_FINE_RATE = 2.00;

    public FineCalculator(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void calculateFineForMember(int memberId) {
        String query = "SELECT BorrowID, DueDate FROM borrowingrecords WHERE MemberID = ? AND Overdue = 0";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            boolean anyFines = false;

            while (rs.next()) {
                int borrowId = rs.getInt("BorrowID");
                LocalDate dueDate = LocalDate.parse(rs.getString("DueDate"));
                LocalDate today = LocalDate.now();

                if (today.isAfter(dueDate)) {
                    long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                    double fine = DAILY_FINE_RATE * overdueDays;

                    markOverdue(borrowId, fine);
                    anyFines = true;
                    DBLogger.log("INFO", "FineCalculator", "Fine calculated for BorrowID: " + borrowId + " (Fine: " + fine + ")", username);
                }
            }

            if (anyFines) {
                showInfo("Fine Calculated", "Overdue fines have been calculated and updated.");
            } else {
                showInfo("No Fines", "No overdue books for this member.");
            }

        } catch (SQLException e) {
            DBLogger.log("ERROR", "FineCalculator", "Error while calculating fine for MemberID: " + memberId, username);
            showAlert("Database Error", "Could not calculate fines. Try again.");
            e.printStackTrace();
        }
    }

    private void markOverdue(int borrowId, double fineAmount) throws SQLException {
        String updateQuery = "UPDATE borrowingrecords SET Overdue = 1, FineAmount = ? WHERE BorrowID = ?";
        try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
            updateStmt.setDouble(1, fineAmount);
            updateStmt.setInt(2, borrowId);
            updateStmt.executeUpdate();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
