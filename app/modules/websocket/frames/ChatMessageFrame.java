package modules.websocket.frames;

import models.User;

/**
 * Websocket frame for a message to be sent to a chat group.
 */
public class ChatMessageFrame implements Frame {

  private int chatGroupId;
  private String message;
  private User sender;
  private int messageId;

  public ChatMessageFrame(int chatGroupId, String message, User sender, int messageId) {
    this.chatGroupId = chatGroupId;
    this.message = message;
    this.sender = sender;
    this.messageId = messageId;
  }

  public int getChatGroupId() {
    return chatGroupId;
  }

  public int getMessageId() {return messageId;}

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
