package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Model for a single message in a chat group
 */
@Entity
public class Message extends Model {

    @Id
    private int messageId;

    @ManyToOne
    private ChatGroup chatGroup;

    private String contents;

    @ManyToOne
    private User user;

    private Date timestamp;

    public Message(ChatGroup chatGroup, String contents, User user) {
        this.chatGroup = chatGroup;
        this.contents = contents;
        this.user = user;

        // Saves the timestamp of the message
        timestamp = new Date();
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChatGroup getChatGroup() {
        return chatGroup;
    }

    public static final Finder<Integer, Message> find = new Finder<>(Message.class);
}
