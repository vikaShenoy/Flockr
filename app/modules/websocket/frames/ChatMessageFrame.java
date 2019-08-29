package modules.websocket.frames;

import models.User;

/**
 * Websocket frame for a message to be sent to a chat group.
 */
public class ChatMessageFrame implements Frame {

  private int chatGroupId;
  private String message;
  private User sender;

  public ChatMessageFrame(int chatGroupId, String message, User sender) {
    this.chatGroupId = chatGroupId;
    this.message = message;
    this.sender = sender;
  }

  public int getChatGroupId() {
    return chatGroupId;
  }

  public String getMessage() {
    return message;
  }

  public User getSender() {
    return sender;
  }

  @Override
  public String getType() {
    return "send-chat-message";
  }
}
