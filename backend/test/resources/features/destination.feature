Feature: The user can manage destinations

  # Test that a 201 code is returned on valid data when creating
  Scenario: A user tries to create a destination with valid data
    Given that I have destination data to create with:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId |
      | Lower Hutt      | 1                 | 1          | -41.2    | 174.9     | 1                                        |
    When I make a "POST" request to "/api/destinations" with the data
    Then I should receive an 200 status code

    Scenario: A user tries to create a destination with no country
      Given that I have destination data to create with:
        | destinationName | destinationTypeId | districtId | latitude | longitude |
        | Lower Hutt | 1     | 1 |  -41.2 | latitudeTest |
      When I make a "POST" request to "/api/destinations" with the data
      Then I should receive an 400 status code

  # Test deleting a destination
  Scenario: A user tries to delete a destination
    Given that I have a destination created
    When I make a "DELETE" request to "/api/destinations/1" with the data
    Then I should receive a 404 status code when checking for the destination




