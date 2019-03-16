package actions;

import models.User;
import play.libs.typedmap.TypedKey;

public class ActionState {
    public static final TypedKey<User> USER = TypedKey.create("user");
}
