package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import models.ChatGroup;
import models.Message;
import models.User;
import modules.voice.JanusServerApi;
import modules.voice.VoiceServerApi;
import modules.websocket.ChatEvents;
import modules.websocket.ConnectedUsers;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.ChatRepository;
import util.ChatUtil;
import util.Security;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ChatController extends Controller {

  private final ChatUtil chatUtil;
  private final ChatRepository chatRepository;
  private HttpExecutionContext httpExecutionContext;
  private VoiceServerApi voiceServerApi;

  @Inject
  public ChatController(
      ChatUtil chatUtil,
      ChatRepository chatRepository,
      HttpExecutionContext httpExecutionContext,
      VoiceServerApi voiceServerApi) {
    this.chatUtil = chatUtil;
    this.chatRepository = chatRepository;
    this.httpExecutionContext = httpExecutionContext;
    this.voiceServerApi = voiceServerApi;
  }

  /**
   * Creates a group chat
   *
   * @param request
   * @return One of the following statuses - 201 - Group was successfully created - 400 - Problem
   *     with request body (invalid users, no name) - 401 - User was not authenticated successfully
   *     - 403 - User tries to add themselves to their own chat group - 500 - Any other interval
   *     server error (db/network error etc.)
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> createChat(Http.Request request) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    JsonNode jsonBody = request.body().asJson();

    String chatName;
    List<User> users;

    try {
      chatName = jsonBody.get("name").asText();
      JsonNode userIdsJson = jsonBody.get("userIds");
      users = chatUtil.transformUsersFromJson(userFromMiddleware.getUserId(), userIdsJson);
    } catch (NullPointerException | BadRequestException e) {
      return supplyAsync(() -> badRequest("Invalid body"));
    } catch (ForbiddenRequestException e) {
      return supplyAsync(() -> forbidden("Tried to add self to chat group"));
    }

    ChatGroup chatGroup = new ChatGroup(chatName, users, new ArrayList<>());

    return chatRepository
        .createChat(chatGroup)
        .thenComposeAsync(
            insertedChatGroup -> chatRepository.getChatById(insertedChatGroup.getChatGroupId()),
            httpExecutionContext.current())
        .thenApplyAsync(
            populatedChatGroup -> {
              if (populatedChatGroup == null) {
                return notFound();
              } else {
                return created(Json.toJson(populatedChatGroup));
              }
            },
            httpExecutionContext.current())
        .exceptionally(e -> internalServerError());
  }

  /**
   * Edit a group chat
   *
   * @param request the play request object with a JSON body
   * @param chatGroupId the id of the group chat to edit
   * @return One of the following - 200 - Chat was successfully edited - 401 - User not
   *     authenticated / logged in - 403 - User is not allowed to edit the group chat (not a member
   *     of the chat) - 404 - Chat group doesn't exist - 400 - Body of request was invalid - 500 -
   *     Any other internal server error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> editChat(Http.Request request, int chatGroupId) {

    User userFromMiddleware = request.attrs().get(ActionState.USER);
    JsonNode jsonBody = request.body().asJson();

    return chatRepository
        .getChatById(chatGroupId)
        .thenComposeAsync(
            chatGroup -> {
              if (chatGroup == null) {
                throw new CompletionException(new NotFoundException("Chat not found"));
              }

              if (!chatUtil.userInGroup(chatGroup.getUsers(), userFromMiddleware)
                  && !userFromMiddleware.isAdmin()) {
                throw new CompletionException(new ForbiddenRequestException("User not in group"));
              }

              String chatName;
              List<User> users;

              try {
                chatName = jsonBody.get("name").asText();
                JsonNode userIdsJson = jsonBody.get("userIds");
                users = chatUtil.transformUsersFromJson(userIdsJson);
              } catch (NullPointerException | BadRequestException e) {
                throw new CompletionException(new BadRequestException("Bad request body"));
              } catch (ForbiddenRequestException e) {
                throw new CompletionException(new ForbiddenRequestException("Duplicate users"));
              }

              chatGroup.setUsers(users);
              chatGroup.setName(chatName);

              return chatRepository.modifyChat(chatGroup);
            },
            httpExecutionContext.current())
        .thenApplyAsync(modifiedChatGroup -> (Result) ok(), httpExecutionContext.current())
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (NotFoundException notFoundException) {
                return notFound(notFoundException.getMessage());
              } catch (ForbiddenRequestException forbiddenException) {
                return forbidden(forbiddenException.getMessage());
              } catch (BadRequestException badRequestException) {
                return badRequest(badRequestException.getMessage());
              } catch (Throwable throwable) {
                return internalServerError();
              }
            });
  }

  /**
   * Gets all chats that are associated with a user
   *
   * @param request The incoming request
   * @return One of the following statuses - 200 - Successfully retrieved chats - 401 - User not
   *     authenticated - 500 - Internal server error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getChats(Http.Request request) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);

    return chatRepository
        .getChatsByUserId(userFromMiddleware.getUserId())
        .thenApplyAsync(chats -> ok(Json.toJson(chats)), httpExecutionContext.current())
        .exceptionally(e -> internalServerError());
  }

  /**
   * Gets the online users from the users of a group chat. called from the GET
   * /api/chats/:chatGroupId/onlineUsers endpoint.
   *
   * @param request the http request.
   * @param chatGroupId the id of the chat to get users from.
   * @return the completion stage with the response. Status codes for the http response: - 200 - ok
   *     - successfully got online users. - 401 - unauthorized - user not logged in. - 403 -
   *     forbidden - user does not have permission. - 404 - not found - chat not found. - 500 -
   *     internal server error - unexpected error.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getOnlineUsers(Http.Request request, int chatGroupId) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    return chatRepository
        .getChatById(chatGroupId)
        .thenApplyAsync(
            chat -> {
              if (chat == null) {
                throw new CompletionException(new NotFoundException("Chat not found"));
              }
              List<User> chatUsers = chat.getUsers();
              if (!userFromMiddleware.isAdmin() && !chatUsers.contains(userFromMiddleware)) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have permission to perform this request."));
              }
              ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
              ArrayNode currentConnectedUsers = Json.newArray();
              for (User user : chatUsers) {
                if (connectedUsers.isUserConnected(user)) {
                  currentConnectedUsers.add(Json.toJson(user));
                }
              }
              return ok(currentConnectedUsers);
            })
        .exceptionally(
            error -> {
              try {
                throw error.getCause();
              } catch (ForbiddenRequestException e) {
                ObjectNode message = Json.newObject();
                message.put("message", e.getMessage());
                return forbidden(Json.toJson(message));
              } catch (NotFoundException e) {
                ObjectNode message = Json.newObject();
                message.put("message", e.getMessage());
                return notFound(Json.toJson(message));
              } catch (Throwable e) {
                ObjectNode message = Json.newObject();
                message.put("message", e.getMessage());
                return internalServerError(message);
              }
            });
  }

  /**
   * Get all messages for a chat using provided query parameters
   *
   * @param request The play request object
   * @param chatGroupId The id of the group chat to get messages for
   * @return One of the following http responses - 200 - Successfully retrieved messages - 401 -
   *     User not authenticated - 403 - User not in group chat - 404 - Chat group not found - 500 -
   *     Any other internal server error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getMessages(Http.Request request, int chatGroupId) {

    User userFromMiddleware = request.attrs().get(ActionState.USER);

    return chatRepository
        .getChatById(chatGroupId)
        .thenComposeAsync(
            (chatGroup -> {
              if (chatGroup == null) {
                throw new CompletionException(new NotFoundException("Chat not found"));
              }

              if (!chatUtil.userInGroup(chatGroup.getUsers(), userFromMiddleware)
                  && !userFromMiddleware.isAdmin()) {
                throw new CompletionException(new ForbiddenRequestException("User not in group"));
              }

              int offset = 0;
              int limit = 20;

              try {
                String offsetString = request.getQueryString("offset");
                offset = Integer.parseInt(offsetString);
              } catch (Exception e) {
                System.out.println("No offset or invalid offset provided, using default of 0");
              }

              try {
                String limitString = request.getQueryString("limit");
                limit = Integer.parseInt(limitString);
              } catch (Exception e) {
                System.out.println("No limit or invalid limit provided, using default of 20");
              }

              return chatRepository.getMessages(chatGroupId, offset, limit);
            }),
            httpExecutionContext.current())
        .thenApplyAsync(
            messages -> {
              JsonNode messagesJson = Json.toJson(messages);
              return ok(messagesJson);
            })
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (NotFoundException notFoundException) {
                return notFound(notFoundException.getMessage());
              } catch (ForbiddenRequestException forbiddenException) {
                return forbidden(forbiddenException.getMessage());
              } catch (Throwable throwable) {
                return internalServerError();
              }
            });
  }

  /**
   * Allows a participant to delete a chat if they are in the chat
   *
   * @param request The incoming request
   * @return One of the following statuses - 200 - Successfully deleted chat - 401 - User not
   *     authenticated - 404 - Chat on found - 403 - Forbidden to delete chat (Because not part of
   *     chat) - 500 - Internal server error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> deleteChat(Http.Request request, int chatGroupId) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);

    return chatRepository
        .getChatById(chatGroupId)
        .thenComposeAsync(
            chatGroup -> {
              if (chatGroup == null) {
                // Chat doesn't exist so 404 it
                throw new CompletionException(new NotFoundException("Chat not found"));
              }

              if (!chatUtil.userInGroup(chatGroup.getUsers(), userFromMiddleware)) {
                throw new CompletionException(new ForbiddenRequestException("User not in group"));
              }

              return chatRepository.deleteChatGroupById(chatGroup);
            },
            httpExecutionContext.current())
        .thenApplyAsync(deletedChatGroup -> (Result) ok(), httpExecutionContext.current())
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (NotFoundException notFoundException) {
                return notFound(notFoundException.getMessage());
              } catch (ForbiddenRequestException forbiddenException) {
                return forbidden(forbiddenException.getMessage());
              } catch (Throwable throwable) {
                return internalServerError();
              }
            });
  }

  /**
   * Creates a message and adds it to a chat group
   *
   * @param request The incoming request
   * @param chatGroupId The chat group ID to add the message to
   * @return One of the following statuses - 201 - The message was successfully created - 400 - The
   *     message was malformed - 401 - User is not authenticated - 403 - The user tried to add a
   *     message to a chat that they don't belong to - 404 - The chat group could not be found - 500
   *     - Any other unexpected error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> createMessage(Http.Request request, int chatGroupId) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    JsonNode jsonBody = request.body().asJson();

    return chatRepository
        .getChatById(chatGroupId)
        .thenComposeAsync(
            chatGroup -> {
              if (chatGroup == null) {
                throw new CompletionException(new NotFoundException("Could not find chat group"));
              }

              if (!chatUtil.userInGroup(chatGroup.getUsers(), userFromMiddleware)) {
                throw new CompletionException(new ForbiddenRequestException("User not in group"));
              }

              if (!jsonBody.has("message")) {
                throw new CompletionException(new BadRequestException("Message does not exist"));
              }

              String messageContents = jsonBody.get("message").asText();
              Message message = new Message(chatGroup, messageContents, userFromMiddleware);

              return chatRepository.createMessage(message);
            })
        .thenApplyAsync(
            createdMessage -> {
              ChatEvents chatEvents = new ChatEvents();
              chatEvents.sendMessageToChatGroup(
                  userFromMiddleware,
                  createdMessage.getChatGroup(),
                  createdMessage.getContents(),
                  createdMessage.getMessageId());
              return created(Json.toJson(createdMessage));
            })
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (NotFoundException notFoundException) {
                return notFound(notFoundException.getMessage());
              } catch (ForbiddenRequestException forbiddenRequestException) {
                return forbidden(forbiddenRequestException.getMessage());
              } catch (BadRequestException badRequestException) {
                return badRequest(badRequestException.getMessage());
              } catch (Throwable throwable) {
                return internalServerError(throwable.getMessage());
              }
            });
  }

  /**
   * Deletes a message in a chat group
   *
   * @param request The incoming request
   * @param messageId The ID of the message to delete
   * @return One of the following statuses - 200 - The message was successfully deleted - 401 - User
   *     is not authenticated - 403 - The user tried to delete a message to a chat - 404 - The chat
   *     group could not be found - 500 - Any other unexpected error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> deleteMessage(Http.Request request, int messageId) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);

    return chatRepository
        .getMessageById(messageId)
        .thenComposeAsync(
            message -> {
              if (!message.isPresent()) {
                throw new CompletionException(new NotFoundException("Message not found"));
              }

              if (message.get().getUser().getUserId() != userFromMiddleware.getUserId()) {
                throw new CompletionException(
                    new ForbiddenRequestException("Cannot delete a message you don't own"));
              }

              return chatRepository.deleteMessage(message.get());
            })
        .thenApplyAsync(
            deletedMessage -> {
              ChatEvents chatEvents = new ChatEvents();
              chatEvents.notifyChatMessageHasBeenDeleted(userFromMiddleware, deletedMessage);
              return (Result) ok();
            })
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (NotFoundException notFoundException) {
                return notFound(notFoundException.getMessage());
              } catch (ForbiddenRequestException forbiddenRequestException) {
                return forbidden(forbiddenRequestException.getMessage());
              } catch (Throwable throwable) {
                throwable.printStackTrace();
                return internalServerError();
              }
            });
  }

  /**
   * Sends a request to the janus server to join a room
   *
   * @return
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> joinRoom(Http.Request request, int chatGroupId) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    JsonNode jsonBody = request.body().asJson();
    long sessionId = jsonBody.get("sessionId").asLong();
    long pluginHandleId = jsonBody.get("pluginHandleId").asLong();

    return chatRepository
        .getChatById(chatGroupId)
        .thenComposeAsync(
            chatGroup -> {
              if (chatGroup == null) {
                throw new CompletionException(new NotFoundException("Could not find chat group"));
              }

              if (!chatUtil.userInGroup(chatGroup.getUsers(), userFromMiddleware)) {
                throw new CompletionException(new ForbiddenRequestException("User not in group"));
              }

              String roomToken = Security.generateToken();
              ObjectNode jsonResponse = Json.newObject();

              if (chatGroup.getVoiceRoomId() != null) {
                long roomId = chatGroup.getVoiceRoomId();
                return voiceServerApi
                    .checkRoomExists(roomId, sessionId, pluginHandleId)
                    .thenComposeAsync(
                        exists -> {
                          if (exists) {
                            jsonResponse.put("token", chatGroup.getRoomToken());
                            jsonResponse.put("room", roomId);
                            return supplyAsync(() -> ok(jsonResponse));
                          } else {
                            return voiceServerApi
                                .generateRoom(roomToken, sessionId, pluginHandleId)
                                .thenComposeAsync(
                                    generatedRoomId -> {
                                      chatGroup.setRoomToken(roomToken);
                                      chatGroup.setVoiceRoomId(generatedRoomId);
                                      return chatRepository.modifyChat(chatGroup);
                                    },
                                    httpExecutionContext.current())
                                .thenApplyAsync(
                                    modifiedRoom -> {
                                      jsonResponse.put("token", roomToken);
                                      jsonResponse.put("room", modifiedRoom.getVoiceRoomId());
                                      return ok(jsonResponse);
                                    });
                          }
                        });
              } else {
                return voiceServerApi
                    .generateRoom(roomToken, sessionId, pluginHandleId)
                    .thenComposeAsync(
                        generatedRoomId -> {
                          chatGroup.setRoomToken(roomToken);
                          chatGroup.setVoiceRoomId(generatedRoomId);
                          return chatRepository.modifyChat(chatGroup);
                        },
                        httpExecutionContext.current())
                    .thenApplyAsync(
                        modifiedChatGroup -> {
                          jsonResponse.put("token", roomToken);
                          jsonResponse.put("room", modifiedChatGroup.getVoiceRoomId());
                          return ok(jsonResponse);
                        });
              }
            },
            httpExecutionContext.current());
  }
}
