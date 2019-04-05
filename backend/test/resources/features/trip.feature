Feature: The user can manage trips

  Background:
    Given this user exists:
      | firstName | middleName | lastName | email             | password      |
      | Felipe    | Rogelio    | Sanchez  | rogelio@email.com | much-security |

  #Test that a trip can be created
  Scenario: A user tries to create a trip
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I send a request to add a trip
    Then The server should return a 201 status

  Scenario: A user tries to add a trip with less then 2 trip destinations
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
    And I send a request to add a trip
    Then The server should return a 400 status

  Scenario: A user tries to add a trip with adjacent destination ID's
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |

    And I send a request to add a trip
    Then The server should return a 400 status

  Scenario: A user tries to update a trip
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I send a request to add a trip
    Then The server should return a 201 status
    When I send a request to update a trip
    Then The server should return a 200 status

  Scenario: A user tries to get a trip
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    And I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 2             | 1553576899807 | 531         | 1553576899807 | 300           |
    When I send a request to add a trip
    Then The server should return a 201 status
    When I send a request to get a trip with id 1
    Then The server should return a 200 status


  Scenario: A user tries to get a trip which doesn't exist
    Given I log in with email "rogelio@email.com" and password "much-security"
    When I send a request to get a trip with id 3
    Then The server should return a 404 status

  Scenario: A user tries to get a list of their trips
    Given I log in with email "rogelio@email.com" and password "much-security"
    When I send a request to get trips
    Then The server should return a 200 status




