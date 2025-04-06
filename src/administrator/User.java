package administrator;

public class User {
    private String username;
    private boolean locked;
    private String role;

    public User(String username) {
        this.username = username;
        this.locked = false;
        this.role = "member"; // Default role
    }

    public User(String username, String role, boolean locked) {
        this.username = username;
        this.role = role;
        this.locked = locked;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return username + (locked ? " [LOCKED]" : "") + " - " + role;
    }
};