package util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class Responses {
    public ObjectNode successJson() {
        ObjectNode jsonSuccess = Json.newObject();
        jsonSuccess.put("response", "success");
        return jsonSuccess;
    }


    /**
     * Sent by the endpoint where the result was an error
     * @return The error json
     */
    public ObjectNode errorJson() {
        ObjectNode jsonError = Json.newObject();
        jsonError.put("response", "error");
        return jsonError;
    }

    /**
     * Sent by the endpoint where the result was an error and with a
     * customizes error message
     * @param error The custom error message
     * @return The error json with customised message
     */
    public ObjectNode errorJson(String error) {
        ObjectNode jsonError = errorJson();
        jsonError.put("error", error);
        return jsonError;
    }

}
