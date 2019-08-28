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

    private User user;

    private Date timestamp;

    public Message(ChatGroup chatGroup, String contents, User user) {
        this.chatGroup = chatGroup;
        this.contents = contents;
        this.user = user;

        // Saves the timestamp of the message
        timestamp = new Date();
    }

    // Used to specify messages that already exist
    private Message(int messageId) {
        this.messageId = messageId;
    }

    public String getContents() {
        return contents;
    }

    public static final Finder<Integer, ChatGroup> find = new Finder<>(ChatGroup.class);
}
