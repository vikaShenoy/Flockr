package util;

import modules.websocket.ConnectedUsers;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestProbe;
import models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ConnectedUsersTest {
    User user1;
    User user2;
    ConnectedUsers connectedUsers;
    ActorRef actorRef1;
    ActorRef actorRef2;


    @Before
    public void setUp() {
        // Have to clear all values as it's a singleton
        connectedUsers = ConnectedUsers.getInstance();
        user1 = new User("Bob", "Jose", "Smith", "secure-hash", "male", "bobsmith@gmail.com", new ArrayList<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), "abc123");
        user1.setUserId(1);
        user2 = new User("Jane", "Jose", "Smith", "secure-hash", "male", "bobsmith@gmail.com", new ArrayList<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), "abc123");
        user2.setUserId(2);
        actorRef1 = new TestProbe(ActorSystem.apply()).ref();
        actorRef2 = new TestProbe(ActorSystem.apply()).ref();
    }

    /**
     * Test a single user connecting
     */
    @Test
    public void addUser() {
        connectedUsers.addConnectedUser(user1, actorRef1);
        Assert.assertTrue(connectedUsers.getConnectedUsers().containsKey(user1));
        Assert.assertEquals(actorRef1, connectedUsers.getConnectedUsers().get(user1));
    }

    /**
     * Test adding two different users
     */
    @Test
    public void addTwoUsers() {
        connectedUsers.addConnectedUser(user1, actorRef1);
        connectedUsers.addConnectedUser(user2, actorRef2);
        Assert.assertTrue(connectedUsers.getConnectedUsers().containsKey(user1));
        Assert.assertTrue(connectedUsers.getConnectedUsers().containsKey(user2));

        Assert.assertEquals(actorRef1, connectedUsers.getConnectedUsers().get(user1));
        Assert.assertEquals(actorRef2, connectedUsers.getConnectedUsers().get(user2));
    }

    /**
     * Test that the same user somehow connects, then they only have one connection open
     */
    @Test
    public void sameUserConnects() {
        connectedUsers.addConnectedUser(user1, actorRef1);
        connectedUsers.addConnectedUser(user1, actorRef2);
        Assert.assertEquals(1, connectedUsers.getConnectedUsers().size());
        Assert.assertTrue(connectedUsers.getConnectedUsers().containsKey(user1));
    }

    @Test
    public void clientCanDisconnect() {
        connectedUsers.addConnectedUser(user1, actorRef1);
        connectedUsers.removeConnectedUser(user1);
        Assert.assertEquals(0, connectedUsers.getConnectedUsers().size());
    }

    @After
    public void tearDown() {
        connectedUsers.clear();
    }
}
