Feature: The user can manage trips

  #Test that a trip can be created
  Scenario: A user tries to create a trip
    Given I have resampled
     When I have a trip named "Trip Name"
    And I have trip destinations
    | destinationId | arrivalDate | arrivalTime | departureDate | departureTime |
    | 1             | 1553576899807 | 531       | 1553576899807 | 300           |
    | 2             | 1553576899807 | 531       | 1553576899807 | 300           |
    And I send a request to add a trip
  Then The server should return a 201 status

  Scenario: A user tries to add a trip with less then 2 trip destinations
    Given I have resampled
    When I have a trip named "Trip Name"
    And I have trip destinations
    | destinationId | arrivalDate | arrivalTime | departureDate | departureTime |
    | 1             | 1553576899807 | 531       | 1553576899807 | 300           |
    And I send a request to add a trip
    Then The server should return a 400 status

     Scenario: A user tries to add a trip with adjacent destination ID's
       Given I have resampled
       When I have a trip named "Trip Name"
       And I have trip destinations
      | destinationId | arrivalDate | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531       | 1553576899807 | 300           |
      | 1             | 1553576899807 | 531       | 1553576899807 | 300           |

      And I send a request to add a trip
       Then The server should return a 400 status

     Scenario: A user tries to update a trip
       Given I have resampled
       When I have a trip named "Trip Name"
       And I have trip destinations
      | destinationId | arrivalDate | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531       | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531       | 1553576899807 | 300           |

      And I send a request to update a trip
       Then The server should return a 200 status

     Scenario: A user tries to get a trip
       Given I have resampled
      When I send a request to get a trip with id 1
       Then The server should return a 200 status


     Scenario: A user tries to get a trip which doesn't exist
       Given I have resampled
      When I send a request to get a trip with id 3
       Then The server should return a 404 status

     Scenario: A user tries to get a list of their trips
       Given I have resampled
      When I send a request to get trips
       Then The server should return a 200 status




