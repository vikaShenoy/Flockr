Feature: The user can log in

  Scenario: A user signs up successfully and then tries to log in
    Given that I have signed up successfully with valid data:
      | firstName | middleName | lastName | email           | password      |
      | Matias    | Felipe     | Gomez    | gomez@email.com | much-security |
    When I make a "POST" request to "/api/auth/users/login" with my email and password
    Then the response should have an authentication token

  Scenario: A user signs up successfully and then tries to log in with incorrect credentials
    Given that I have signed up successfully with valid data:
      | firstName | middleName | lastName | email              | password    |
      | Josefina  | Manuela    | Pena     | josefina@email.com | catsAreCool |
    When I make a "POST" request to "/api/auth/users/login" with my email and the wrong password
    Then the server should not log me in