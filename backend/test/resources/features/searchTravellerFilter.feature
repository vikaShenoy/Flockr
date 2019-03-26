Feature: The user can search travellers by one or more of the following attributes: nationality, gender, age range, type of traveller.

  Scenario: I can get a list of available nationalities from the database
    Given the back-end server is operating
    And the database has been populated with test data
    When I request nationalities from the database
    Then I get a list of all nationalities as follows:
      | nationalityId | nationalityName |
      | 1             | Australia       |
      | 2             | New Zealand     |
      | 3             | Belgium         |
      | 4             | Philippines     |
      | 5             | England         |
      | 6             | South Africa    |
      | 7             | Argentina       |
      | 8             | Japan           |


  Scenario Outline: The user can search a traveller by nationality id
    Given the back-end server is operating
    And the database has been populated with test data
    When I request travellers from the <nationalityId>
    Then I get the following <travellerNames>

    Examples:
      | nationalityId  | travellerNames                |
      | 1              | ["Barry", "Baz", "Bazza"]     |
      | 2              | ["Suzie", "Caroline", "Anna"] |
      | 4              | ["Bruce", "Fran"]             |
      | 8              | ["Stephen", "Steven"]         |
      | Not a Country  | []                            |
      | 60000000000000 | []                            |


  Scenario Outline: The user can search a traveller by gender
    Given the back-end server is operating
    And the database has been populated with test data
    When I request travellers from the <gender>
    Then I get the following <travellerNames>

    Examples:
      | gender | travellerNames                                 |
      | male   | ["Barry", "Baz", "Bazza", "Stephen", "Steven"] |
      | female | ["Suzie", "Caroline", "Anna", "Fran"]          |
      | other  | ["Bruce"]                                      |


  Scenario Outline: The user can search a traveller by age in a range
    Given the back-end server is operating
    And the database has been populated with test data
    When I request travellers in the range <minAge> to <maxAge>
    Then I get the following <results>


    Examples:
      | minAge | maxAge | results                                   |
      | 18     | 20     | ["Barry", "Baz", "Bazza"]                 |
      | 20     | 100    | ["Caroline", "Anna", "Stephen", "Steven"] |
      | 4000   | 4001   | []                                        |
      | 100    | 130    | ["Suzie"]                                 |


  Scenario Outline: The user can search a traveller by traveller type
    Given the back-end server is operating
    And the database has been populated with test data
    When I request travellers of the type <travellerTypeId>
    Then I get the following <results>

    Examples:
      | travellerTypeId | results                       |
      | 1               | ["Barry", "Baz", "Bazza"]     |
      | 2               | ["Suzie", "Caroline", "Anna"] |
      | 3               | ["Stephen", "Steven"]         |