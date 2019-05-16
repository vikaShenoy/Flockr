Feature: As a registered user I want to have photos that display on my profile.

  Background:
    Given a user with the following information exists:
      | firstName | lastName   | email                  | password   |
      | Phillip   | Phototaker | ppt@phillipsphotos.com | say-cheese |

  @UserPhoto
  Scenario: a user wants to view all of their photos
    Given the user has the following photos in the system:
      | filename | isPrimary | isPublic |
      | 1.jpeg   | true      | false    |
      | 2.jpeg   | false     | false    |
      | 3.jpeg   | false     | false    |
      | 4.jpeg   | false     | false    |
      | 5.jpeg   | false     | false    |
    When the user requests all their photos
    Then the user gets the same list

  @UserPhoto
  Scenario: A user can add a photo
    Given a user has a photo called "test.png"
    And they want the photo to be public
    And they want the photo to be their profile photo
    When they add the photo
    Then the photo is added
