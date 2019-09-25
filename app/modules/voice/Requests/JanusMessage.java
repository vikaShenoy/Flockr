package modules.voice.Requests;

import util.Security;

/**
 * Represents an abstract message
 */
public class JanusMessage {
   /**
   * Janus property specifies the type of request, in this case message
   */
  private static final String MESSAGE_TYPE = "message";

  /**
   * Transaction is used to clients know what responses correspond with what request
   */
  private String transaction;

  private JanusVoiceRequest voiceRequest;

  public JanusMessage(JanusVoiceRequest voiceRequest) {
    this.voiceRequest = voiceRequest;
    this.transaction = Security.generateToken();
  }


  public String getJanus() {
    return MESSAGE_TYPE;
  }

  public String getTransaction() {
    return transaction;
  }

  public JanusVoiceRequest getBody() {
    return voiceRequest;
  }
}
