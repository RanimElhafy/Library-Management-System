package librarian_assistant;

import common.DBLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

/**
 * Allows librarian assistants to monitor library facilities
 * View-only access to facility status and maintenance schedules
 */
public class FacilityMonitor {
    private Connection con;
    private String username;
    
    public FacilityMonitor(Connection con, String username) {
        this.con = con;
        this.username = username;
    }
    
    public void display() {
        Stage stage = new Stage();
        stage.setTitle("Facility Monitoring");
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        
        // Create tabs for different views
        TabPane tabPane = new TabPane();
        
        // Create facility status tab
        Tab statusTab = new Tab("Facility Status");
        statusTab.setContent(createFacilityStatusView());
        statusTab.setClosable(false);
        
        // Create maintenance schedule tab
        Tab maintenanceTab = new Tab("Maintenance Schedule");
        maintenanceTab.setContent(createMaintenanceScheduleView());
        maintenanceTab.setClosable(false);
        
        tabPane.getTabs().addAll(statusTab, maintenanceTab);
        mainLayout.setCenter(tabPane);
        
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    private VBox createFacilityStatusView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(10));
        
        Label titleLabel = new Label("Library Facilities");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create table for facilities
        TableView<Facility> facilitiesTable = createFacilitiesTable();
        
        // Create filter controls
        HBox filterBox = new HBox(15);
        filterBox.setPadding(new Insets(5));
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Available", "Under Maintenance", "Out of Service");
        statusFilter.setValue("All Statuses");
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadFacilities(facilitiesTable, statusFilter.getValue()));
        
        filterBox.getChildren().addAll(new Label("Filter by Status:"), statusFilter, refreshBtn);
        
        // Add components to view
        view.getChildren().addAll(titleLabel, filterBox, facilitiesTable);
        
        // Initial load
        loadFacilities(facilitiesTable, "All Statuses");
        
        return view;
    }
    
    private VBox createMaintenanceScheduleView() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(10));
        
        Label titleLabel = new Label("Maintenance Schedule");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create table for maintenance
        TableView<Maintenance> maintenanceTable = createMaintenanceTable();
        
        // Create filter controls
        HBox filterBox = new HBox(15);
        filterBox.setPadding(new Insets(5));
        
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusMonths(1));
        
        Button filterBtn = new Button("Filter");
        filterBtn.setOnAction(e -> {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            loadMaintenanceSchedule(maintenanceTable, start, end);
        });
        
        Button allBtn = new Button("Show All");
        allBtn.setOnAction(e -> loadMaintenanceSchedule(maintenanceTable, null, null));
        
        filterBox.getChildren().addAll(new Label("From:"), startDatePicker, 
                                     new Label("To:"), endDatePicker, filterBtn, allBtn);
        
        // Add components to view
        view.getChildren().addAll(titleLabel, filterBox, maintenanceTable);
        
        // Initial load
        loadMaintenanceSchedule(maintenanceTable, null, null);
        
        return view;
    }
    
    private TableView<Facility> createFacilitiesTable() {
        TableView<Facility> table = new TableView<>();
        
        // Create columns
        TableColumn<Facility, Integer> idCol = new TableColumn<>("Facility ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Facility, String> nameCol = new TableColumn<>("Facility Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Facility, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);
        
        TableColumn<Facility, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Facility, String> lastMaintenanceCol = new TableColumn<>("Last Maintenance");
        lastMaintenanceCol.setCellValueFactory(new PropertyValueFactory<>("lastMaintenance"));
        
        // Add columns to table
        table.getColumns().addAll(idCol, nameCol, typeCol, statusCol, lastMaintenanceCol);
        
        return table;
    }
    
    private TableView<Maintenance> createMaintenanceTable() {
        TableView<Maintenance> table = new TableView<>();
        
        // Create columns
        TableColumn<Maintenance, Integer> idCol = new TableColumn<>("Maintenance ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Maintenance, Integer> facilityIdCol = new TableColumn<>("Facility ID");
        facilityIdCol.setCellValueFactory(new PropertyValueFactory<>("facilityId"));
        
        TableColumn<Maintenance, String> facilityNameCol = new TableColumn<>("Facility Name");
        facilityNameCol.setCellValueFactory(new PropertyValueFactory<>("facilityName"));
        facilityNameCol.setPrefWidth(200);
        
        TableColumn<Maintenance, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(250);
        
        TableColumn<Maintenance, String> scheduledDateCol = new TableColumn<>("Scheduled Date");
        scheduledDateCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));
        
        TableColumn<Maintenance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Maintenance, Integer> librarianIdCol = new TableColumn<>("Librarian ID");
        librarianIdCol.setCellValueFactory(new PropertyValueFactory<>("librarianId"));
        
        // Add columns to table
        table.getColumns().addAll(idCol, facilityIdCol, facilityNameCol, descriptionCol, 
                               scheduledDateCol, statusCol, librarianIdCol);
        
        return table;
    }
    
    private void loadFacilities(TableView<Facility> table, String statusFilter) {
        ObservableList<Facility> facilities = FXCollections.observableArrayList();
        
        try {
            String query = 
                "SELECT f.FacilityID, f.FacilityName, f.Status, " +
                "(SELECT MAX(MaintenanceDate) FROM maintenancerecords WHERE FacilityID = f.FacilityID) as LastMaintenance " +
                "FROM libraryfacilities f";
                
            // Add status filter if needed
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                query += " WHERE f.Status = ?";
            }
            
            PreparedStatement stmt = con.prepareStatement(query);
            
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                stmt.setString(1, statusFilter);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("FacilityID");
                String name = rs.getString("FacilityName");
                String type = "Library Facility"; // Using a default since Type doesn't exist
                String status = rs.getString("Status");
                String lastMaintenance = rs.getString("LastMaintenance");
                
                if (lastMaintenance == null) {
                    lastMaintenance = "None";
                }
                
                facilities.add(new Facility(id, name, type, status, lastMaintenance));
            }
            
            DBLogger.log("INFO", "FacilityMonitor", "Loaded facilities with filter: " + statusFilter, username);
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "FacilityMonitor", "Error loading facilities: " + e.getMessage(), username);
            showAlert("Database Error", "Error loading facilities: " + e.getMessage());
            e.printStackTrace();
        }
        
        table.setItems(facilities);
    }
    
    private void loadMaintenanceSchedule(TableView<Maintenance> table, LocalDate startDate, LocalDate endDate) {
        ObservableList<Maintenance> maintenanceList = FXCollections.observableArrayList();
        
        try {
            StringBuilder queryBuilder = new StringBuilder(
                "SELECT m.RecordID as MaintenanceID, m.FacilityID, f.FacilityName, " +
                "m.Description, m.MaintenanceDate as ScheduledDate, f.LibrarianID " +
                "FROM maintenancerecords m " +
                "JOIN libraryfacilities f ON m.FacilityID = f.FacilityID"
            );
            
            // Add date filters if provided
            if (startDate != null && endDate != null) {
                queryBuilder.append(" WHERE m.MaintenanceDate BETWEEN ? AND ?");
            }
            
            queryBuilder.append(" ORDER BY m.MaintenanceDate");
            
            PreparedStatement stmt = con.prepareStatement(queryBuilder.toString());
            
            // Set date parameters if needed
            if (startDate != null && endDate != null) {
                stmt.setString(1, startDate.toString());
                stmt.setString(2, endDate.toString());
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("MaintenanceID");
                int facilityId = rs.getInt("FacilityID");
                String facilityName = rs.getString("FacilityName");
                String description = rs.getString("Description");
                String scheduledDate = rs.getString("ScheduledDate");
                int librarianId = rs.getInt("LibrarianID");
                
                // Determine status based on date since there's no Status column
                String status;
                if (scheduledDate != null) {
                    LocalDate scheduled = LocalDate.parse(scheduledDate);
                    if (scheduled.isBefore(LocalDate.now())) {
                        status = "Completed";
                    } else {
                        status = "Scheduled";
                    }
                } else {
                    status = "Unknown";
                }
                
                maintenanceList.add(new Maintenance(id, facilityId, facilityName, 
                                                  description, scheduledDate, status, librarianId));
            }
            
            DBLogger.log("INFO", "FacilityMonitor", "Loaded maintenance schedule", username);
            
        } catch (SQLException e) {
            DBLogger.log("ERROR", "FacilityMonitor", "Error loading maintenance: " + e.getMessage(), username);
            showAlert("Database Error", "Error loading maintenance schedule: " + e.getMessage());
            e.printStackTrace();
        }
        
        table.setItems(maintenanceList);
    }
    
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    // Facility class for TableView
    public static class Facility {
        private final int id;
        private final String name;
        private final String type;
        private final String status;
        private final String lastMaintenance;
        
        public Facility(int id, String name, String type, String status, String lastMaintenance) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.status = status;
            this.lastMaintenance = lastMaintenance;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getLastMaintenance() { return lastMaintenance; }
    }
    
    // Maintenance class for TableView
    public static class Maintenance {
        private final int id;
        private final int facilityId;
        private final String facilityName;
        private final String description;
        private final String scheduledDate;
        private final String status;
        private final int librarianId;
        
        public Maintenance(int id, int facilityId, String facilityName, 
                          String description, String scheduledDate, String status, int librarianId) {
            this.id = id;
            this.facilityId = facilityId;
            this.facilityName = facilityName;
            this.description = description;
            this.scheduledDate = scheduledDate;
            this.status = status;
            this.librarianId = librarianId;
        }
        
        public int getId() { return id; }
        public int getFacilityId() { return facilityId; }
        public String getFacilityName() { return facilityName; }
        public String getDescription() { return description; }
        public String getScheduledDate() { return scheduledDate; }
        public String getStatus() { return status; }
        public int getLibrarianId() { return librarianId; }
    }
}