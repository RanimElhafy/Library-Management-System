package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class hashing {
    private String pass;
    private String salt = createSalt();
    public hashing(String pass){
        this.pass = pass;
    }

    public String[] generateHash() throws NoSuchAlgorithmException{
        MessageDigest digest  = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt.getBytes());
        byte[] hash = digest.digest(pass.getBytes());
        String[] hashList = new String[2];
        hashList[0] = bytesToStringHex(hash);
        hashList[1] = salt;
        return hashList;
    }

    public String generateHash(String salt) throws NoSuchAlgorithmException{
        MessageDigest digest  = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(salt.getBytes());
        byte[] hash = digest.digest(pass.getBytes());

        return bytesToStringHex(hash);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String bytesToStringHex(byte[] bytes){
        char[] hexChars = new char[bytes.length * 2];
        for (int j=0;j<bytes.length;j++){
            int v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v >> 4];
            hexChars[j* 2+1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String createSalt(){
        byte[] bytes = new byte[20];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytesToStringHex(bytes);
    }


}