package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

import io.ebean.*;
import models.finders.UserFinder;

/**
 * A traveller, who may wish to create trips, go to destinations, book hotels, book flights, etc
 */
@Entity
public class User extends Model {

    @Id
    private int userId;

    private String firstName;

    private String middleName;

    private String lastName;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Nationality> nationalities;

    private Date dateOfBirth;

    private String gender;

    private String email;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List <TravellerType> travellerTypes;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Passport> passports;

    private LocalDateTime timestamp;

    private String password;

    private String token;


    /**
     * Create a new traveller
     * @param firstName the traveller's first name
     * @param middleName the traveller's middle name
     * @param lastName the traveller's last name
     * @param password the traveller's password
     * @param gender the traveller's gender
     * @param email the traveller's email address
     * @param nationalities the traveller's nationalities
     * @param dateOfBirth the traveller's birthday
     * @param passports the traveller's passports
     */
    public User(String firstName, String middleName, String lastName, String password, String gender, String email, List<Nationality> nationalities, Date dateOfBirth, List<Passport> passports, String token) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.password = password;
        this.timestamp = LocalDateTime.now();
        this.gender = gender;
        this.email = email;
        this.nationalities = nationalities;
        this.dateOfBirth = dateOfBirth;
        this.passports =passports;
        this.token = token;
    }

    public User(String firstName, String lastName, String email, String password, String token) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final UserFinder find = new UserFinder();
}