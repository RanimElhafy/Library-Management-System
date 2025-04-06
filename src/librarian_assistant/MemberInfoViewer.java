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

/**
 * Allows librarian assistants to view member information
 * Read-only access to member data and borrowing history
 */
public class MemberInfoViewer {
    private Connection con;
    private String username;
    
    public MemberInfoViewer(Connection con, String username) {
        this.con = con;
        this.username = username;
    }
    
    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Member Information Viewer");
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        
        // Create search panel
        GridPane searchPanel = createSearchPanel();
        mainLayout.setTop(searchPanel);
        
        // Create member details panel
        GridPane detailsPanel = new GridPane();
        detailsPanel.setHgap(10);
        detailsPanel.setVgap(10);
        detailsPanel.setPadding(new Insets(15, 0, 15, 0));
        
        Label memberDetailsLabel = new Label("Member Details:");
        memberDetailsLabel.setStyle("-fx-font-weight: bold");
        
        TextArea memberDetailsArea = new TextArea();
        memberDetailsArea.setEditable(false);
        memberDetailsArea.setPrefRowCount(5);
        memberDetailsArea.setId("memberDetailsArea");
        
        detailsPanel.add(memberDetailsLabel, 0, 0);
        detailsPanel.add(memberDetailsArea, 0, 1);
        
        // Create borrowing history table
        TableView<BorrowingRecord> borrowingTable = createBorrowingHistoryTable();
        borrowingTable.setId("borrowingTable");
        
        Label borrowingHistoryLabel = new Label("Borrowing History:");
        borrowingHistoryLabel.setStyle("-fx-font-weight: bold");
        
        VBox borrowingSection = new VBox(10, borrowingHistoryLabel, borrowingTable);
        
        // Add both to center in a VBox
        VBox centerContent = new VBox(15);
        centerContent.getChildren().addAll(detailsPanel, borrowingSection);
        mainLayout.setCenter(centerContent);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private GridPane createSearchPanel() {
        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(10);
        panel.setPadding(new Insets(0, 0, 10, 0));
        
        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>(1, getMaxMemberID(), 1);
        memberIdSpinner.setEditable(true);
        
        Label memberNameLabel = new Label("or Member Name:");
        TextField memberNameField = new TextField();
        
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> {
            try {
                int memberId = memberIdSpinner.getValue();
                String memberName = memberNameField.getText().trim();
                
                // Get the UI components
                Scene scene = searchBtn.getScene();
                TextArea detailsArea = (TextArea) scene.lookup("#memberDetailsArea");
                TableView<BorrowingRecord> table = (TableView<BorrowingRecord>) scene.lookup("#borrowingTable");
                
                if (detailsArea != null && table != null) {
                    if (!memberName.isEmpty()) {
                        // Search by name
                        searchMemberByName(memberName, detailsArea, table);
                    } else {
                        // Search by ID
                        loadMemberInfo(memberId, detailsArea, table);
                    }
                }
            } catch (Exception ex) {
                showAlert("Search Error", "Error searching for member: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        panel.add(memberIdLabel, 0, 0);
        panel.add(memberIdSpinner, 1, 0);
        panel.add(memberNameLabel, 2, 0);
        panel.add(memberNameField, 3, 0);
        panel.add(searchBtn, 4, 0);
        
        return panel;
    }
    
    private TableView<BorrowingRecord> createBorrowingHistoryTable() {
        TableView<BorrowingRecord> table = new TableView<>();
        
        // Create columns
        TableColumn<BorrowingRecord, Integer> borrowIdCol = new TableColumn<>("Borrow ID");
        borrowIdCol.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        
        TableColumn<BorrowingRecord, Integer> bookIdCol = new TableColumn<>("Book ID");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        
        TableColumn<BorrowingRecord, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        
        TableColumn<BorrowingRecord, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(150);
        
        TableColumn<BorrowingRecord, String> borrowDateCol = new TableColumn<>("Borrow Date");
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        
        TableColumn<BorrowingRecord, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        
        TableColumn<BorrowingRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add columns to table
        table.getColumns().addAll(borrowIdCol, bookIdCol, titleCol, authorCol, 
                                 borrowDateCol, dueDateCol, statusCol);
        
        return table;
    }
    
    private void loadMemberInfo(int memberId, TextArea detailsArea, TableView<BorrowingRecord> borrowingTable) {
        try {
            String query = "SELECT * FROM members WHERE MemberID = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Member ID: ").append(rs.getInt("MemberID")).append("\n");
                details.append("Name: ").append(rs.getString("Name")).append("\n");
                details.append("Contact: ").append(rs.getString("ContactInfo")).append("\n");
                details.append("Membership Type: ").append(rs.getString("MembershipType")).append("\n");
                
                String registrationDate = rs.getString("RegistrationDate");
                details.append("Registration Date: ").append(registrationDate).append("\n");
                
                String expiryDate = rs.getString("MembershipExpiry");
                details.append("Membership Expiry: ").append(expiryDate).append("\n");
                
                // Check if membership is active
                LocalDate expiry = LocalDate.parse(expiryDate);
                if (expiry.isBefore(LocalDate.now())) {
                    details.append("Membership Status: EXPIRED\n");
                } else {
                    details.append("Membership Status: ACTIVE\n");
                }
                
                detailsArea.setText(details.toString());
                
                // Load borrowing history
                loadBorrowingHistory(memberId, borrowingTable);
                
                DBLogger.log("INFO", "MemberInfoViewer", "Loaded info for MemberID: " + memberId, username);
            } else {
                detailsArea.setText("No member found with ID: " + memberId);
                borrowingTable.setItems(FXCollections.observableArrayList());
                DBLogger.log("WARN", "MemberInfoViewer", "No member found with ID: " + memberId, username);
            }
            
        } catch (SQLException e) {
            detailsArea.setText("Error loading member information: " + e.getMessage());
            DBLogger.log("ERROR", "MemberInfoViewer", "Error loading member data: " + e.getMessage(), username);
            e.printStackTrace();
        }
    }
    
    private void searchMemberByName(String name, TextArea detailsArea, TableView<BorrowingRecord> borrowingTable) {
        try {
            String query = "SELECT * FROM members WHERE Name LIKE ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int memberId = rs.getInt("MemberID");
                loadMemberInfo(memberId, detailsArea, borrowingTable);
            } else {
                detailsArea.setText("No member found with name containing: " + name);
                borrowingTable.setItems(FXCollections.observableArrayList());
                DBLogger.log("WARN", "MemberInfoViewer", "No member found with name: " + name, username);
            }
            
        } catch (SQLException e) {
            detailsArea.setText("Error searching for member: " + e.getMessage());
            DBLogger.log("ERROR", "MemberInfoViewer", "Error searching member: " + e.getMessage(), username);
            e.printStackTrace();
        }
    }
    
