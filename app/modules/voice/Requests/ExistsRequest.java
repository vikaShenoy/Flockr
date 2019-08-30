package modules.voice.Requests;

/**
 * Queries the voice server and checks if a chat room exists
 */
public class ExistsRequest implements JanusVoiceRequest {
  private final String request = "exists";
  private int roomId;

  public ExistsRequest(int roomId) {
    this.roomId = roomId;
  }

  @Override
  public String getRequest() {
    return request;
  }

  public int getRoomId() {
    return roomId;
  }
}
