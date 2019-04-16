Feature: A user can modify their roles

  Scenario: A traveller user wants to update their roles
    Given A traveller user exists
    And The user is logged in
    When The user updates their roles to contain "ADMIN"
    Then The user's roles contain the "ADMIN" role