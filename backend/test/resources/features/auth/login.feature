Feature: The user can log in

  Background: A user has signed up
    Given that the user has valid user data to sign up
      | firstName | lastName | email              | password    |
      | Josefina  | Perez    | josefina@email.com | so-security |
    When the user signs up
    Then the user now exists in the system

  Scenario: A user can login successfully
    Given the user has the following data to login with
      | email              | password      |
      | josefina@email.com | so-security   |
    When the user tries to login
    Then the user should be logged in

  Scenario:
    Given the user has the following data to login with
      | email              | password       |
      | josefina@email.com | wrong-password |
    When the user tries to login
    Then the user should not be logged in


