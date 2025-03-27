package librarian;

import java.sql.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class ManageReadingProgress {
    private Connection con;
    private String username;
    public GridPane manageProgressLayout = new GridPane();

    public ManageReadingProgress(Connection con, String username) {
        this.con = con;
        this.username = username;
    }

    public void initializeComponents() {
        manageProgressLayout.setPadding(new Insets(10));
        manageProgressLayout.setHgap(10);
        manageProgressLayout.setVgap(10);

        Button insertButton = new Button("Insert Progress");
        Button trackButton = new Button("Track Progress");

        manageProgressLayout.add(insertButton, 0, 0);
        manageProgressLayout.add(trackButton, 0, 1);

        insertButton.setOnAction(e -> {
            manageProgressLayout.getChildren().clear();
            InsertReadingProgress insert = new InsertReadingProgress(con, manageProgressLayout, username);
            insert.initializeComponents();
        });

        trackButton.setOnAction(e -> {
            manageProgressLayout.getChildren().clear();
            TrackReadingProgress track = new TrackReadingProgress(con, manageProgressLayout, username);
            track.initializeComponents();
        });
    }
}
