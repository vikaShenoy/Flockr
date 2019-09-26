package modules.websocket.frames;

/**
 * Frame sent back from the server to keep the connection open
 */
public class PongFrame implements Frame {
  @Override
  public String getType() {
    return "pong";
  }
}
