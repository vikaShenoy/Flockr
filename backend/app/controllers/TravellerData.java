package controllers;

import play.data.validation.Constraints;
import java.util.Date;

/**
 * A form processing DTO that maps to the new traveller form.
 *
 * Using a class specifically for form binding reduces the chances
 * of a parameter tampering attack and makes code clearer, because
 * you can define constraints against the class.
 */


public class TravellerData {
    @Constraints.Required
    private String firstName;

    @Constraints.Required
    private String middleName;

    @Constraints.Required
    private String lastName;

    @Constraints.Required
    private String password;

    @Constraints.Required
    private String gender;

    @Constraints.Required
    private String emailAddress;

    @Constraints.Required
    private Date birthday;

    @Constraints.Required
    private String nationalities;

    @Constraints.Required
    private String passports;


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

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public String getEmailAddress() { return emailAddress; }

    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public Date getBirthday() { return birthday; }

    public void setBirthday(Date birthday) { this.birthday = birthday; }

    public String getNationalities() { return nationalities; }

    public void setNationalities(String nationalities) { this.nationalities = nationalities; }

    public String getPassports() { return passports; }

    public void setPassports(String passports) { this.passports = passports; }

}