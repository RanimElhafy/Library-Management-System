package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class hashing {

    private String password;
    private String salt;

    // Constructor with random salt
    public hashing(String password) {
        this.password = password;
        this.salt = createSalt();
    }

    // Constructor with provided salt
    public hashing(String password, String salt) {
        this.password = password;
        this.salt = salt;
    }

    // Generates new hash with random salt
    public String[] generateHash() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt.getBytes());
        byte[] hash = digest.digest(password.getBytes());
        return new String[]{bytesToHex(hash), salt};
    }

    // Generates hash with provided salt
    public String generateHashWithSalt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt.getBytes());
        byte[] hash = digest.digest(password.getBytes());
        return bytesToHex(hash);
    }

    // For testing
    public static void main(String[] args) {
        try {
            String testPassword = "libpass";
            hashing hasher = new hashing(testPassword);
            String[] result = hasher.generateHash();

            System.out.println("Hashed Password: " + result[0]);
            System.out.println("Salt: " + result[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String createSalt() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
