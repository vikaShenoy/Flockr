package testingUtilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;

/**
 * Utility class used for testing backend server responses
 */
public class PlayResultToJson {

    /**
     * Return a JsonNode from a Play Result object
     * @param result the play result we are turning into JSON
     * @return the JSON representation of the Play Result body
     * @throws IOException when can't convert the given Play Result to JSON
     */
    public static JsonNode convertResultToJson(Result result) throws IOException {
        String jsonAsString = Helpers.contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonAsString);
        return json;
    }
}
