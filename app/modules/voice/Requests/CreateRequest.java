package modules.voice.Requests;

import java.util.ArrayList;
import java.util.List;

public class CreateRequest implements JanusVoiceRequest {

    private final boolean is_private = true;
    private List<String> allowed = new ArrayList<>();



    public CreateRequest(String token) {
        this.allowed.add(token);
    }

    @Override
    public String getRequest() {
        return "create";
    }

    public boolean isIs_private() {
        return is_private;
    }

    public List<String> getAllowed() {
        return allowed;
    }
}
