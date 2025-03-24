package common;

public class HashGenerator {
    public static void main(String[] args) {
        try {
            String password = "libpass"; // Change this as needed
            hashing hasher = new hashing(password);
            String[] result = hasher.generateHash();

            System.out.println("Hashed Password: " + result[0]);
            System.out.println("Salt: " + result[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
