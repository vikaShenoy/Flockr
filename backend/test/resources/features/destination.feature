Feature: The user can manage destinations

  Background:
    Given a user with the following information exists:
      | firstName | middleName | lastName | email             | password      |
      | Felipe    | Rogelio    | Sanchez  | rogelio@email.com | much-security |

  # Test that a 201 code is returned when creating a Destinations with valid data
  Scenario: A user tries to create a destination with complete valid data
    Given the database has been populated with countries, districts and destination types
    Given that I want to create a Destination with the following valid data:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId |
      | Lower Hutt      | 1                 | 1          | -41.2    | 174.9     | 1         |
    When I make a "POST" request to "/api/destinations" with the data
    Then I should receive an 201 status code

    # Test that a 400 status code is returned when a user creates a Destination with incomplete data
    Scenario: A user tries to create a destination with no country
      Given the database has been populated with countries, districts and destination types
      Given that I want to create a Destination with the following incomplete data:
        | destinationName | destinationTypeId | districtId | latitude | longitude    |
        | Lower Hutt      | 1                 | 1          | -41.2    | latitudeTest |
      When I make a "POST" request to "/api/destinations" with the data
      Then I should receive an 400 status code

  # Test deleting a destination
  Scenario: A user tries to delete a destination
    Given that I am logged in
    And the database has been populated with countries, districts and destination types
    And that I have a destination created with id 1
    When I make a "DELETE" request to "/api/destinations/1" to delete the destination
    Then I should receive a 404 status code when getting the destination with id 1




