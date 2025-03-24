package administrator;

import java.util.*;

public class Administrator {
    private String adminId;
    private String name;

    public Administrator(String adminId, String name) {
        this.adminId = adminId;
        this.name = name;
    }

    public void addUser(User user, List<User> users) {
        users.add(user);
        System.out.println("User added: " + user.getUsername());
    }

    public void lockUser(User user) {
        user.setLocked(true);
        System.out.println("User locked: " + user.getUsername());
    }

    public void deleteUser(User user, List<User> users) {
        users.remove(user);
        System.out.println("User deleted: " + user.getUsername());
    }

    public void enforcePolicy(SecurityPolicy policy) {
        System.out.println("Policy enforced: " + policy.getDescription());
    }

    public void viewLogs(List<String> logs) {
        System.out.println("System Logs:");
        logs.forEach(System.out::println);
    }
}
