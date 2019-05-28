Feature: The user can manage destinations

  Background:
    Given users with the following information exist:
      | firstName | lastName     | email        | password    |
      | Danny     | Destinations | dd@email.com | where-to-go |

  # Test that a 201 code is returned when creating a Destinations with valid data
  Scenario: A user tries to create a destination with complete valid data
    Given that I am logged in
    Given the database has been populated with the following countries, districts and destination types:
      | destinationType | country                  | district        |
      | Event           | United States of America | Black Rock City |
      | City            | Australia                | New Farm        |
    Given that I want to create a Destination with the following valid data:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId |
      | Lower Hutt      | 1                 | 1          | -41.2    | 174.9     | 1         |
    When I click the Add Destination button
    Then The destination should be created successfully

    # Test that a 400 status code is returned when a user creates a Destination with incomplete data
  Scenario: A user tries to create a destination with no country
    Given that I am logged in
    Given the database has been populated with the following countries, districts and destination types:
      | destinationType | country                  | district        |
      | Event           | United States of America | Black Rock City |
      | City            | Australia                | New Farm        |
    Given that I want to create a Destination with the following incomplete data:
      | destinationName | destinationTypeId | districtId | latitude | longitude    |
      | Lower Hutt      | 1                 | 1          | -41.2    | latitudeTest |
    When I click the Add Destination button
    Then I should receive an error, because the data is incomplete

  # Test deleting a destination
  Scenario: A user tries to delete a destination
    Given that I am logged in
    And the database has been populated with the following countries, districts and destination types:
      | destinationType | country                  | district        |
      | Event           | United States of America | Black Rock City |
      | City            | Australia                | New Farm        |
    And that I have the following destinations:
      | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId |
      | The Dairy Down The Street | 1                 | 1          | =41.2    | 174.9     | 1         |
    When I click the Delete Destination button
    Then I should receive an error indicating that the Destination is not found




