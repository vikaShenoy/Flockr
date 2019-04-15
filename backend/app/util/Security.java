package util;


import models.Role;
import models.RoleType;
import models.User;
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
        return BCrypt.hashpw(password, BCrypt.gensalt());
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
     * @return String the token for authentication
     */
    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    /**
     * Checks if a user has a given role type
     * @return if the role exists or not
     */
    public boolean checkRoleExists(User user, RoleType roleType) {
        for (Role userRole : user.getRoles()) {
            if (userRole.getRoleType().equals(roleType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if an user has permission to run certain functionality
     * @param user The user object inferred by the middleware
     * @param comparedUser The user retrieved from the url parameter
     * @return True if the user
     */
    public boolean userHasPermission(User user, User comparedUser) {
        boolean isAdmin = checkRoleExists(user, RoleType.ADMIN);
        boolean isSuperAdmin = checkRoleExists(user, RoleType.SUPER_ADMIN);

        boolean comparedUserIsSuper = checkRoleExists(comparedUser, RoleType.SUPER_ADMIN);

        if (isSuperAdmin) {
            return true;
        } else if (isAdmin && comparedUserIsSuper) {
            return false;
        } else if (isAdmin) {
            return true;
        } else return user.getUserId() == comparedUser.getUserId();
    }

    /**
     * Checks if an admin has permission to run certain functionality
     * @param user The currently logged in user
     * @param comparedUser The user
     * @return boolean true if the user has permission
     */
    public boolean adminHasPermission(User user, User comparedUser) {
        boolean isAdmin = checkRoleExists(user, RoleType.ADMIN);
        boolean isSuperAdmin = checkRoleExists(user, RoleType.SUPER_ADMIN);

        boolean comparedUserIsSuperAdmin = checkRoleExists(comparedUser, RoleType.SUPER_ADMIN);

        if (!isAdmin && !isSuperAdmin) {
            return false;
        }

        return !isAdmin || !comparedUserIsSuperAdmin;
    }
}
