package modules.websocket.frames;

/**
 * Websocket frame for notifying users of a ping-map event.
 */
public class PingMapFrame implements Frame {

  private int tripNodeId;
  private double latitude;
  private double longitude;

  public PingMapFrame(int tripNodeId, int latitude, int longitude) {
    this.tripNodeId = tripNodeId;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public int getTripNodeId() {
    return tripNodeId;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  @Override
  public String getType() {
    return "ping-map";
  }
}
