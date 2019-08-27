package models;

import io.ebean.Finder;
import io.ebean.Model;

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

    @ManyToMany
    private List<User> users;


    @OneToMany(mappedBy = "chatGroup", cascade = CascadeType.ALL)
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
}
