Feature: The user can manage destinations

  Background:
    Given a user with the following information exists:
      | firstName | middleName | lastName | email             | password      |
      | Felipe    | Rogelio    | Sanchez  | rogelio@email.com | much-security |

  # Test that a 201 code is returned when creating a Destinations with valid data
  Scenario: A user tries to create a destination with complete valid data
    Given that I want to create a Destination with the following valid data:
      | destinationName | destinationTypeId | districtId | latitude | longitude | countryId |
      | Lower Hutt      | 1                 | 1          | -41.2    | 174.9     | 1         |
    When I click the Add Destination button
    Then I should receive a 200 status code indicating that the Destination is successfully created

    # Test that a 400 status code is returned when a user creates a Destination with incomplete data
    Scenario: A user tries to create a destination with no country
      Given that I want to create a Destination with the following incomplete data:
        | destinationName | destinationTypeId | districtId | latitude | longitude    |
        | Lower Hutt      | 1                 | 1          | -41.2    | latitudeTest |
      When I click the Add Destination button
      Then I should receive a 400 status code indicating that the Destination is not successfully created

  # Test deleting a destination
  Scenario: A user tries to delete a destination
    Given that I have resampled the database and there is a Destination with an ID of one
    When I click the Delete Destination button
    Then I try to search the Destination with the ID of one
    And I should receive a 404 status code indicating that the Destination with the given ID is not found




