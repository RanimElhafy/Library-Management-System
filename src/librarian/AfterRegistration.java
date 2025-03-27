package librarian;

import common.DBLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AfterRegistration {
    private Connection con;
    private String name;
    private String contact;
    private String regDate;
    private LocalDate expiryDate;
    private int memberID;
    private int planID;
    private int sectionID;
    private String membershipType;
    private String username;
    private LibraryFunctions libraryFunctions;

    public AfterRegistration(Connection con, String name, String contact, String regDate, LocalDate expiryDate,
                             int memberID, int planID, int sectionID, String membershipType, String username) {
        this.con = con;
        this.name = name;
        this.contact = contact;
        this.regDate = regDate;
        this.expiryDate = expiryDate;
        this.memberID = memberID;
        this.planID = planID;
        this.sectionID = sectionID;
        this.membershipType = membershipType;
        this.username = username;
        this.libraryFunctions = new LibraryFunctions(con, username);
    }

    public void registerMember() throws SQLException {
        try {
            String query = "INSERT INTO library_members (Name, ContactInformation, MembershipType, RegistrationDate, MembershipExpiry) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, contact);
            statement.setString(3, membershipType);
            statement.setString(4, regDate);
            statement.setString(5, expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            int rs = statement.executeUpdate();
            if (rs == 1) {
                DBLogger.log("INFO", "AfterRegistration", "New member registered in library_members.", username);
            }
            libraryFunctions.showConfirm("Registration Successful", "New library member has been registered.");
        } catch (SQLException e) {
            e.printStackTrace();
            DBLogger.log("ERROR", "AfterRegistration", "Failed to register library member.", username);
            libraryFunctions.showAlert("Database Error", "Registration failed. Please try again.");
        }
    }

    public void renewMembership() throws SQLException {
        try {
            String updateQuery = "UPDATE library_members SET MembershipExpiry = ?, MembershipType = ? WHERE MemberID = ?";
            PreparedStatement updateStatement = con.prepareStatement(updateQuery);
            updateStatement.setString(1, expiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            updateStatement.setString(2, membershipType);
            updateStatement.setInt(3, memberID);
            int rs = updateStatement.executeUpdate();
            if (rs > 0) {
                DBLogger.log("INFO", "AfterRegistration", "Membership renewed in library_members.", username);
            }
            libraryFunctions.showConfirm("Membership Renewed", "Membership renewed successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            DBLogger.log("ERROR", "AfterRegistration", "Membership renewal failed.", username);
            libraryFunctions.showAlert("Database Error", "Renewal failed. Please try again.");
        }
    }

    public void assignPlan() throws SQLException {
        try {
            String query = "INSERT INTO member_plan (member_id, plan_id, section_id) VALUES (?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, memberID);
            if (planID == 0) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, planID);
            }
            statement.setInt(3, sectionID);

            int rs = statement.executeUpdate();
            if (rs == 1) {
                DBLogger.log("INFO", "AfterRegistration", "Plan assigned to member in member_plan table.", username);
            }
            libraryFunctions.showConfirm("Plan Assigned", "Plan assigned successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            DBLogger.log("ERROR", "AfterRegistration", "Failed to assign plan to member.", username);
            libraryFunctions.showAlert("Database Error", "Plan assignment failed. Please try again.");
        }
    }
}
