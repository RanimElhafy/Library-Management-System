package member;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewBooks {
    public void start(Stage stage, Connection connection) {
        VBox layout = new VBox(10);
        TextArea booksArea = new TextArea();
        booksArea.setEditable(false);

        StringBuilder builder = new StringBuilder("Available Books:\n\n");
        try {
            String sql = "SELECT BookID, Title, Author FROM books WHERE Availability = 1";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                builder.append("Book ID: ").append(rs.getInt("BookID"))
                    .append(" | Title: ").append(rs.getString("Title"))
                    .append(" | Author: ").append(rs.getString("Author"))
                    .append("\n");
            }

            booksArea.setText(builder.toString());

        } catch (Exception e) {
            e.printStackTrace();
            booksArea.setText("Error fetching books:\n" + e.getMessage());
        }


        layout.getChildren().add(booksArea);
        Scene scene = new Scene(layout, 500, 400);
        stage.setScene(scene);
        stage.setTitle("Available Books");
        stage.show();
    }
}
