@pet @negative
Feature: Pet API error handling
  As a client of the Petstore API
  I want invalid requests to be rejected with meaningful errors
  So that I can detect and handle mistakes

  Scenario: Retrieve a pet that does not exist
    Given a pet id that does not exist in the store
    When I retrieve the pet by its id
    Then the response status code should be 404
    And the response content type should be JSON
    And the response body should match the "api-response" JSON schema
    And the error response should have code 1, type "error" and message "Pet not found"

  Scenario: Delete a pet that does not exist
    Given a pet id that does not exist in the store
    When I delete the pet
    Then the response status code should be 404

  Scenario: Update a pet that does not exist with form data
    Given a pet id that does not exist in the store
    When I update the pet via form with name "Ghost" and status "sold"
    Then the response status code should be 404
    And the error response should have code 404, type "unknown" and message "not found"

  # The Swagger definition documents 400 for an invalid id; the server actually answers 404
  # because the path parameter cannot be converted to a number. The tests assert real behaviour.
  Scenario Outline: Retrieve a pet using a non-numeric id "<invalid id>"
    When I retrieve the pet with id "<invalid id>"
    Then the response status code should be 404
    And the response content type should be JSON
    And the error response code should be 404

    Examples:
      | invalid id |
      | abc        |
      | 1.5        |

  # The Swagger definition documents 405 for invalid input; the server answers 400 "bad input".
  Scenario: Add a pet with a malformed JSON body
    When I add a pet with the request body:
      """
      {"id": 1, "name": "Broken",
      """
    Then the response status code should be 400
    And the response content type should be JSON
    And the error response should have code 400, type "unknown" and message "bad input"
