package repository;

import models.ChatGroup;

import javax.inject.Inject;
import java.util.List;
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

}