    private void loadBorrowingHistory(int memberId, TableView<BorrowingRecord> table) {
        ObservableList<BorrowingRecord> records = FXCollections.observableArrayList();
        
        try {
            String query = 
                "SELECT br.BorrowID, br.BookID, b.Title, b.Author, " +
                "br.BorrowDate, br.DueDate, br.Overdue " +
                "FROM borrowingrecords br " +
                "JOIN books b ON br.BookID = b.BookID " +
                "WHERE br.MemberID = ? " +
                "ORDER BY br.BorrowDate DESC";
            
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int borrowId = rs.getInt("BorrowID");
                int bookId = rs.getInt("BookID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String borrowDate = rs.getString("BorrowDate");
                String dueDate = rs.getString("DueDate");
                boolean overdue = rs.getBoolean("Overdue");
                
                String status;
                if (overdue) {
                    status = "Overdue";
                } else if (LocalDate.parse(dueDate).isBefore(LocalDate.now())) {
                    status = "Late";
                } else {
                    status = "Active";
                }
                
                records.add(new BorrowingRecord(borrowId, bookId, title, author, 
                                              borrowDate, dueDate, status));
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading borrowing history: " + e.getMessage());
            DBLogger.log("ERROR", "MemberInfoViewer", "Error loading borrowing history: " + e.getMessage(), username);
            e.printStackTrace();
        }
        
        table.setItems(records);
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
    
    // BorrowingRecord class for TableView
    public static class BorrowingRecord {
        private final int borrowId;
        private final int bookId;
        private final String title;
        private final String author;
        private final String borrowDate;
        private final String dueDate;
        private final String status;
        
        public BorrowingRecord(int borrowId, int bookId, String title, String author,
                             String borrowDate, String dueDate, String status) {
            this.borrowId = borrowId;
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.status = status;
        }
        
        public int getBorrowId() { return borrowId; }
        public int getBookId() { return bookId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getBorrowDate() { return borrowDate; }
        public String getDueDate() { return dueDate; }
        public String getStatus() { return status; }
    }
}