package util;

import models.Role;
import models.RoleType;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/** Contains security util functions */
public class Security {

  private Security() {}

  /**
   * Hashes a password
   *
   * @param password The password to hash
   * @return The hashed password
   */
  public static String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  /**
   * Compares a password to a hashed password
   *
   * @param password Password to compare
   * @param hash Hash to compare
   * @return true if the passwords are the same, false otherwise
   */
  public static boolean comparePasswordAndHash(String password, String hash) {
    return BCrypt.checkpw(password, hash);
  }

  /**
   * Generates a token to be used for authentication
   *
   * @return String the token for authentication
   */
  public static String generateToken() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[20];
    random.nextBytes(bytes);
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString(bytes);
  }

  /**
   * Checks if a user has a given role type
   *
   * @return if the role exists or not
   */
  public static boolean checkRoleExists(User user, RoleType roleType) {
    for (Role userRole : user.getRoles()) {
      if (userRole.getRoleType().equals(roleType.name())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if an user has permission to run certain functionality
   *
   * @param userFromMiddleware The user object inferred by the middleware
   * @param userIdFromUrl The user retrieved from the url parameter
   * @return False if the user from middleware is the same as the user provided in the url
   */
  public static boolean userHasPermission(User userFromMiddleware, int userIdFromUrl) {
    if (checkRoleExists(userFromMiddleware, RoleType.ADMIN)
        || checkRoleExists(userFromMiddleware, RoleType.SUPER_ADMIN)) {
      return false;
    }

    return userFromMiddleware.getUserId() != userIdFromUrl;
  }
}
