package administrator;

public class User {
    private String username;
    private boolean isLocked;

    public User(String username) {
        this.username = username;
        this.isLocked = false;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    @Override
    public String toString() {
        return username + " (Locked: " + isLocked + ")";
    }
}
