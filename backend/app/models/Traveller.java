package models;

import play.data.format.Formats;
import play.data.validation.Constraints;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Constraint;
import java.util.Date;
import java.time.LocalDateTime;
import io.ebean.*;

/**
 * A traveller, who may wish to create trips, go to destinations, book hotels, book flights, etc
 */
@Entity
public class Traveller extends BaseModel {
    @Constraints.Required
    private String gender;

    @Constraints.Required
    private String firstName;

    @Constraints.Required
    private String middleName;

    @Constraints.Required
    private String lastName;

    @Constraints.Required
    private String nationalities;

    @Constraints.Required
    @Formats.DateTime(pattern="yyyy-MM-dd")
    private Date birthday;

    @Constraints.Required
    private String emailAddress;

    @Constraints.Required
    private TravellerType travellerType;

    @Constraints.Required
    private String passports;

    private LocalDateTime timestamp;

    @Constraints.Required
    private String password;


    /**
     * Create a new traveller
     * @param firstName the traveller's first name
     * @param middleName the traveller's middle name
     * @param lastName the traveller's last name
     * @param password the traveller's password
     * @param gender the traveller's gender
     * @param emailAddress the traveller's email address
     * @param nationalities the traveller's nationalities
     * @param birthday the traveller's birthday
     * @param passports the traveller's passports
     */
    public Traveller(String firstName, String middleName, String lastName, String password, String gender, String emailAddress, String nationalities, Date birthday, String passports) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.password = password;
        this.timestamp = LocalDateTime.now();
        this.gender = gender;
        this.emailAddress = emailAddress;
        this.nationalities = nationalities;
        this.birthday = birthday;
        this.passports = passports;
    }


    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Long, Traveller> find = new Finder<>(Traveller.class);


    //Getters and Setters


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getNationalities() {
        return nationalities;
    }

    public void setNationalities(String nationalities) {
        this.nationalities = nationalities;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getEmailAddress(){
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress= emailAddress;
    }


    public TravellerType getTravellerType() {
        return travellerType;
    }

    public void setTravellerType(TravellerType travellerType) {
        this.travellerType = travellerType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPassports() {
        return passports;
    }

    public void setPassports(String passports) {
        this.passports = passports;
    }
}