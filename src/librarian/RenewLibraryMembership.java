package librarian;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RenewLibraryMembership {
    public GridPane layout;
    private Connection con;
    private String username;

    public RenewLibraryMembership(Connection con, String username) {
        this.con = con;
        this.username = username;
        addComponents();
    }

    private void addComponents() {
        layout = new GridPane();
        layout.setHgap(10);
        layout.setVgap(10);

        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>();
        memberIdSpinner.setEditable(true);
        memberIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, getMaxMemberID()));

        Label typeLabel = new Label("New Membership Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Basic", "Premium", "Student");

        Button renewButton = new Button("Renew Membership");
        renewButton.setOnAction(e -> {
            int memberId = memberIdSpinner.getValue();
            String type = typeComboBox.getValue();
            try {
                renewMembership(memberId, type);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        layout.add(memberIdLabel, 0, 0);
        layout.add(memberIdSpinner, 1, 0);
        layout.add(typeLabel, 0, 1);
        layout.add(typeComboBox, 1, 1);
        layout.add(renewButton, 0, 2);
    }

    private void renewMembership(int memberId, String type) throws SQLException {
        if (type == null || type.isEmpty() || !validateMemberExists(memberId)) {
            showAlert("Input Error", "Provide valid Member ID and Membership Type");
            return;
        }

        String updateQuery = "UPDATE members SET MembershipType = ?, MembershipExpiry = ? WHERE MemberID = ?";
        PreparedStatement stmt = con.prepareStatement(updateQuery);
        stmt.setString(1, type);
        stmt.setString(2, LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        stmt.setInt(3, memberId);
        stmt.executeUpdate();

        showInfo("Success", "Membership renewed for MemberID: " + memberId);
    }

    private boolean validateMemberExists(int memberId) throws SQLException {
        String query = "SELECT * FROM members WHERE MemberID = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, memberId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private int getMaxMemberID() {
        try {
            String query = "SELECT MAX(MemberID) as MaxID FROM members";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("MaxID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
