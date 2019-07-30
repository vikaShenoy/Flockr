Feature: As a registered user, I can make proposals for public destinations
  that admins should be able to accept or reject

  Background:
    Given users with the following information exist:
      | firstName | lastName     | email                 | password   | isAdmin |
      | Phillip   | Phototaker | ppta@phillipsphotos.com | say-cheese | false   |
      | Angelo    | Admin      | aaa@admin.com           | its-ya-boi | true    |
    And An admin user exists

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

      @DestinationProposal
      Scenario: An admin can accept a proposal
       Given that I have the following destinations:
         | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
         | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
       And I propose the following traveller types
         | travellerTypeId |
         | 1               |
         | 2               |
         | 3               |
       When an admin accepts the proposal
       Then the proposal is accepted

      @DestinationProposal
      Scenario: A user that is not an admin cannot accept a proposal
       Given that I have the following destinations:
         | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
         | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
       And I propose the following traveller types
         | travellerTypeId |
         | 1               |
         | 2               |
         | 3               |
       When a user tries to accept the proposal
       Then the proposal is not accepted




      @DestinationProposal
      Scenario: An admin can succesfully reject a destination proposal
       Given that I have the following destinations:
         | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
         | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
       And I propose the following traveller types
         | travellerTypeId |
         | 1               |
         | 2               |
         | 3               |
       When an admin rejects the proposal
       Then the proposal is rejected

      @DestinationProposal
      Scenario: A user cannot reject a destination proposal
       Given that I have the following destinations:
         | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
         | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
       And I propose the following traveller types
         | travellerTypeId |
         | 1               |
         | 2               |
         | 3               |
       When a user tries to reject the proposal
       Then the proposal is not rejected


      @DestinationProposal
      Scenario: A user cannot reject a destination proposal
      Given that I have the following destinations:
        | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
        | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
      And I propose the following traveller types
        | travellerTypeId |
        | 1               |
        | 2               |
        | 3               |
      When an admin tries to reject a proposal that does not exist
      Then the proposal is not rejected

      @DestinationProposal
      Scenario: An admin can get a list of proposals
       Given that I have the following destinations:
        | destinationName           | destinationTypeId | districtId | latitude | longitude | countryId | isPublic |
        | The Dairy Down The Street | 1                 | 1          | 41.2     | 174.9     | 1         | true     |
      And I propose the following traveller types
        | travellerTypeId |
        | 1               |
        | 2               |
        | 3               |
      When an admin requests all proposals
      Then the correct data will be returned






