Feature: The user can search travellers

  Scenario: I can get a list of available nationalities from the database
    And I have logged in with email "luis@gmail.com" and password "so-secure"
    And the database has been populated with test data
    When I request nationalities from the database
    Then I get a list of all nationalities as follows:
      | nationalityId | nationalityName |
      | 1             | New Zealand     |
      | 2             | Australia       |
      | 3             | Afghanistan     |
      | 4             | Peru            |


  Scenario Outline: The user can search a traveller by nationality id
    And I have logged in with email "luis@gmail.com" and password "so-secure"
    And the database has been populated with test data
    When I request travellers from the <nationalityId> nationality id
    Then I get the following <emails> emails

    Examples:
      | nationalityId | emails                     |
      | 1             | ["stoney-steve@gmail.com"] |
      | 3             | ["p.andre@hotmail.com"]    |
      | 4             | ["luis@gmail.com"]         |


  Scenario Outline: The user can search a traveller by gender
    And I have logged in with email "luis@gmail.com" and password "so-secure"
    And the database has been populated with test data
    When I request travellers from the <gender> gender
    Then I get the following <emails> emails

    Examples:
      | gender | emails                     |
      | Male   | ["luis@gmail.com"]         |
      | Female | ["stoney-steve@gmail.com"] |
      | Other  | ["p.andre@hotmail.com"]    |


#  Scenario Outline: The user can search a traveller by age in a range
#    Given the backend server is operating
#    And I have logged in with email "luis@gmail.com" and password "so-secure"
#    And the database has been populated with test data
#    When I request travellers in the range <minAge> to <maxAge>
#    Then I get the following <results>
#
#
#    Examples:
#      | minAge | maxAge | results                                   |
#      | 18     | 20     | ["Barry", "Baz", "Bazza"]                 |
#      | 20     | 100    | ["Caroline", "Anna", "Stephen", "Steven"] |
#      | 4000   | 4001   | []                                        |
#      | 100    | 130    | ["Suzie"]                                 |


  Scenario Outline: The user can search a traveller by traveller type
    And I have logged in with email "luis@gmail.com" and password "so-secure"
    And the database has been populated with test data
    When I request travellers of the type <travellerTypeId>
    Then I get the following <emails> emails

    Examples:
      | travellerTypeId | emails                     |
      | 1               | ["stoney-steve@gmail.com"] |
      | 2               | ["p.andre@hotmail.com"]    |
      | 7               | ["luis@gmail.com"]         |