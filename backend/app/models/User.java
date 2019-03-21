package models;

import javax.persistence.*;
import javax.validation.Constraint;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import io.ebean.*;
import io.ebean.annotation.CreatedTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import play.data.format.Formats;
import play.data.validation.Constraints;
/**
 * A traveller, who may wish to create trips, go to destinations, book hotels, book flights, etc
 */
@Entity
public class User extends Model {

    @Id
    @Constraints.Required
    private int userId;

    @Constraints.Required
    private String firstName;

    private String middleName;

    @Constraints.Required
    private String lastName;

    @ManyToMany(mappedBy = "users")
    public List<Nationality> nationalities;

    private Timestamp dateOfBirth;

    @OneToOne
    private Gender gender;

    @Constraints.Required
    private String email;

    @ManyToMany(mappedBy = "users")
    public List <TravellerType> travellerTypes;

    @ManyToMany(mappedBy = "users")
    public List<Passport> passports;

    @Constraints.Required
    @CreatedTimestamp
    @Column(updatable=false)
    private java.sql.Timestamp timestamp;

    @Constraints.Required
    private String passwordHash;


    private String token;


    /**
     * Create a new traveller
     * @param firstName the traveller's first name
     * @param middleName the traveller's middle name
     * @param lastName the traveller's last name
     * @param passwordHash the traveller's hashed password
     * @param gender the traveller's gender
     * @param email the traveller's email address
     * @param nationalities the traveller's nationalities
     * @param dateOfBirth the traveller's birthday
     * @param passports the traveller's passports
     * @param token the traveller's token
     */
    public User(String firstName, String middleName, String lastName, String passwordHash, Gender gender, String email, List<Nationality> nationalities, Timestamp dateOfBirth, List<Passport> passports, String token) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.gender = gender;
        this.email = email;
        this.nationalities = nationalities;
        this.dateOfBirth = dateOfBirth;
        this.passports =passports;
        this.token = token;
    }

    /**
     * Constructor used for signin up
     * @param firstName Traveller's first name
     * @param lastName Traveller's last name
     * @param email Traveller's email
     * @param passwordHash Traveller's hashed password
     * @param token Traveller's token
     */
    public User(String firstName, String middleName, String lastName, String email, String passwordHash, String token) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.token = token;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Nationality> getNationalities() {
        return nationalities;
    }

    public void setNationalities(List<Nationality> nationalities) {
        this.nationalities = nationalities;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<TravellerType> getTravellerTypes() {
        return travellerTypes;
    }

    public void setTravellerTypes(List<TravellerType> travellerTypes) {
        this.travellerTypes = travellerTypes;
    }

    public List<Passport> getPassports() {
        return passports;
    }

    public void setPassports(List<Passport> passports) {
        this.passports = passports;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean profileCompleted() {
        return firstName != null && middleName != null && lastName != null & nationalities.size() != 0 && dateOfBirth != null && gender != null && email != null && travellerTypes.size() != 0 && timestamp != null && passwordHash != null && token != null;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, User> find = new Finder<>(User.class);
}