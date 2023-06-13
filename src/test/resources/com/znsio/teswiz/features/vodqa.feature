@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using credentials
    When I click on Vertical Swiping
    And I scroll from "C" to "Ruby" element
    Then "Jasmine" element should be visible