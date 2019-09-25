package util;

/**
 * Utility functions used by the authorisation controller.
 */
public class AuthUtil {

    private AuthUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * A function that checks if the given string contains all alphabet letters. If yes, it returns true.
     * Otherwise, return false.
     * @param name The name of the User
     * @return true or false depending on the content of the string
     */
    public static boolean isAlpha(String name) {
        return name.matches("[a-zA-Z ]+");
    }

    /**
     * A function that checks if the given email is a valid email format. If yes, it returns true.
     * Otherwise, returns false.
     * @param email
     * @return
     */
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}
