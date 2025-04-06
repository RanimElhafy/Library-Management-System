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
 * Allows librarian assistants to handle book borrowing
 * Shows available books and can assign them to members
 */
public class BookBorrowingAssistant {
    private Connection con;
    private String username;
    
    public BookBorrowingAssistant(Connection con, String username) {
        this.con = con;
        this.username = username;
    }
    
    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Book Borrowing Assistant");
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        
        // Create the book borrowing form (top section)
        GridPane borrowingForm = createBorrowingForm();
        mainLayout.setTop(borrowingForm);
        
        // Create available books table (center section)
        TableView<Book> booksTable = createBooksTable();
        refreshBooksTable(booksTable);
        
        // Refresh button for books
        Button refreshBtn = new Button("Refresh Book List");
        refreshBtn.setOnAction(e -> refreshBooksTable(booksTable));
        
        VBox booksSection = new VBox(10, new Label("Available Books"), booksTable, refreshBtn);
        mainLayout.setCenter(booksSection);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private GridPane createBorrowingForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(0, 0, 20, 0));
        
        // Member selection
        Label memberIdLabel = new Label("Member ID:");
        Spinner<Integer> memberIdSpinner = new Spinner<>();
        memberIdSpinner.setEditable(true);
        memberIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, getMaxMemberID()));
        
        // Member info display
        Button loadMemberBtn = new Button("Load Member");
        TextArea memberInfoArea = new TextArea();
        memberInfoArea.setEditable(false);
        memberInfoArea.setPrefRowCount(3);
        memberInfoArea.setPrefWidth(300);
        
        loadMemberBtn.setOnAction(e -> {
            int memberId = memberIdSpinner.getValue();
            String memberInfo = getMemberInfo(memberId);
            memberInfoArea.setText(memberInfo);
        });
        
        // Book selection
        Label bookIdLabel = new Label("Book ID:");
        TextField bookIdField = new TextField();
        
        // Due date selection
        Label dueDateLabel = new Label("Due Date:");
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(14));
        
        // Borrow button
        Button borrowBtn = new Button("Record Borrowing");
        borrowBtn.setOnAction(e -> {
            try {
                int memberId = memberIdSpinner.getValue();
                int bookId = Integer.parseInt(bookIdField.getText());
                LocalDate dueDate = dueDatePicker.getValue();
                
                if (memberInfoArea.getText().isEmpty()) {
                    showAlert("Error", "Please load member information first");
                    return;
                }
                
                recordBorrowing(memberId, bookId, dueDate);
                bookIdField.clear();
                
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid Book ID");
            } catch (Exception ex) {
                showAlert("Error", "Failed to record borrowing: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Add components to form
        form.add(memberIdLabel, 0, 0);
        form.add(memberIdSpinner, 1, 0);
        form.add(loadMemberBtn, 2, 0);
        
        form.add(new Label("Member Info:"), 0, 1);
        form.add(memberInfoArea, 1, 1, 2, 1);
        
        form.add(bookIdLabel, 0, 2);
        form.add(bookIdField, 1, 2);
        
        form.add(dueDateLabel, 0, 3);
        form.add(dueDatePicker, 1, 3);
        
        form.add(borrowBtn, 1, 4);
        
        return form;
    }
    
    private TableView<Book> createBooksTable() {
        TableView<Book> table = new TableView<>();
        
        // Create columns
        TableColumn<Book, Integer> idCol = new TableColumn<>("Book ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(200);
        
        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add columns to table
        table.getColumns().addAll(idCol, titleCol, authorCol, statusCol);
        
        // Double-click handler to select a book
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Book book = row.getItem();
                    // Find text field in the parent scene and set its value
                    Scene scene = row.getScene();
                    if (scene != null) {
                        TextField bookIdField = (TextField) scene.lookup("#bookIdField");
                        if (bookIdField != null) {
                            bookIdField.setText(String.valueOf(book.getId()));
                        }
                    }
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void refreshBooksTable(TableView<Book> table) {
        ObservableList<Book> books = FXCollections.observableArrayList();
        
        try {
            String query = "SELECT BookID, Title, Author, Availability FROM books WHERE Availability = 1";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("BookID");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                boolean available = rs.getBoolean("Availability");
                
                books.add(new Book(id, title, author, available ? "Available" : "Borrowed"));
            }
            
            DBLogger.log("INFO", "BookBorrowingAssistant", "Refreshed available books list", username);
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "BookBorrowingAssistant", "Error loading available books: " + e.getMessage(), username);
            e.printStackTrace();
        }
        
        table.setItems(books);
    }
    
    private void recordBorrowing(int memberId, int bookId, LocalDate dueDate) {
        try {
            // Validate member and book first
            if (!validateMember(memberId)) {
                showAlert("Invalid Member", "Member ID does not exist.");
                return;
            }

            if (!validateBookAvailability(bookId)) {
                showAlert("Book Unavailable", "Book is not available for borrowing.");
                return;
            }

            // Generate a new borrow ID
            int borrowId = getNextBorrowID();
            LocalDate borrowDate = LocalDate.now();

            // Insert the borrowing record
            String insertQuery = "INSERT INTO borrowingrecords (BorrowID, MemberID, BookID, BorrowDate, DueDate, Overdue, FineAmount) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insertQuery);
            insertStmt.setInt(1, borrowId);
            insertStmt.setInt(2, memberId);
            insertStmt.setInt(3, bookId);
            insertStmt.setString(4, borrowDate.toString());
            insertStmt.setString(5, dueDate.toString());
            insertStmt.setBoolean(6, false);
            insertStmt.setDouble(7, 0.00);

            int insertResult = insertStmt.executeUpdate();

            // Update book availability
            String updateQuery = "UPDATE books SET Availability = 0 WHERE BookID = ?";
            PreparedStatement updateStmt = con.prepareStatement(updateQuery);
            updateStmt.setInt(1, bookId);
            
            int updateResult = updateStmt.executeUpdate();

            if (insertResult > 0 && updateResult > 0) {
                DBLogger.log("INFO", "BookBorrowingAssistant", "Book borrowing recorded: MemberID: " + memberId + ", BookID: " + bookId, username);
                showInfo("Success", "Book has been borrowed successfully.\nDue Date: " + dueDate.toString());
            } else {
                showAlert("Error", "Failed to record borrowing");
            }

        } catch (SQLException e) {
            DBLogger.log("ERROR", "BookBorrowingAssistant", "Error recording borrowing: " + e.getMessage(), username);
            showAlert("Database Error", "Could not record borrowing. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getNextBorrowID() throws SQLException {
        String query = "SELECT MAX(BorrowID) AS MaxID FROM borrowingrecords";
        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("MaxID") + 1;
            return 1;
        }
    }
    
    private boolean validateMember(int memberId) throws SQLException {
        String query = "SELECT MemberID FROM members WHERE MemberID = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, memberId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }
    
    private boolean validateBookAvailability(int bookId) throws SQLException {
        String query = "SELECT Availability FROM books WHERE BookID = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, bookId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getBoolean("Availability");
    }
    
    private String getMemberInfo(int memberId) {
        StringBuilder info = new StringBuilder();
        
        try {
            String query = "SELECT Name, ContactInfo, MembershipType, MembershipExpiry FROM members WHERE MemberID = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                info.append("Name: ").append(rs.getString("Name")).append("\n");
                info.append("Contact: ").append(rs.getString("ContactInfo")).append("\n");
                info.append("Membership: ").append(rs.getString("MembershipType"));
                
                // Check if membership is valid
                LocalDate expiry = LocalDate.parse(rs.getString("MembershipExpiry"));
                if (expiry.isBefore(LocalDate.now())) {
                    info.append("\nWARNING: Membership expired on ").append(expiry);
                }
                
                DBLogger.log("INFO", "BookBorrowingAssistant", "Retrieved info for MemberID: " + memberId, username);
            } else {
                info.append("Member not found");
                DBLogger.log("WARN", "BookBorrowingAssistant", "Member not found for ID: " + memberId, username);
            }
            
        } catch (SQLException e) {
            info.append("Error loading member information");
            DBLogger.log("ERROR", "BookBorrowingAssistant", "Error loading member data: " + e.getMessage(), username);
            e.printStackTrace();
        }
        
        return info.toString();
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
    
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Book class for TableView
    public static class Book {
        private final int id;
        private final String title;
        private final String author;
        private final String status;
        
        public Book(int id, String title, String author, String status) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.status = status;
        }
        
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getStatus() { return status; }
    }
}