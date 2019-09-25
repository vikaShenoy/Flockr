package modules.voice;

import com.fasterxml.jackson.databind.JsonNode;
import modules.voice.Requests.*;
import modules.voice.Requests.JanusMessage;
import modules.voice.Requests.JanusVoiceRequest;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class JanusServerApi implements VoiceServerApi {
  private static final String JANUS_SERVER_URL = "http://csse-s302g5.canterbury.ac.nz:8088";

  private final WSClient wsClient;

  @Inject
  public JanusServerApi(WSClient wsClient) {
    this.wsClient = wsClient;
  }



  @Override
  public CompletionStage<Long> generateRoom(String token, long sessionId, long pluginHandle) {
    CreateRequest createRequest = new CreateRequest(token);
    return sendRequest(createRequest, sessionId, pluginHandle).thenApplyAsync(
            jsonResponse -> jsonResponse.get("plugindata").get("data").get("room").asLong());
  }

  /**
   * Queries the janus server and checks if a room ID exists
   * @param roomId The room ID to check
   * @return True if the room exists, false otherwise
   */
  @Override
  public CompletionStage<Boolean> checkRoomExists(long roomId, long sessionId, long pluginHandle) {
    ExistsRequest existsRequest = new ExistsRequest(roomId);
    return sendRequest(existsRequest, sessionId, pluginHandle)
            .thenApplyAsync(jsonResponse -> jsonResponse.get("plugindata").get("data").get("exists").asBoolean());
  }

  /**
   * Sends a request to the janus server
   * @param request The request to send (e.g. does a room exist and create room)
   * @param sessionId the id of the session.
   * @param pluginHandle the handle of the plugin.
   * @return the response from the janus server.
   */
  private CompletionStage<JsonNode> sendRequest(JanusVoiceRequest request, long sessionId, long pluginHandle) {
    // Is the message that will be sent in the request body
    JanusMessage janusMessage = new JanusMessage(request);
    JsonNode requestBody = Json.toJson(janusMessage);
    String endpointUrl = String.format("%s/janus/%d/%d", JANUS_SERVER_URL, sessionId, pluginHandle);
    return wsClient.url(endpointUrl).post(requestBody)
            .thenApplyAsync(WSResponse::asJson);
  }
}
