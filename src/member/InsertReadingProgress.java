package librarian;

import java.sql.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import common.DBLogger;

public class InsertReadingProgress {
    private Connection con;
    private GridPane layout;
    private HBox buttonsLayout;
    private String username;

    public InsertReadingProgress(Connection con, GridPane manageProgressLayout, String username) {
        this.con = con;
        this.username = username;
        this.layout = manageProgressLayout;
    }

    public void initializeComponents() {
        Label memberIdLabel = new Label("Select Member ID:");
        ComboBox<Integer> memberIdComboBox = new ComboBox<>();

        populateMemberIds(memberIdComboBox);

        Label progressDetailLabel = new Label("Reading Notes:");
        TextArea progressDetailTextArea = new TextArea();
        progressDetailTextArea.setWrapText(true);
        progressDetailTextArea.setPrefWidth(200);

        Label progressMetricLabel = new Label("Progress Percentage (1-100):");
        TextField progressMetricField = new TextField();
        progressMetricField.setPrefWidth(200);

        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");

        buttonsLayout = new HBox(10, submitButton, backButton);

        layout.add(memberIdLabel, 0, 0);
        layout.add(memberIdComboBox, 1, 0);
        layout.add(progressDetailLabel, 0, 1);
        layout.add(progressDetailTextArea, 1, 1);
        layout.add(progressMetricLabel, 0, 2);
        layout.add(progressMetricField, 1, 2);
        layout.add(buttonsLayout, 0, 3, 2, 1);

        submitButton.setOnAction(e -> {
            try {
                String progressDetail = progressDetailTextArea.getText();
                double progressMetric = Double.parseDouble(progressMetricField.getText());

                if (memberIdComboBox.getValue() == null || progressDetail.isEmpty()) {
                    showAlert("Empty Fields", "Please fill in all fields.");
                    return;
                }
                if (progressMetric < 1 || progressMetric > 100) {
                    showAlert("Invalid Metric", "Progress must be between 1 and 100.");
                    return;
                }

                insertProgress(memberIdComboBox.getValue(), progressDetail, progressMetric);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Enter a valid number for progress.");
            }
        });

        backButton.setOnAction(e -> {
            layout.getChildren().clear();
            ManageReadingProgress manage = new ManageReadingProgress(con, username);
            manage.initializeComponents();
            layout.add(manage.manageProgressLayout, 0, 0);
        });
    }

    private void populateMemberIds(ComboBox<Integer> comboBox) {
        String query = "SELECT MemberID FROM members";
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getInt("MemberID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertProgress(int memberId, String details, double metric) {
        String insert = "INSERT INTO progress_tracking (MemberID, LibrarianID, ProgressDate, ProgressDetails, ProgressMetric) VALUES (?, ?, CURDATE(), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setInt(1, memberId);
            ps.setInt(2, getLibrarianId(username));
            ps.setString(3, details);
            ps.setDouble(4, metric);
            ps.executeUpdate();
            showAlert("Success", "Reading progress inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Could not insert progress.");
        }
    }

    private int getLibrarianId(String username) throws SQLException {
        String query = "SELECT LibrarianID FROM librarians WHERE Username = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("LibrarianID");
            }
        }
        return -1;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
