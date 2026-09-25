@pet @crud
Feature: Pet CRUD operations
  As a client of the Petstore API
  I want to create, read, update and delete pets
  So that the store's pet inventory can be managed

  @smoke @create
  Scenario: Create a new pet
    Given a new pet named "Rex" in category "Dogs" with status "available"
    When I add the pet to the store
    Then the response status code should be 200
    And the response content type should be JSON
    And the response body should match the "pet" JSON schema
    And the response body should describe the pet
    And the pet should be retrievable by its id

  @smoke @read
  Scenario: Retrieve an existing pet by its id
    Given an existing pet named "Bella" in category "Cats" with status "pending"
    When I retrieve the pet by its id
    Then the response status code should be 200
    And the response content type should be JSON
    And the response header "Access-Control-Allow-Origin" should be "*"
    And the response body should match the "pet" JSON schema
    And the response body should describe the pet

  @update
  Scenario: Update an existing pet
    Given an existing pet named "Max" in category "Dogs" with status "available"
    When I update the pet with name "Max Senior" and status "sold"
    Then the response status code should be 200
    And the response content type should be JSON
    And the response body should describe the pet
    And retrieving the pet should return name "Max Senior" and status "sold"

  @update
  Scenario: Update an existing pet with form data
    Given an existing pet named "Luna" in category "Cats" with status "available"
    When I update the pet via form with name "Luna Star" and status "pending"
    Then the response status code should be 200
    And the response body should match the "api-response" JSON schema
    And the response message should be the pet id
    And retrieving the pet should return name "Luna Star" and status "pending"

  @delete
  Scenario: Delete an existing pet
    Given an existing pet named "Charlie" in category "Dogs" with status "sold"
    When I delete the pet
    Then the response status code should be 200
    And the response body should match the "api-response" JSON schema
    And the response message should be the pet id
    And the pet should no longer be retrievable
