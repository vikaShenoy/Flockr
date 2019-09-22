package modules.websocket.frames;

import models.Message;

/**
 * Websocket frame for when a message in a chat group is deleted.
 */
public class DeletedChatMessageFrame implements Frame {

  private Message message;

  public DeletedChatMessageFrame(Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }

  @Override
  public String getType() {
    return "delete-chat-message";
  }
}
