Feature: The user can sign up

  Scenario: A user tries to sign up
    Given that the user has valid user data to sign up
      | firstName | lastName | email              | password    |
      | Josefina  | Perez    | josefina@email.com | so-security |
    When the user signs up
    Then the user now exists in the system

  # Test that a 400 code is returned when signing up with *some* incomplete data
  Scenario: A user tries to sign up without a last name
    Given the user has incomplete user data to sign up
      | firstName | email          | password      |
      | Jose      | jose@gmail.com | much-security |
    When the user signs up
    Then I should receive an error message saying "Failed to sign up the user."

  Scenario: A user tries to sign up without a password
    Given the user has incomplete user data to sign up
      | firstName | lastName  | email          |
      | Jose      | Rodriguez | jose@gmail.com |
    When the user signs up
    Then I should receive an error message saying "Failed to sign up the user."

  Scenario: A user tries to sign up without an email
    Given the user has incomplete user data to sign up
      | firstName | lastName  | password  |
      | Jose      | Rodriguez | so-secure |
    When the user signs up
    Then I should receive an error message saying "Failed to sign up the user."

  Scenario: A user tries to sign up without a first name
    Given the user has incomplete user data to sign up
      | lastName | email              | password    |
      | Perez    | josefina@email.com | so-security |
    When the user signs up
    Then I should receive an error message saying "Failed to sign up the user."