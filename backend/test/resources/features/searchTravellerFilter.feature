Feature: The user can search travellers

  Background:
    Given the following user exists:
      | firstName | middleName | lastName | email             | password      |
      | Felipe    | Rogelio    | Sanchez  | rogelio@email.com | much-security |

  Scenario: I can get a list of available nationalities from the database
    And I have logged in with email "rogelio@email.com" and password "much-security"
    And I populate the database with test data
    When I want all types of nationalities from the database
    Then I get a list of all nationalities as follows:
      | nationalityId | nationalityName |
      | 1             | Chile           |
      | 2             | Mexico          |
      | 3             | New Zealand     |
      | 4             | Australia       |
      | 5             | Afghanistan     |
      | 6             | Peru            |

  Scenario Outline: The user can search a traveller by nationality id
    And I have logged in with email "rogelio@email.com" and password "much-security"
    And I populate the database with test data
    When I search travellers with the <nationalityId> nationality id
    Then I get the following <emails> emails

    Examples:
      | nationalityId | emails                     |
      | 3             | ["stoney-steve@gmail.com"] |
      | 5             | ["p.andre@hotmail.com"]    |
      | 6             | ["luis@gmail.com"]         |

  Scenario Outline: The user can search a traveller by gender
    And I have logged in with email "rogelio@email.com" and password "much-security"
    And I populate the database with test data
    When I search travellers with the gender <gender>
    Then I get the following <emails> emails

    Examples:
      | gender | emails                     |
      | Male   | ["luis@gmail.com"]         |
      | Female | ["stoney-steve@gmail.com"] |
      | Other  | ["p.andre@hotmail.com"]    |


  Scenario Outline: The user can search a traveller by traveller type
    And I have logged in with email "rogelio@email.com" and password "much-security"
    And I populate the database with test data
    When I search travellers with the traveller type ID <travellerTypeId>
    Then I get the following <emails> emails

    Examples:
      | travellerTypeId | emails                     |
      | 1               | ["stoney-steve@gmail.com"] |
      | 2               | ["p.andre@hotmail.com"]    |
      | 7               | ["luis@gmail.com"]         |