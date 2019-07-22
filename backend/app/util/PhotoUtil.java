package util;

import java.util.UUID;

/**
 * Contains utility methods for photo's endpoint
 */
public class PhotoUtil {

    /**
     *
     * @return a random string to be used as a photo name
     */
    public String generatePhotoName() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }


}
