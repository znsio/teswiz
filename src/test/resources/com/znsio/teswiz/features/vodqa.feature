@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using credentials
    When I scroll from "C" to "Ruby" element on vertical swiping screen
    Then "Jasmine" element should be visible