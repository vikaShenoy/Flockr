Feature: The user can manage trips

  Background:
    Given users with the following information exist:
      | firstName | lastName  | email         | password   |
      | Terry     | Triptaker | ttt@email.com | triptastic |

  #Test that a trip can be created
  Scenario: A user tries to create a trip
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I click the Add Trip button
    Then The server should return a 201 status indicating the Trip is successfully created

  Scenario: A user tries to add a trip with less then 2 trip destinations
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
    And  I click the Add Trip button
    Then The server should return a 400 status indicating the Trip is not successfully created

  Scenario: A user tries to add a trip with adjacent destination ID's
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |

    And I click the Add Trip button
    Then The server should return a 400 status indicating the Trip is not successfully created

  Scenario: A user tries to update a trip
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I click the Add Trip button
    Then The server should return a 201 status indicating the Trip is successfully created
    When I update a trip and click the Save Trip button
    Then The server should return a 200 status indicating the Trip is successfully updated

  Scenario: A user tries to get a trip
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I click the Add Trip button
    Then The server should return a 201 status indicating the Trip is successfully created
    When I send a request to get a trip with id 1
    Then The server should return a 200 status indicating the Trip exists

  Scenario: A user tries to get a trip which doesn't exist
    When I send a request to get a trip with id 3
    Then The server should return a 404 status indicating the Trip is not found

  Scenario: A user tries to get a list of their trips
    When I send a request to get trips
    Then The server should return a 200 status indicating the Trip exists

