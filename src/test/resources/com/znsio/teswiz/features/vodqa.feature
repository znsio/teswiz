@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using valid credentials
    When I scroll from one to another element point on vertical swiping screen
    Then Element text should be visible

  @android
  Scenario: Validate that user is able to scroll down by screen size
    Given I login to vodqa application using valid credentials
    When I scroll down by screen size on vertical swiping screen
    Then Element lower in the list should be visible