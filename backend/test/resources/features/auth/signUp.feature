Feature: The user can sign up

  # Test that a 200 code is returned on valid data
  Scenario: A user tries to sign up with valid data with middle name
    Given that I have valid user data to sign up:
      | firstName | middleName | lastName | email        | password        |
      | Matias   | Felipe   | Suarez | matias@email.com  | much-security |
    When I make a "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 200 status code

  # Test that a 400 code is returned on *some* incomplete data
  Scenario: A user tries to sign up without a lastname
    Given that I have incomplete user data to sign up:
      | firstName | middleName | email            | password |
      | Jose      | Manuel      | jose@gmail.com | much-security |
    When I make an "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 400 status code

  Scenario: A user tries to sign up without a password
    Given that I have incomplete user data to sign up:
      | firstName | middleName | lastName     | email            |
      | Jose    | Manuel   | Rodriguez  | jose@gmail.com |
    When I make an "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 400 status code

  Scenario: A user tries to sign up without an email
    Given that I have incomplete user data to sign up:
      | firstName | middleName | lastName     | password    |
      | Jose    | Manuel   | Rodriguez  | so-secure |
    When I make an "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 400 status code

  Scenario: A user tries to sign up without a middle name
    Given that I have incomplete user data to sign up:
      | firstName  | lastName | email                 | password      |
      | Josefina | Perez  | josefina@email.com  | so-security |
    When I make an "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 200 status code

  Scenario: A user tries to sign up without a first name
    Given that I have incomplete user data to sign up:
      | middleName  | lastName | email                 | password      |
      | Pablo     | Perez  | josefina@email.com  | so-security |
    When I make an "POST" request to "/api/auth/travellers/signup" with the data
    Then I should receive a 400 status code