Feature: A user can have their roles updated

  Background:
    Given ROLES - A user with the following info exists...
      | firstName | middleName | lastName  | password | email                |
      | Russell   | Vikas      | Westbrook | testPass | russWest44@gmail.com |

    Given ROLES - An admin with the following info exists...
      | firstName | middleName | lastName | password     | email          |
      | Andy      | Admin      | Holden   | passwordTest | test@gmail.com |

#   Test the patch endpoint works to change user role
  Scenario: A user has their role changed from TRAVELLER to ADMIN by an admin
    Given ROLES - The admin is logged in...
    When ROLES - An admin adds an admin role to a user
    And ROLES - I request roles from the database
    Then ROLES - The user has an admin role

  Scenario: A non-admin user tries to give themselves admin role
    Given ROLES - the user is logged in...
    When ROLES - A user adds an admin role to themselves
    And ROLES - I request roles from the database
    Then ROLES - I receive a 401 status code

