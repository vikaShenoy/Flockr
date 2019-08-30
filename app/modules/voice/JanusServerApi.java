package modules.voice;

import com.fasterxml.jackson.databind.JsonNode;
import modules.voice.Requests.ExistsRequest;
import modules.voice.Requests.JanusMessage;
import modules.voice.Requests.JanusVoiceRequest;
import modules.voice.Requests.JanusMessage;
import modules.voice.Requests.JanusVoiceRequest;
import play.libs.Json;
import play.libs.ws.WSClient;
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
  public int generateRoom(String token) {
    return 0;
  }

  /**
   * Queries the janus server and checks if a room ID exists
   * @param roomId The room ID to check
   * @return True if the room exists, false otherwise
   */
  @Override
  public boolean checkRoomExists(int roomId, int sessionId, int pluginHandle) {
    ExistsRequest existsRequest = new ExistsRequest(roomId);
    sendRequest(existsRequest, sessionId, pluginHandle);
    return true;
  }

  private void sendRequest(JanusVoiceRequest request, int sessionId, int pluginHandle) {
    // Is the message that will be sent in the request body
    JanusMessage janusMessage = new JanusMessage(request);
    JsonNode requestBody = Json.toJson(janusMessage);
    String endpointUrl = String.format("%s/janus/%d/%d", janusServerUrl, sessionId, pluginHandle);
    wsClient.url(endpointUrl).post(requestBody)
            .thenApplyAsync(response -> {
              System.out.println(response.getStatus());
              return null;
            });
  }


}
