Feature: As a registered user, I can make proposals for public destinations
  that admins should be able to accept or reject

  Background:
    Given users with the following information exist:
      | firstName | lastName     | email                 | password    |
      | Phillip   | Phototaker | ppta@phillipsphotos.com | say-cheese |
      | Angelo    | Admin      | aaa@admin.com           | its-ya-boi |

    @DestinationProposal
    Scenario: A user can create a proposal for a public destination
      Given that I have the following destinations:
       | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
       | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
      When I propose the following traveller types
       | travellerTypeId |
       | 1               |
       | 2               |
       | 3               |
      Then the proposal should be made

    @DestinationProposal
    Scenario: A user cannot create a proposal for a public destination with a traveller type that does not exist
      Given that I have the following destinations:
       | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
       | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
      When I propose the following traveller types
       | travellerTypeId |
       | 1               |
       | 300             |
       | 3               |
      Then the proposal should not be made

    @DestinationProposal
    Scenario: A user cannot create a proposal for a public destination with duplicate traveller types
      Given that I have the following destinations:
       | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
       | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
      When I propose the following traveller types
       | travellerTypeId |
       | 1               |
       | 3               |
       | 3               |
      Then the proposal should not be made





