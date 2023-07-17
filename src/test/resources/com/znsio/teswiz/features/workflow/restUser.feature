@restUserAPI @api
Feature: CRUD operations using JSONPlaceHolder api
# CONFIG=./configs/api_local_config.properties TAG=restUserAPI PLATFORM=api ./gradlew run

  @createAndUpdatePost @workflow
  Scenario: Create a post then update title and finally delete the post
    Given I create a post
    Then verify post created
    When I update title of the post
    Then verify title updated
    Then I delete the post
