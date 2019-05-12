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
    When I click the Add Trip button
    Then The server should return a 201 status indicating the Trip is successfully created

  Scenario: A user tries to add a trip with less then 2 trip destinations
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
    And  I click the Add Trip button
    Then The server should return a 400 status indicating the Trip is not successfully created

  Scenario: A user tries to add a trip with adjacent destination ID's
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
    When I have a trip named "Trip Name"
    And I have trip destinations
      | destinationId | arrivalDate   | arrivalTime | departureDate | departureTime |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |
      | 1             | 1553576899807 | 531         | 1553576899807 | 300           |

    And I click the Add Trip button
    Then The server should return a 400 status indicating the Trip is not successfully created

  Scenario: A user tries to update a trip
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
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
    Given I log in with email "rogelio@email.com" and password "much-security"
    And Destinations have been added to the database
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
    Given I log in with email "rogelio@email.com" and password "much-security"
    When I send a request to get a trip with id 3
    Then The server should return a 404 status indicating the Trip is not found

  Scenario: A user tries to get a list of their trips
    Given I log in with email "rogelio@email.com" and password "much-security"
    When I send a request to get trips
    Then The server should return a 200 status indicating the Trip exists

