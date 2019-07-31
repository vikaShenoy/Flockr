Feature: The user can search travellers

  Background:
    Given users with the following information exist:
      | firstName | lastName | email                    | password           |
      | Sally     | Searcher | sally.searcher@email.com | me-gusta-encontrar |
    And full users with the following information exist:
      | firstName | middleName | lastName | password     | email                  | gender | nationality | passport    | travellerType | role        | dateOfBirth |
      | Luis      | Sebastian  | Ruiz     | so-secure    | luis@gmail.com         | Male   | Peru        | Bolivia     | Backpacker    | SUPER_ADMIN | 637920534   |
      | Peter     |            | Andre    | in-your-town | p.andre@hotmail.com    | Other  | Afghanistan | Peru        | Thrillseeker  | ADMIN       | 637920534   |
      | Steven    | middle     | Austin   | stone-cold   | stoney.steve@gmail.com | Female | New Zealand | New Zealand | Groupies      |             | 637920534   |

  Scenario: I can get a list of available nationalities from the database
    When I want all types of nationalities from the database
    Then I get a list of all nationalities as follows:
      | nationalityId | nationalityName |
      | 5             | Afghanistan     |
      | 4             | Australia       |
      | 1             | Chile           |
      | 2             | Mexico          |
      | 3             | New Zealand     |
      | 6             | Peru            |

  Scenario Outline: The user can search a traveller by nationality id
    When I search travellers with the <nationalityId> nationality id
    Then I get the following <emails> emails

    Examples:
      | nationalityId | emails                     |
      | 3             | ["stoney.steve@gmail.com"] |
      | 5             | ["p.andre@hotmail.com"]    |
      | 6             | ["luis@gmail.com"]         |

  Scenario Outline: The user can search a traveller by gender
    When I search travellers with the gender <gender>
    Then I get the following <emails> emails

    Examples:
      | gender | emails                     |
      | Male   | ["luis@gmail.com"]         |
      | Female | ["stoney.steve@gmail.com"] |
      | Other  | ["p.andre@hotmail.com"]    |


  Scenario Outline: The user can search a traveller by traveller type
    When I search travellers with the traveller type ID <travellerTypeId>
    Then I get the following <emails> emails

    Examples:
      | travellerTypeId | emails                     |
      | 1               | ["stoney.steve@gmail.com"] |
      | 2               | ["p.andre@hotmail.com"]    |
      | 7               | ["luis@gmail.com"]         |