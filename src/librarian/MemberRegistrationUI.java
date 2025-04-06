package librarian;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.time.LocalDate;

public class MemberRegistrationUI {
    private Connection con;
    private String username;
    private RegisterLibraryMemberFunctions regFunctions;

    public MemberRegistrationUI(Connection con, String username) {
        this.con = con;
        this.username = username;
        this.regFunctions = new RegisterLibraryMemberFunctions(con, username);
    }

    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Register Library Member");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(15));
        layout.setHgap(10);
        layout.setVgap(10);

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label contactLabel = new Label("Email Address:");
        TextField contactField = new TextField();

        Label typeLabel = new Label("Membership Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Regular", "Premium");

        Button registerBtn = new Button("Register Member");

        layout.add(nameLabel, 0, 0);
        layout.add(nameField, 1, 0);
        layout.add(contactLabel, 0, 1);
        layout.add(contactField, 1, 1);
        layout.add(typeLabel, 0, 2);
        layout.add(typeComboBox, 1, 2);
        layout.add(registerBtn, 1, 3);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String type = typeComboBox.getValue();

            if (!regFunctions.validateInputs(name, contact, type)) {
                regFunctions.showAlert("Input Error", "Please enter valid details.");
                return;
            }

            try {
                int newMemberID = regFunctions.fetchNextMemberID();
                LocalDate today = LocalDate.now();
                LocalDate expiry = type.equals("Premium") ? today.plusYears(1) : today.plusMonths(6);

                MemberRegistrar after = new MemberRegistrar(
                    con, name, contact, today.toString(), expiry,
                    newMemberID, -1, 0, type, username
                );

                after.registerMember();

                nameField.clear();
                contactField.clear();
                typeComboBox.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                regFunctions.showAlert("Registration Failed", "Could not register new member.");
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(layout, 400, 250);
        stage.setScene(scene);
        stage.show();
    }
}
