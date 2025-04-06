package librarian;

import common.DBLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MemberRegistrar {
    private Connection con;
    private String name;
    private String contact;
    private String regDate;
    private LocalDate expiryDate;
    private int memberID;
    private int planID;
    private int facilityID;
    private String membershipType;
    private String username;
    private MemberUtils libraryFunctions;

    public MemberRegistrar(Connection con, String name, String contact, String regDate, LocalDate expiryDate,
                             int memberID, int planID, int facilityID, String membershipType, String username) {
        this.con = con;
        this.name = name;
        this.contact = contact;
        this.regDate = regDate;
        this.expiryDate = expiryDate;
        this.memberID = memberID;
        this.planID = planID;
        this.facilityID = facilityID;
        this.membershipType = membershipType;
        this.username = username;
        this.libraryFunctions = new MemberUtils(con, username);
    }

    public void registerMember() throws SQLException {
        try {
            String query = "INSERT INTO members (MemberID, Name, ContactInfo, MembershipType, RegistrationDate, MembershipExpiry, RoleID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, memberID);
            statement.setString(2, name);
            statement.setString(3, contact);
            statement.setString(4, membershipType);
            statement.setString(5, regDate);
            statement.setString(6, expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            statement.setInt(7, 3); // RoleID for Member

            int rs = statement.executeUpdate();
            if (rs == 1) {
                DBLogger.log("INFO", "AfterRegistration", "New member inserted into members table.", username);
            }
            libraryFunctions.showConfirm("Registration Successful", "New library member has been registered.");
        } catch (SQLException e) {
            DBLogger.log("ERROR", "AfterRegistration", "Failed to register library member.", username);
            e.printStackTrace();
            libraryFunctions.showAlert("Database Error", "Registration failed. Please try again.");
        }
    }

    public void renewMembership() throws SQLException {
        try {
            String updateQuery = "UPDATE members SET MembershipExpiry = ?, MembershipType = ? WHERE MemberID = ?";
            PreparedStatement updateStatement = con.prepareStatement(updateQuery);
            updateStatement.setString(1, expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            updateStatement.setString(2, membershipType);
            updateStatement.setInt(3, memberID);

            int rs = updateStatement.executeUpdate();
            if (rs > 0) {
                DBLogger.log("INFO", "AfterRegistration", "Membership renewed in members table.", username);
            }
            libraryFunctions.showConfirm("Membership Renewed", "Membership renewed successfully.");
        } catch (SQLException ex) {
            DBLogger.log("ERROR", "AfterRegistration", "Membership renewal failed.", username);
            ex.printStackTrace();
            libraryFunctions.showAlert("Database Error", "Renewal failed. Please try again.");
        }
    }

    public void assignFacility() throws SQLException {
        try {
            String query = "INSERT INTO libraryfacilities (FacilityID, FacilityName, Status, LibrarianID) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, facilityID);
            statement.setString(2, "Assigned Facility for MemberID " + memberID);
            statement.setString(3, "Available");
            statement.setInt(4, 2); // Default librarian assigned

            int rs = statement.executeUpdate();
            if (rs == 1) {
                DBLogger.log("INFO", "AfterRegistration", "Facility assigned for member.", username);
            }
            libraryFunctions.showConfirm("Facility Assigned", "Facility assigned successfully.");
        } catch (SQLException e) {
            DBLogger.log("ERROR", "AfterRegistration", "Facility assignment failed.", username);
            e.printStackTrace();
            libraryFunctions.showAlert("Database Error", "Facility assignment failed.");
        }
    }
}
