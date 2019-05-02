Feature: A user can be deleted

  Background:
    Given that the default admin exists
    And an admin user exists
    And two regular users exist

  Scenario: An admin tries to delete a regular user
    Given that the default admin is logged in
    And the admin is logged in
    And the two regular users are logged in
    When the admin tries to delete the regular user
    Then the regular user should be deleted

  Scenario: An admin tries to delete the default admin
    When the admin tries to delete the default admin
    Then the default admin should not be deleted

  Scenario: A regular user tries to delete another regular user
    When a regular user tries to delete another regular user
    Then the second user should not be deleted

  Scenario: A regular user tries to delete their own profile
    When a regular user tries to delete their own profile
    Then the regular user should be deleted

  Scenario: The default admin tries to delete their own profile
    When the default admin tries to delete their own profile
    Then the default admin should not be deleted

  Scenario: A regular user tries to delete the default admin
    When a regular user tries to delete the default admin
    Then the default admin should not be deleted