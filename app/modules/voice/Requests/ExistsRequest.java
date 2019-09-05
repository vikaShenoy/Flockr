package modules.voice.Requests;

/**
 * Queries the voice server and checks if a chat room exists
 */
public class ExistsRequest implements JanusVoiceRequest {
  private final String request = "exists";
  private long roomId;

  public ExistsRequest(long roomId) {
    this.roomId = roomId;
  }

  @Override
  public String getRequest() {
    return request;
  }

  public long getRoom() {
    return roomId;
  }
}
