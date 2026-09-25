@pet @search
Feature: Find pets by status
  As a client of the Petstore API
  I want to search pets by their status
  So that I can list pets that are available, pending or sold

  Scenario Outline: Pets with status "<status>" are found by that status
    Given an existing pet named "Finder" in category "Dogs" with status "<status>"
    When I search for pets with status "<status>"
    Then the response status code should be 200
    And the response content type should be JSON
    And every pet in the response should have status "<status>"
    And the search results for status "<status>" should include the pet

    Examples:
      | status    |
      | available |
      | pending   |
      | sold      |

  @negative
  Scenario: Searching by a status no pet has returns an empty list
    When I search for pets with a status that no pet has
    Then the response status code should be 200
    And the response should be an empty list
