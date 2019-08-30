package repository;

import io.ebean.PagedList;
import models.ChatGroup;
import models.Message;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ChatRepository {
  private final DatabaseExecutionContext executionContext;


  @Inject
  public ChatRepository(DatabaseExecutionContext databaseExecutionContext) {
    this.executionContext = databaseExecutionContext;
  }

  /**
   * Creates a chat group
   * @param chatGroup The chat group to create
   * @return A completion stage wrapped with the created chat group
   */
  public CompletionStage<ChatGroup> create(ChatGroup chatGroup) {
    return supplyAsync(() -> {
      chatGroup.insert();
      return chatGroup;
    }, executionContext);
  }

  /**
   * Modify a chat group
   * @param chatGroup The modified chat group
   * @return A completion stage wrapped with the modified chat group
   */
  public CompletionStage<ChatGroup> modify(ChatGroup chatGroup) {
    return supplyAsync(() -> {
      chatGroup.save();
      return chatGroup;
    }, executionContext);
  }

  /**
   * Gets a chat by it's ID
   * @param chatGroupId The chat group ID to get
   * @return A completion stage wrapped with the retrieved chat group
   */
  public CompletionStage<ChatGroup> getChatById(int chatGroupId) {
    return supplyAsync(() -> ChatGroup.find.byId(chatGroupId), executionContext);
  }

  /**
   * Gets all chats that are associated with a user
   * @param userId The user to get the chats from
   * @return A completion stage wrapped with the chats
   */
  public CompletionStage<List<ChatGroup>> getChatsByUserId(int userId) {
    return supplyAsync(() -> ChatGroup.find.query().fetch("users").where().eq("users.userId", userId).findList(), executionContext);
  }

  /**
   * Deletes a chat group by it's chat group ID
   * @param chatGroup The chat group to delete
   * @return An empty completion stage
   */
  public CompletionStage<Void> deleteChatGroupById(ChatGroup chatGroup) {
    return supplyAsync(() -> {
      chatGroup.delete();
      return null;
    }, executionContext);
  }

  /**
   * Adds a message to a chat group
   * @param message The message to add
   * @return A completion stage with the updated message
   */
  public CompletionStage<Message> createMessage(Message message) {
    return supplyAsync(() -> {
      message.insert();
      return message;
    }, executionContext);
  }

  /**
   * Get all messages in a chat between a certain range
   * @param chatGroupId the id of the chat to get messages for
   * @param offset the number of rows to skip in the query
   * @param limit the maximum number of rows to return
   * @return the list of messages
   */
  public CompletionStage<List<Message>> getMessages(int chatGroupId, int offset, int limit) {

    List<Message> messages = Message.find.query()
            .where()
            .eq("chat_group_chat_group_id", chatGroupId)
            .orderBy().desc("timestamp")
            .setFirstRow(offset)
            .setMaxRows(limit)
            .findList();
    Collections.reverse(messages);

    return supplyAsync(() -> messages);
  }

  public CompletionStage<Optional<Message>> getMessageById(int messageId) {
    return supplyAsync(() -> Message
            .find
            .query().where()
            .eq("messageId", messageId).findOneOrEmpty(),
            executionContext);
  }

  /**
   * Deletes a message
   * @param message The message to delete
   * @return The deleted message
   */
  public CompletionStage<Message> deleteMessage(Message message) {
    return supplyAsync(() -> {
      message.delete();
      return message;
    }, executionContext);
  }

}
