Feature: The server endpoints are live

  # Smoke test backend index
  # Removing request to "/" endpoint until we have vue being served by play sorted
#  Scenario: A GET request is sent to the backend server's index
#    When I do a "GET" request on "/"
#    Then the response should have a 200 status code

  # Smoke test backend auth endpoint with no request bodies
  Scenario: A POST request is sent to the backend server's /auth/users/signup endpoint without a body
    When I do a "POST" request on "/api/auth/users/signup"
    Then the response should have a 400 status code