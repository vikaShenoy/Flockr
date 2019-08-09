package models;

import io.ebean.Finder;
import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserMessage extends Model {

//  @Id private int messageId;

  public UserMessage(int messageId) {
//    this.messageId = messageId;
  }

  /** This is required by Ebean to make queries on the databse */
  public static final Finder<Integer, Role> find = new Finder<>(Role.class);
}
