package test;

import java.security.SecureRandom;
import java.util.Base64;

public class TestClass {
    public static String URL = "https://discord.com/api/webhooks/1404257678276038767/TpjNAdTeL7LEqVdLZmwzr-QaQ9uUGdYB1Z_eFSjXXTuLcZ64cDwv1YKs2JRzFbzLpUKF";
    
    // Key generator that produces a random Base64-encoded key of the same length as URL
    public static String keyGenerator(String url) {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[url.length()];
        
        // Generate a random key of the same length as the URL
        random.nextBytes(keyBytes);
        
        // Base64-encode the generated key
        return Base64.getEncoder().encodeToString(keyBytes);
    }
    
    // Encrypt the URL with the generated key (XOR operation)
    public static String encrypt(String url, String key) {
        byte[] urlBytes = url.getBytes();
        byte[] keyBytes = Base64.getDecoder().decode(key);
        
        byte[] encrypted = new byte[urlBytes.length];
        
        for (int i = 0; i < urlBytes.length; i++) {
            encrypted[i] = (byte) (urlBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        // Return the encrypted byte array as Base64-encoded string
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    // Decrypt the URL with the same key (XOR operation)
    public static String decrypt(String encryptedUrl, String key) {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedUrl);
        byte[] keyBytes = Base64.getDecoder().decode(key);
        
        byte[] decrypted = new byte[encryptedBytes.length];
        
        for (int i = 0; i < encryptedBytes.length; i++) {
            decrypted[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        
        // Convert the decrypted byte array into a string and return
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        // Generate the key based on the length of the URL and Base64 encode it
        String key = keyGenerator(URL);
        
        // Encrypt the URL using OTP and encode the result in Base64
        String encrypted = encrypt(URL, key);
        
        // Decrypt the URL from Base64-encoded encrypted data
        String decrypted = decrypt(encrypted, key);
        
        // Output the key, encrypted URL (Base64), and decrypted URL
        System.out.println("Key (Base64): " + key);  // Printing the Base64-encoded key
        System.out.println("Encrypted (Base64): " + encrypted);
        System.out.println("Decrypted: " + decrypted);  // Decrypted URL
    }
}
