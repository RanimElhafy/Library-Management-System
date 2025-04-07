package librarian;

import common.DBLogger;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Connection;

public class BorrowingPlanUI {
    public GridPane layout;
    private Connection con;
    private BorrowingPlanFunctions planFunctions;
    private String username;

    public BorrowingPlanUI(Connection con, String username) {
        layout = new GridPane();
        this.con = con;
        this.username = username;
        this.planFunctions = new BorrowingPlanFunctions(con, username);
        addComponents();
    }

    private void addComponents() {
        layout.setVgap(10);
        layout.setHgap(10);

        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>();
        memberIdSpinner.setEditable(true);
        memberIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, planFunctions.getMaxMemberID()));

        Label bookIdLabel = new Label("Book ID:");
        TextField bookIdField = new TextField();

        Button assignButton = new Button("Record Borrowing");

        assignButton.setOnAction(e -> {
            int memberId = memberIdSpinner.getValue();
            int bookId = Integer.parseInt(bookIdField.getText());
            planFunctions.recordBorrowing(memberId, bookId);
        });

        layout.add(memberIdLabel, 0, 0);
        layout.add(memberIdSpinner, 1, 0);
        layout.add(bookIdLabel, 0, 1);
        layout.add(bookIdField, 1, 1);
        layout.add(assignButton, 0, 2);
    }
}
