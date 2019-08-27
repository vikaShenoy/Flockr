package repository;

import models.ChatGroup;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ChatRepository {
  private final DatabaseExecutionContext executionContext;


  @Inject
  public ChatRepository(DatabaseExecutionContext databaseExecutionContext) {
    this.executionContext = databaseExecutionContext;
  }

  public CompletionStage<ChatGroup> create(ChatGroup chatGroup) {
    return supplyAsync(() -> {
      chatGroup.insert();
      return chatGroup;
    }, executionContext);
  }

  public CompletionStage<ChatGroup> getChatById(int chatGroupId) {
    return supplyAsync(() -> ChatGroup.find.byId(chatGroupId), executionContext);
  }
}
