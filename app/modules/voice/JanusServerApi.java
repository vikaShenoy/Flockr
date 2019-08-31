package modules.voice;

import com.fasterxml.jackson.databind.JsonNode;
import modules.voice.Requests.*;
import modules.voice.Requests.JanusMessage;
import modules.voice.Requests.JanusVoiceRequest;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class JanusServerApi implements VoiceServerApi {
  private final String janusServerUrl = "http://localhost:8088";

  private final WSClient wsClient;

  @Inject
  public JanusServerApi(WSClient wsClient) {
    this.wsClient = wsClient;
  }



  @Override
  public int generateRoom(String token, long sessionId, long pluginHandle) {

    CreateRequest createRequest = new CreateRequest(token);
    sendRequest(createRequest, sessionId, pluginHandle);

    return 1;

  }

  /**
   * Queries the janus server and checks if a room ID exists
   * @param roomId The room ID to check
   * @return True if the room exists, false otherwise
   */
  @Override
  public boolean checkRoomExists(long roomId, long sessionId, long pluginHandle) {
    ExistsRequest existsRequest = new ExistsRequest(roomId);
    sendRequest(existsRequest, sessionId, pluginHandle);
//    boolean roomExists = jsonResponse.get("plugindata").get("data").get("exists").asBoolean();
    return true;
  }

  private CompletionStage<JsonNode> sendRequest(JanusVoiceRequest request, long sessionId, long pluginHandle) {
    // Is the message that will be sent in the request body
    JanusMessage janusMessage = new JanusMessage(request);
    JsonNode requestBody = Json.toJson(janusMessage);
    String endpointUrl = String.format("%s/janus/%d/%d", janusServerUrl, sessionId, pluginHandle);
    return wsClient.url(endpointUrl).post(requestBody)
            .thenApplyAsync(WSResponse::asJson);
  }
}
