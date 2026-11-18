
package main.java.utils;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final SecureRandom RANDOM = new SecureRandom();

    // Generate a random salt for each password
    public static String generateSalt() {

        byte[] salt = new byte[SALT_LENGTH];

        RANDOM.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    // Convert password into a secure hash
    public static String hashPassword(String password, String salt) {

        try {

            byte[] saltBytes = Base64.getDecoder().decode(salt);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Password hashing failed.", e);
        }
    }

    // Verify entered password against the stored hash
    public static boolean verifyPassword(
            String enteredPassword,
            String storedHash,
            String storedSalt) {

        String generatedHash = hashPassword(
                enteredPassword,
                storedSalt);

        byte[] generatedBytes = Base64.getDecoder().decode(generatedHash);

        byte[] storedBytes = Base64.getDecoder().decode(storedHash);

        return java.security.MessageDigest.isEqual(
                generatedBytes,
                storedBytes);
    }
}
