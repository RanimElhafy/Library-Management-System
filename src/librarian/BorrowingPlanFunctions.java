package librarian;

import common.DBLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowingPlanFunctions {
    private Connection con;
    private String username;

    public BorrowingPlanFunctions(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void recordBorrowing(int memberId, int bookId) {
        try {
            if (!validateMember(memberId)) {
                showAlert("Invalid Member", "Member ID does not exist.");
                return;
            }

            if (!validateBookAvailability(bookId)) {
                showAlert("Book Unavailable", "Book is already borrowed or unavailable.");
                return;
            }

            String query = "INSERT INTO borrowingrecords (BorrowID, MemberID, BookID, BorrowDate, DueDate, Overdue, FineAmount) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);

            int borrowId = getNextBorrowID();
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(14);

            statement.setInt(1, borrowId);
            statement.setInt(2, memberId);
            statement.setInt(3, bookId);
            statement.setString(4, borrowDate.toString());
            statement.setString(5, dueDate.toString());
            statement.setBoolean(6, false);
            statement.setDouble(7, 0.00);

            statement.executeUpdate();

            markBookUnavailable(bookId);

            DBLogger.log("INFO", "BorrowingPlanFunctions", "Recorded borrowing for MemberID " + memberId + " and BookID " + bookId, username);
            showConfirm("Borrow Recorded", "Book has been borrowed successfully.");

        } catch (Exception e) {
            DBLogger.log("ERROR", "BorrowingPlanFunctions", "Failed to record borrowing", username);
            e.printStackTrace();
            showAlert("Error", "An error occurred while recording borrowing.");
        }
    }

    public int getNextBorrowID() throws SQLException {
        String query = "SELECT MAX(BorrowID) AS MaxID FROM borrowingrecords";
        PreparedStatement stmt = con.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("MaxID") + 1;
        return 1;
    }

    public boolean validateMember(int memberId) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT MemberID FROM members WHERE MemberID = ?");
        stmt.setInt(1, memberId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public boolean validateBookAvailability(int bookId) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT Availability FROM books WHERE BookID = ?");
        stmt.setInt(1, bookId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getBoolean("Availability");
    }

    public void markBookUnavailable(int bookId) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("UPDATE books SET Availability = 0 WHERE BookID = ?");
        stmt.setInt(1, bookId);
        stmt.executeUpdate();
    }

    public int getMaxMemberID() {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT MAX(MemberID) AS MaxID FROM members");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("MaxID");
        } catch (SQLException e) {
            DBLogger.log("ERROR", "BorrowingPlanFunctions", "Failed to get max member ID.", username);
            e.printStackTrace();
        }
        return 1;
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showConfirm(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
