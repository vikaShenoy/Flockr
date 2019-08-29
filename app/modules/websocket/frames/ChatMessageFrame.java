package modules.websocket.frames;

/**
 * Websocket frame for a message to be sent to a chat group.
 */
public class ChatMessageFrame implements Frame {

  private int chatGroupId;
  private String message;
  private int senderId;

  public ChatMessageFrame(int chatGroupId, String message, int senderId) {
    this.chatGroupId = chatGroupId;
    this.message = message;
    this.senderId = senderId;
  }

  public int getChatGroupId() {
    return chatGroupId;
  }

  public String getMessage() {
    return message;
  }

  public int getSenderId() {
    return senderId;
  }

  @Override
  public String getType() {
    return "send-chat-message";
  }
}
