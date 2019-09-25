package modules.voice.Requests;

/**
 * Queries the voice server and checks if a chat room exists
 */
public class ExistsRequest implements JanusVoiceRequest {

  private long roomId;

  public ExistsRequest(long roomId) {
    this.roomId = roomId;
  }

  @Override
  public String getRequest() {
    return "exists";
  }

  public long getRoom() {
    return roomId;
  }
}
