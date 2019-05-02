Feature: The admin user can do any task that is available within the system

  Scenario: A traveller user wants to check what roles it has
    Given A traveller user exists
    And The user is logged in
    When The user requests its roles
    Then A list of roles is returned containing the "TRAVELLER" role

  Scenario: An admin user wants to check what roles it has
    Given An admin user exists
    And The user is logged in
    When The user requests its roles
    Then A list of roles is returned containing the "ADMIN" role

  Scenario: An admin user wants to check what roles another user has
    Given An admin user and another user exists
    And The user is logged in
    When The user requests the roles of another user
    Then A list of roles is returned containing the "TRAVELLER" role

  Scenario: A traveller user wants to get a list of roles available on the system
    Given A traveller user exists
    And The user is logged in
    When The user requests the roles available on the system
    Then A list of roles is returned containing all the roles

  Scenario: An admin user wants to get a list of roles available on the system
    Given An admin user exists
    And The user is logged in
    When The user requests the roles available on the system
    Then A list of roles is returned containing all the roles

#
  Scenario: An admin user wants to delete a non admin user
    Given An admin user and another user exists
    And The user is logged in
    When The admin deletes the other user
    Then The user should no longer exist in the database

  Scenario: An admin user wants to delete a second admin user
    Given An admin user exists and another admin user exists
    And The user is logged in
    When The first admin deletes the second admin
    Then The second admin user should no longer exist in the database

  Scenario: An admin user wants to delete itself
    Given An admin user exists
    When The admin user deletes itself
    Then The admin user should no longer exist in the database

  Scenario: An admin user attempts to delete the default admin
    Given The default admin exists
    And Another admin exists
    When The other admin tries to delete the default admin
    Then The default admin should not be deleted
