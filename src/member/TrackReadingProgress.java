package member;

import java.sql.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class TrackReadingProgress {
    private Connection con;
    private GridPane layout;
    private ComboBox<Integer> memberIdComboBox = new ComboBox<>();
    private ComboBox<Integer> progressIdComboBox = new ComboBox<>();
    private TextArea detailArea = new TextArea();
    private TextArea metricArea = new TextArea();
    private String username;

    public TrackReadingProgress(Connection con, GridPane layout, String username) {
        this.con = con;
        this.layout = layout;
        this.username = username;
    }

    public void initializeComponents() {
        layout.getChildren().clear();

        Label memberLabel = new Label("Select Member:");
        populateMembers();

        Label progressLabel = new Label("Select Record:");
        memberIdComboBox.setOnAction(e -> populateProgress());

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> fetchProgress());

        layout.add(memberLabel, 0, 0);
        layout.add(memberIdComboBox, 1, 0);
        layout.add(progressLabel, 0, 1);
        layout.add(progressIdComboBox, 1, 1);
        layout.add(new Label("Reading Notes:"), 0, 2);
        layout.add(detailArea, 1, 2);
        layout.add(new Label("Progress %:"), 0, 3);
        layout.add(metricArea, 1, 3);
        layout.add(searchButton, 0, 4);
    }

    private void populateMembers() {
        String query = "SELECT DISTINCT MemberID FROM progress_tracking WHERE LibrarianID = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, getLibrarianId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                memberIdComboBox.getItems().add(rs.getInt("MemberID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateProgress() {
        progressIdComboBox.getItems().clear();
        int memberId = memberIdComboBox.getValue();
        String query = "SELECT ProgressID FROM progress_tracking WHERE MemberID = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                progressIdComboBox.getItems().add(rs.getInt("ProgressID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchProgress() {
        int progressId = progressIdComboBox.getValue();
        String query = "SELECT ProgressDetails, ProgressMetric FROM progress_tracking WHERE ProgressID = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, progressId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                detailArea.setText(rs.getString("ProgressDetails"));
                metricArea.setText(String.valueOf(rs.getDouble("ProgressMetric")));
            } else {
                detailArea.setText("");
                metricArea.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getLibrarianId() throws SQLException {
        String query = "SELECT LibrarianID FROM librarians WHERE Username = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("LibrarianID");
        }
        return -1;
    }
}
