package actions;

import models.User;
import play.libs.typedmap.TypedKey;

/**
 * Manages objects that can be passed from middleware to controllers
 */
public class ActionState {

    private ActionState() {
        throw new IllegalStateException("Utility class");
    }

    public static final TypedKey<User> USER = TypedKey.create("user");

}
