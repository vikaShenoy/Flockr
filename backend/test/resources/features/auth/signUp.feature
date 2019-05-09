Feature: The user can sign up

  # Test that a 201 code is returned when signing up with valid data
  @SignUpSteps
  Scenario: A user tries to sign up with valid data with a middle name
    Given that I have valid user data to sign up:
      | firstName | middleName | lastName | email            | password      |
      | Matias    | Felipe     | Suarez   | matias@email.com | much-security |
    When I click the Sign Up button
    Then I should receive a 201 status code indicating that the User is successfully created

  @SignUpSteps
  Scenario: A user tries to sign up without a middle name
    Given that I have incomplete user data to sign up:
      | firstName | lastName | email              | password    |
      | Josefina  | Perez    | josefina@email.com | so-security |
    When I click the Sign Up button
    Then I should receive a 201 status code indicating that the User is successfully created

  # Test that a 400 code is returned when signing up with *some* incomplete data
  @SignUpSteps
  Scenario: A user tries to sign up without a lastname
    Given that I have incomplete user data to sign up:
      | firstName | middleName | email          | password      |
      | Jose      | Manuel     | jose@gmail.com | much-security |
    When I click the Sign Up button
    Then I should receive a 400 status code indicating that the User filled the form with invalid data

  @SignUpSteps
  Scenario: A user tries to sign up without a password
    Given that I have incomplete user data to sign up:
      | firstName | middleName | lastName  | email          |
      | Jose      | Manuel     | Rodriguez | jose@gmail.com |
    When I click the Sign Up button
    Then I should receive a 400 status code indicating that the User filled the form with invalid data

  @SignUpSteps
  Scenario: A user tries to sign up without an email
    Given that I have incomplete user data to sign up:
      | firstName | middleName | lastName  | password  |
      | Jose      | Manuel     | Rodriguez | so-secure |
    When I click the Sign Up button
    Then I should receive a 400 status code indicating that the User filled the form with invalid data

  @SignUpSteps
  Scenario: A user tries to sign up without a first name
    Given that I have incomplete user data to sign up:
      | middleName | lastName | email              | password    |
      | Pablo      | Perez    | josefina@email.com | so-security |
    When I click the Sign Up button
    Then I should receive a 400 status code indicating that the User filled the form with invalid data