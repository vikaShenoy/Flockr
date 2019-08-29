package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatUtil {
  /**
   * Tranforms users from json to list of users. Also validates that users from json is valid
   * is valid
   * @param ownUserId
   * @param usersFromReq
   * @throws BadRequestException Gets thrown when users from json is invalid
   * @throws ForbiddenRequestException When the user tries to add themselves to their own group
   * @return
   */
  public List<User> transformUsersFromJson(int ownUserId, JsonNode usersFromReq) throws BadRequestException, ForbiddenRequestException {
    List<User> usersInChat = new ArrayList<>();

    // Add own user to group
    User user = new User();
    user.setUserId(ownUserId);
    usersInChat.add(user);

    if (usersFromReq.size() == 0) {
      throw new BadRequestException("Cannot have no users in chat");
    }

    // Used to check for duplicate users
    Set<Integer> userIdsInChat = new HashSet<>();

    for (JsonNode userIdJson : usersFromReq) {
      int userId = userIdJson.asInt();
      if (userId == ownUserId) {
        throw new ForbiddenRequestException("Cannot add yourself to your own trip");
      }

      if (userIdsInChat.contains(userId)) {
        throw new ForbiddenRequestException("Cannot have duplicate users in chat");
      }

      userIdsInChat.add(userId);
      User currentUser = new User();
      currentUser.setUserId(userId);
      usersInChat.add(currentUser);
    }

    return usersInChat;
  }

  /**
   * Tranforms users from json to list of users. Also validates that users from json is valid
   * Overloaded: This method assumes that the user who is editing will be passed into the json body
   * is valid
   * @param usersFromReq
   * @throws BadRequestException Gets thrown when users from json is invalid
   * @throws ForbiddenRequestException When the user tries to add themselves to their own group
   * @return
   */
  public List<User> transformUsersFromJson(JsonNode usersFromReq) throws BadRequestException, ForbiddenRequestException {
    List<User> usersInChat = new ArrayList<>();

    if (usersFromReq.size() == 0) {
      throw new BadRequestException("Cannot have no users in chat");
    }
    // Used to check for duplicate users
    Set<Integer> userIdsInChat = new HashSet<>();

    for (JsonNode userIdJson : usersFromReq) {
      int userId = userIdJson.asInt();
      if (userIdsInChat.contains(userId)) {
        throw new ForbiddenRequestException("Cannot have duplicate users in chat");
      }
      userIdsInChat.add(userId);
      User currentUser = new User();
      currentUser.setUserId(userId);
      usersInChat.add(currentUser);
    }

    return usersInChat;
  }

  /**
   * Checks to see if current user is in group
   * @param usersInGroup Current users of a group
   * @param user The user to check if they are in the group
   * @return True if they are in the group, false otherwise
   */
  public boolean userInGroup(List<User> usersInGroup, User user) {
    for (User currentUser : usersInGroup) {
      if (currentUser.getUserId() == user.getUserId()) {
        return true;
      }
    }

    return false;
  }
}
