package util;

import java.util.UUID;

/**
 * Contains utility methods for photo's endpoint
 */
public class PhotoUtil {

    // TODO: this javadoc needs finishing
    /**
     *
     * @return
     */
    public String generatePhotoName() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }


}
