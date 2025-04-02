package member;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BorrowingHistory {

    public void start(Stage stage, String username, Connection connection) {
        VBox layout = new VBox(10);
        TextArea historyArea = new TextArea();
        historyArea.setEditable(false);

        try {
            int memberId = getMemberIdByUsername(connection, username);

            PreparedStatement stmt = connection.prepareStatement(
                "SELECT BookID, BorrowDate, ReturnDate FROM borrowingrecords WHERE MemberID = ?"
            );

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder history = new StringBuilder("Borrowing History:\n\n");

            while (rs.next()) {
                history.append("Book ID: ").append(rs.getInt("BookID"))
                       .append(" | Borrowed On: ").append(rs.getDate("BorrowDate"))
                       .append(" | Returned On: ")
                       .append(rs.getDate("ReturnDate") != null ? rs.getDate("ReturnDate") : "Not returned")
                       .append("\n");
            }

            historyArea.setText(history.toString());

        } catch (Exception e) {
            e.printStackTrace();
            historyArea.setText("Failed to fetch borrowing history.");
        }

        layout.getChildren().add(historyArea);
        Scene scene = new Scene(layout, 500, 400);
        stage.setScene(scene);
        stage.setTitle("Borrowing History");
        stage.show();
    }

    private int getMemberIdByUsername(Connection connection, String username) throws Exception {
        PreparedStatement stmt = connection.prepareStatement(
            "SELECT MemberID FROM members WHERE ContactInfo = CONCAT(?, '@email.com')"
        );
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("MemberID");
        return -1;
    }
    
}
