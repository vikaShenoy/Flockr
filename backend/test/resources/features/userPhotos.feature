Feature: As a registered user I want to have photos that display on my profile.

  Background:
    Given users with the following information exist:
      | firstName | lastName   | email                  | password   |
      | Phillip   | Phototaker | ppt@phillipsphotos.com | say-cheese |
      | Angelo    | Admin      | aa@admin.com           | its-ya-boi |

  @UserPhoto
  Scenario: a user wants to view all of their photos
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the user requests all their photos
    Then the user gets the same list

  @UserPhotos
  Scenario: a user requests a list of photos for a user that does not exist
    When a user requests photos for a non signed up userId
    Then they should receive a "Not Found" error message with a 404 error code

  @UserPhotos
  Scenario: a user that has not signed in requests a list of their photos
    When a non signed in user requests photos for another user
    Then they should receive a "Unauthorized" error message with a 401 error code

  @UserPhotos
  Scenario: an admin user requests the photos of another user
    Given one of the users is an admin
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the admin user requests the photos of another user
    Then the user gets the same list

  @UserPhoto
  Scenario: A user can add a photo
    Given a user has a photo called "test.png"
    And they want the photo to be public
    And they want the photo to be their profile photo
    When they add the photo
    Then the photo is added

  @UserPhoto
  Scenario: A user wants to change their photo permission from public to private.
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    And The photo "dog.jpg" permission is public
    When The user changes the photo permission to private
    Then The photo permission is set to private

  @UserPhoto
  Scenario: An admin wants to change a user's photo permission from public to private.
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | true      | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    And The photo "dog.jpg" permission is public
    When The admin changes the photo permission to private
    Then The photo permission is set to private
