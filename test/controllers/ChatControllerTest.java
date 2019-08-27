package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.ChatGroup;
import models.Role;
import models.RoleType;
import models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatControllerTest {
  private Application application;
  private User user;
  private User otherUser;
  private User adminUser;
  private FakeClient fakeClient;

  @Before
  public void setUp() throws IOException, ServerErrorException, FailedToSignUpException {
    Map<String, String> testSettings = new HashMap<>();
    testSettings.put("db.default.driver", "org.h2.Driver");
    testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
    testSettings.put("play.evolutions.db.default.enabled", "true");
    testSettings.put("play.evolutions.db.default.autoApply", "true");
    testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

    application = Helpers.fakeApplication(testSettings);
    Helpers.start(application);

    TestState.getInstance().setApplication(application);
    TestState.getInstance().setFakeClient(new FakePlayClient(application));

    fakeClient = TestState.getInstance().getFakeClient();
    user = fakeClient.signUpUser("Timmy", "Tester", "timmy@tester.com",
            "abc123");
    otherUser = fakeClient.signUpUser("Tammy", "Tester", "tammy@tester.com",
            "abc123");
    adminUser = fakeClient.signUpUser("Andy", "Admin", "andy@admin.com",
            "abc123");

    Role role = new Role(RoleType.ADMIN);
    List<Role> roles = new ArrayList<>();
    roles.add(role);
    role.save();
    user.save();
    adminUser = User.find.byId(adminUser.getUserId());
    adminUser.setRoles(roles);
    adminUser.save();
    otherUser.save();
  }

  @After
  public void tearDown() {
    Application application = TestState.getInstance().getApplication();
    Helpers.stop(application);
    TestState.clear();
  }

  @Test
  public void shouldCreateChatWithValidData() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    chatGroupBody.putArray("userIds")
           .add(otherUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;

    Assert.assertEquals(201, result.status());
    JsonNode chatGroupResBody = PlayResultToJson.convertResultToJson(result);

    // Make sure json contains the right fields
    Assert.assertTrue(chatGroupResBody.has("chatGroupId"));
    Assert.assertTrue(chatGroupResBody.has("name"));
    Assert.assertTrue(chatGroupResBody.has("users"));
    int partnerUserId = chatGroupResBody.get("users").get(0).get("userId").asInt();
    Assert.assertEquals(otherUser.getUserId(), partnerUserId);

    int ownUserId = chatGroupResBody.get("users").get(1).get("userId").asInt();
    Assert.assertEquals(adminUser.getUserId(), ownUserId);

    Assert.assertTrue(chatGroupResBody.has("messages"));

    // Make sure DB contains right data
    ChatGroup chatGroup = ChatGroup.find.byId(chatGroupResBody.get("chatGroupId").asInt());
    Assert.assertNotNull(chatGroup);
    Assert.assertEquals(chatGroup.getUsers().size(), 2);
    Assert.assertEquals(otherUser.getUserId(), chatGroup.getUsers().get(0).getUserId());
    Assert.assertEquals(adminUser.getUserId(), chatGroup.getUsers().get(1).getUserId());
    Assert.assertEquals(0, chatGroup.getMessages().size());
    Assert.assertEquals(chatGroupResBody.get("chatGroupId").asInt(), chatGroup.getChatGroupId());
  }

  @Test
  public void shouldNotCreateChatWithNoName() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.putArray("userIds")
           .add(otherUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }

  @Test
  public void shouldNotCreateChatWithNoUserIds() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }

  @Test
  public void shouldNotCreateChatWhenOwnUserIsSpecified() throws IOException {
    // Should not create chat when own user id specified as the people that should be in the chat
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");
    chatGroupBody.putArray("userIds")
         .add(adminUser.getUserId());

    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(403, result.status());
  }

  @Test
  public void shouldNotCreateChatWhenNoUsersAreSpecified() throws IOException {
    ObjectNode chatGroupBody = Json.newObject();
    chatGroupBody.put("name", "my chat name");

    // Empty array means you aren't in a group with anyone which is invalid
    chatGroupBody.putArray("userIds");
    String endpoint = "/api/chats";
    Result result = fakeClient.makeRequestWithToken("POST", chatGroupBody, endpoint, adminUser.getToken()) ;;
    Assert.assertEquals(400, result.status());
  }






}
