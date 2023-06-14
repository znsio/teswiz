@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using valid credentials
    When I scroll from one to another element point on vertical swiping screen
    Then Element text should be visible

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: User tap in the middle of the screen
    Given I login to vodqa application using valid credentials
    When I tap in the middle of the screen
    Then I am able to move from "Samples List" page to next page

