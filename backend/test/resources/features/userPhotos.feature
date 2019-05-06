Feature: As a registered user I want to have photos that display on my profile.

  Background:
    Given a user exists with the following information:
      | firstName | middleName | lastName | email                         | password         |
      | Timmy     | The        | Tester   | timmytester@testinstitute.com | time-for-testing |

  @UserPhoto
  Scenario: a user wants to view all of their photos
    Given the user has the following photos in the system:
      | filename | isPrimary |
      | 1.jpeg   | true      |
      | 2.jpeg   | false     |
      | 3.jpeg   | false     |
      | 4.jpeg   | false     |
      | 5.jpeg   | false     |
    When the user tries to retrieve their photos
    Then the user gets a list of the following photos:
      | filename | isPrimary |
      | 1.jpeg   | true      |
      | 2.jpeg   | false     |
      | 3.jpeg   | false     |
      | 4.jpeg   | false     |
      | 5.jpeg   | false     |