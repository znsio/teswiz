@restUserAPI @api
Feature: CRUD operations using JSONPlaceHolder api
# CONFIG=./configs/api_local_config.properties TAG=restUserAPI PLATFORM=api ./gradlew run

  @createAndUpdatePost @workflow
  Scenario: Verify we are able to update and delete a new post
    Given I create a new post
    And I modify title of the created post
    Then the title of the post should be updated
    When I delete the modified post
    Then the post should be deleted successfully
