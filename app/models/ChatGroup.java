package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Model for a chat group used to chat with other users
 */
@Entity
public class ChatGroup extends Model {

    @Id
    private int chatGroupId;

    private String name;

    @ManyToMany(cascade=CascadeType.ALL)
    private List<User> users;

    @JsonIgnore
    // Fetch lazily so that we don't get all messages when fetching the chat group
    @OneToMany(mappedBy = "chatGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    // Is transient because we don't want to insert into db
    @Transient
    private Set<User> connectedUsers;

    public ChatGroup(String name, List<User> users, List<Message> messages) {
        this.name = name;
        this.users = users;
        this.messages = messages;
    }

    public static final Finder<Integer, ChatGroup> find = new Finder<>(ChatGroup.class);

    public List<User> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getChatGroupId() {
        return chatGroupId;
    }

    public String getName() {
        return name;
    }
}
