Feature: The user can manage destinations

  Background:
    Given users with the following information exist:
      | firstName | lastName     | email              | password    |
      | Danny     | Destinations | dd@email.com       | where-to-go |
      | Alice     | Admin        | alice@travelea.com | so-secure   |

  # Test that a 201 code is returned when creating a Destinations with valid data
  Scenario: A user tries to create a destination with complete valid data
    Given that user 0 logged in
    Given that I want to create a Destination with the following valid data:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId |
      | Lower Hutt      | 1                 | 1          | -41.2    | 174.9     | 1         |
    When I click the Add Destination button
    Then The destination should be created successfully

    # Test that a 400 status code is returned when a user creates a Destination with incomplete data
  Scenario: A user tries to create a destination with no country
    Given that user 0 logged in
    Given that I want to create a Destination with the following incomplete data:
      | destinationName | destinationTypeId | districtId | latitude | longitude    |
      | Lower Hutt      | 1                 | 1          | -41.2    | latitudeTest |
    When I click the Add Destination button
    Then I should receive an error, because the data is incomplete

  # Test deleting a destination
  Scenario: A user tries to delete a destination
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          | -41.2    | 174.9     | 1         |
    When I click the Delete Destination button
    Then I should receive an error indicating that the Destination is not found

  Scenario: A user tries to get their own destination photos
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | -41.2    | 174.9     | 1         | true     |
      | Some Name                 | 1                 | 1          | -41.2    | 174.9     | 1         | false    |
    When the user gets their own destinations
    Then 2 destinations should be returned


  Scenario: A user tries to access another user's destinations
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | -41.2    | 174.9     | 1         | true     |
      | Some Name                 | 1                 | 1          | -41.2    | 174.9     | 1         | false    |
    When another user gets the user's destinations
    Then 1 destinations should be returned

  # Test updating a destination
  Scenario: A user tries to update a destination with no change in the information
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | false    |
    When I update the Destination with the following information:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | false    |
    Then I should be allowed to update the Destination

  Scenario: A user tries to update a destination with new information
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | false    |
    When I update the Destination with the following information:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | 40.0     | 184.9     | 1         |  true    |
    Then the Destination information is updated

  Scenario: A user tries to update a non-existent destination with the given ID
    Given that user 0 logged in
    When I try to update the Destination with the following information:
      | destinationId | destinationName | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | 10000         | America         | 1000              | 111        | 40.0     | 184.9     | 1         | true     |
    Then I get an error indicating that the Destination is not found


  Scenario: A user tries to update another user's destination
    Given that user 0 logged in
    And that another user have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         |
    When I try to update another user's destination with the following information:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | America         | 1000              | 111        | 40.0     | 184.9     | 1         | true     |
    Then I get an error indicating that I am not allowed to make changes on the destination

  @UserPhoto
  Scenario: A user updates a destination that has a duplicate and is linked to a private photo
    Given that user 0 logged in
    And that another user has the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         |
    And the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | false     | false    |
    And that destination is linked to the user's destination photo
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         |
    When I update the Destination with the following information:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
      | The Dairy Down The Street | 1                 | 1          | 40.0     | 184.9     | 1         | true     |
    Then the other user's private destination is deleted
    And the photo is changed to link to the public destination

  # Test adding a photo to a destination
  Scenario: A user tries to add a photo to a destination with a photo that doesn't exist
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    And the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | false     | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the user adds "imageidonthave.png" to the destination "The Dairy Down The Street"
    Then the photo does not get added to the destination

  Scenario: A user tries to add a photo to a destination with a destination that doesn't exist
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    And the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | false     | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the user adds "monkey.png" to the destination "Some destination I don't have"
    Then the photo does not get added to the destination

  Scenario: A user tries to add a photo to a destination that isn't theirs
    Given that user 0 logged in
    And that another user has the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    And the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | false     | false    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | false    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    When the user adds "monkey.png" to the destination "Some destination I don't have"
    Then the photo does not get added to the destination

  Scenario: A user tries to add a destination that already exists
    Given that user 0 logged in
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    And that I want to create a Destination with the following valid data:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    When I click the Add Destination button
    Then I get a message saying that the destination already exists

  Scenario: A regular user tries to get all the photos for a destination
    Given that the user 0 is a regular user
    Given that user 0 logged in
    Given that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          |  41.2    | 174.9     | 1         |
    Given the user has the following photos in the system:
      | filename      | isPrimary | isPublic |
      | monkey.png    | false     | true    |
      | dog.jpg       | false     | false    |
      | cat.jpeg      | false     | true    |
      | cucumber.jpeg | false     | false    |
      | whale.png     | false     | false    |
    Given the photo "monkey.png" is linked to the destination "The Dairy Down The Street"
    Given the photo "whale.png" is linked to the destination "The Dairy Down The Street"
    When the user gets all the photos for the destination
    Then the user can see all the public photos for the destination
    And the user can see only their private photos linked to the destination

  Scenario: A regular user tries to get all the photos for a destination that does not exist
    Given that the user 0 is a regular user
    Given that user 0 logged in
    When the user gets all the photos for a destination that does not exist
    Then they are told that the destination does not exist

