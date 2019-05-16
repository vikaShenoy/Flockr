Feature: The user can log in

  Scenario: A user signs up successfully and then tries to log in
    Given that I have signed up successfully with valid data:
      | firstName | lastName | email           | password      |
      | Matias    | Gomez    | gomez@email.com | much-security |
    When I write correct login credentials in the Login form and I click the Login button
    Then the response should have an authentication token

  Scenario: A user signs up successfully and then tries to log in with incorrect credentials
    Given that I have signed up successfully with valid data:
      | firstName | lastName | email              | password    |
      | Josefina  | Pena     | josefina@email.com | catsAreCool |
    When I write incorrect login credentials in the Login form and I click the Login button
    Then the server should not log me in