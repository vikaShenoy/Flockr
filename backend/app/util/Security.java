package util;


import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Contains security util functions
 */
public class Security {
    /**
     * Hashes a password
     * @param password The password to hash
     * @return The hashed password
     */
    public String hashPassword(String password) {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        return passwordHash;
    }

    /**
     * Compares a password to a hashed password
     * @param password Password to compare
     * @param hash Hash to compare
     * @return true if the passwords are the same, false otherwise
     */
    public boolean comparePasswordAndHash(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    /**
     * Generates a token to be used for authentication
     * @return
     */
    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);
        return token;
    }

}
