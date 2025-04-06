package librarian_assistant;

import common.DBLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Allows librarian assistants to view fines for overdue books
 * Cannot modify or process payments - view only
 */
public class FineViewAssistant {
    private Connection con;
    private String username;
    
    public FineViewAssistant(Connection con, String username) {
        this.con = con;
        this.username = username;
    }
    
    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Fine Viewer");
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        
        // Create member search form
        GridPane searchForm = createSearchForm();
        mainLayout.setTop(searchForm);
        
        // Create fines table
        TableView<Fine> finesTable = createFinesTable();
        
        // Refresh button
        Button refreshBtn = new Button("Refresh All Fines");
        refreshBtn.setOnAction(e -> refreshAllFines(finesTable));
        
        VBox finesSection = new VBox(10, new Label("Overdue Books and Fines"), finesTable, refreshBtn);
        finesSection.setPadding(new Insets(10, 0, 0, 0));
        mainLayout.setCenter(finesSection);
        
        // Initial load of all fines
        refreshAllFines(finesTable);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private GridPane createSearchForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(0, 0, 15, 0));
        
        // Member ID search
        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>();
        memberIdSpinner.setEditable(true);
        memberIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, getMaxMemberID()));
        
        // Member search button
        Button searchBtn = new Button("View Member Fines");
        
        // Calculation section
        CheckBox recalculateBox = new CheckBox("Recalculate fines based on current date");
        recalculateBox.setSelected(true);
        
        // Results label
        Label resultLabel = new Label("Select a member to view their fines");
        
        // Add components to form
        form.add(memberIdLabel, 0, 0);
        form.add(memberIdSpinner, 1, 0);
        form.add(searchBtn, 2, 0);
        form.add(recalculateBox, 1, 1);
        form.add(resultLabel, 1, 2);
        
        // Member search handler
        searchBtn.setOnAction(e -> {
            int memberId = memberIdSpinner.getValue();
            TableView<Fine> table = (TableView<Fine>) searchBtn.getScene().lookup("#finesTable");
            
            if (table != null) {
                boolean recalculate = recalculateBox.isSelected();
                refreshFinesForMember(table, memberId, recalculate);
                
                // Update result label
                resultLabel.setText("Showing fines for Member ID: " + memberId);
            }
        });
        
        return form;
    }
    
    private TableView<Fine> createFinesTable() {
        TableView<Fine> table = new TableView<>();
        table.setId("finesTable");
        
        // Create columns
        TableColumn<Fine, Integer> borrowIdCol = new TableColumn<>("Borrow ID");
        borrowIdCol.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        
        TableColumn<Fine, Integer> memberIdCol = new TableColumn<>("Member ID");
        memberIdCol.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        
        TableColumn<Fine, String> memberNameCol = new TableColumn<>("Member Name");
        memberNameCol.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        memberNameCol.setPrefWidth(150);
        
        TableColumn<Fine, Integer> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        
        TableColumn<Fine, String> bookTitleCol = new TableColumn<>("Book Title");
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookTitleCol.setPrefWidth(200);
        
        TableColumn<Fine, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        
        TableColumn<Fine, Integer> daysLateCol = new TableColumn<>("Days Late");
        daysLateCol.setCellValueFactory(new PropertyValueFactory<>("daysLate"));
        
        TableColumn<Fine, Double> fineAmountCol = new TableColumn<>("Fine Amount");
        fineAmountCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        
        // Add columns to table
        table.getColumns().addAll(borrowIdCol, memberIdCol, memberNameCol, bookIdCol, 
                                 bookTitleCol, dueDateCol, daysLateCol, fineAmountCol);
        
        return table;
    }
    
    private void refreshAllFines(TableView<Fine> table) {
        ObservableList<Fine> fines = FXCollections.observableArrayList();
        
        try {
            String query = 
                "SELECT br.BorrowID, br.MemberID, m.Name, br.BookID, b.Title, " +
                "br.DueDate, br.FineAmount, br.Overdue " +
                "FROM borrowingrecords br " +
                "JOIN members m ON br.MemberID = m.MemberID " +
                "JOIN books b ON br.BookID = b.BookID " +
                "WHERE br.DueDate < ? AND br.BorrowDate IS NOT NULL";
            
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, LocalDate.now().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int borrowId = rs.getInt("BorrowID");
                int memberId = rs.getInt("MemberID");
                String memberName = rs.getString("Name");
                int bookId = rs.getInt("BookID");
                String bookTitle = rs.getString("Title");
                String dueDateStr = rs.getString("DueDate");
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                
                // Calculate days late
                long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                daysLate = Math.max(0, daysLate); // Ensure non-negative
                
                // Calculate or get fine amount
                double fineAmount = rs.getDouble("FineAmount");
                boolean isOverdue = rs.getBoolean("Overdue");
                
                // If not marked as overdue or fine amount is 0, calculate it
                if (!isOverdue || fineAmount == 0) {
                    fineAmount = daysLate * 2.00; // Using same rate as FineCalculator
                }
                
                fines.add(new Fine(borrowId, memberId, memberName, bookId, bookTitle, 
                                  dueDateStr, (int)daysLate, fineAmount));
            }
            
            DBLogger.log("INFO", "FineViewAssistant", "Loaded all overdue fines", username);
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "FineViewAssistant", "Error loading fines: " + e.getMessage(), username);
            showAlert("Database Error", "Error loading fines: " + e.getMessage());
            e.printStackTrace();
        }
        
        table.setItems(fines);
    }
    
    private void refreshFinesForMember(TableView<Fine> table, int memberId, boolean recalculate) {
        ObservableList<Fine> fines = FXCollections.observableArrayList();
        
        try {
            String query = 
                "SELECT br.BorrowID, br.MemberID, m.Name, br.BookID, b.Title, " +
                "br.DueDate, br.FineAmount, br.Overdue " +
                "FROM borrowingrecords br " +
                "JOIN members m ON br.MemberID = m.MemberID " +
                "JOIN books b ON br.BookID = b.BookID " +
                "WHERE br.MemberID = ? AND br.DueDate < ? AND br.BorrowDate IS NOT NULL";
            
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            stmt.setString(2, LocalDate.now().toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int borrowId = rs.getInt("BorrowID");
                String memberName = rs.getString("Name");
                int bookId = rs.getInt("BookID");
                String bookTitle = rs.getString("Title");
                String dueDateStr = rs.getString("DueDate");
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                
                // Calculate days late
                long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
                daysLate = Math.max(0, daysLate); // Ensure non-negative
                
                // Calculate or get fine amount
                double fineAmount = rs.getDouble("FineAmount");
                boolean isOverdue = rs.getBoolean("Overdue");
                
                // If requested to recalculate or fine is not set
                if (recalculate || !isOverdue || fineAmount == 0) {
                    fineAmount = daysLate * 2.00; // Using same rate as FineCalculator
                }
                
                fines.add(new Fine(borrowId, memberId, memberName, bookId, bookTitle, 
                                  dueDateStr, (int)daysLate, fineAmount));
            }
            
            DBLogger.log("INFO", "FineViewAssistant", "Loaded fines for MemberID: " + memberId, username);
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "FineViewAssistant", "Error loading fines for member: " + e.getMessage(), username);
            showAlert("Database Error", "Error loading fines: " + e.getMessage());
            e.printStackTrace();
        }
        
        table.setItems(fines);
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
        return 100; // Default fallback
    }
    
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Fine class for TableView
    public static class Fine {
        private final int borrowId;
        private final int memberId;
        private final String memberName;
        private final int bookId;
        private final String bookTitle;
        private final String dueDate;
        private final int daysLate;
        private final double fineAmount;
        
        public Fine(int borrowId, int memberId, String memberName, int bookId, String bookTitle,
                   String dueDate, int daysLate, double fineAmount) {
            this.borrowId = borrowId;
            this.memberId = memberId;
            this.memberName = memberName;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.dueDate = dueDate;
            this.daysLate = daysLate;
            this.fineAmount = fineAmount;
        }
        
        public int getBorrowId() { return borrowId; }
        public int getMemberId() { return memberId; }
        public String getMemberName() { return memberName; }
        public int getBookId() { return bookId; }
        public String getBookTitle() { return bookTitle; }
        public String getDueDate() { return dueDate; }
        public int getDaysLate() { return daysLate; }
        public double getFineAmount() { return fineAmount; }
    }
}