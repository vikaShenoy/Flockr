Feature: A user can be deleted

  Background:
    Given that the default admin exists
    And an admin user exists
    And a regular user with first name Alice exists
    And a regular user with first name Bob exists
    And that the default admin is logged in
    And the admin is logged in
    And the two regular users are logged in

  @DeleteUserSteps
  Scenario: An admin tries to delete a regular user
    When the admin tries to delete the regular user Alice
    Then the regular user Alice should be deleted

  @DeleteUserSteps
  Scenario: An admin tries to delete the default admin
    When the admin tries to delete the default admin
    Then the default admin should not be deleted

  @DeleteUserSteps
  Scenario: A regular user tries to delete another regular user
    When the regular user Alice tries to delete the other regular user Bob
    Then the regular user Bob should not be deleted

  @DeleteUserSteps
  Scenario: A regular user tries to delete their own profile
    When a regular user Bob tries to delete their own profile
    Then the regular user Bob should be deleted

  @DeleteUserSteps
  Scenario: The default admin tries to delete their own profile
    When the default admin tries to delete their own profile
    Then the default admin should not be deleted

  @DeleteUserSteps
  Scenario: A regular user tries to delete the default admin
    When a regular user Bob tries to delete the default admin
    Then the default admin should not be deleted

  @DeleteUserSteps
  Scenario: Deleting a non existent user
    When someone tries to delete a user that does not exist
    Then the server should respond saying that the user does not exist