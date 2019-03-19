Feature: The server endpoints are live

  # Smoke test backend index
  Scenario: A GET request is sent to the backend server's index
    When I do a "GET" request on "/"
    Then the response should have a 200 status code

  # Smoke test backend auth endpoint with no request bodies
  Scenario: A POST request is sent to the backend server's /auth/travellers/signup endpoint without a proper body
    When I do a "POST" request on "/api/auth/travellers/signup"
    Then the response should have a 400 status code

  Scenario: A POST request is sent to the backend server's /auth/travellers/login endpoint without a proper body
    When I do a "POST" request on "/api/auth/travellers/login"
    Then the response should have a 400 status code

  Scenario: A POST request is sent to the backend server's /auth/traveller/logout endpoint without a proper body
    When I do a "POST" request on "/api/auth/travellers/logout"
    Then the response should have a 401 status code