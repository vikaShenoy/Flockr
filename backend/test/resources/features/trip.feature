Feature: The user can manage trips

  Background: I have resample
    Given i have resampled

  @TripSteps
  #Test that a trip can be created
  Scenario: A user tries to create a trip
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    And I send a request to add a trip
    Then The server should return a 201 status

  @TripSteps
  Scenario: A user tries to add a trip with less then 2 trip destinations
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
    And I send a request to add a trip
    Then The server should return a 400 status

  @TripSteps
  Scenario: A user tries to add a trip with adjacent destination ID's
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |

    And I send a request to add a trip
    Then The server should return a 400 status

  @TripSteps
  Scenario: A user tries to update a trip
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |

    And I send a request to update a trip
    Then The server should return a 200 status

  @TripSteps
  Scenario: A user tries to get a trip
    When I send a request to get a trip with id 1
    Then The server should return a 200 status

  @TripSteps
  Scenario: A user tries to get a trip which doesn't exist
    When I send a request to get a trip with id 3
    Then The server should return a 404 status

  @TripSteps
  Scenario: A user tries to get a list of their trips
    When I send a request to get trips
    Then The server should return a 200 status

