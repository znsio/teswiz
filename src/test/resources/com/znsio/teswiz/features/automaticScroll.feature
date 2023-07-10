@automaticScroll
#  CONFIG=./configs/automatic_scroll_config.properties PLATFORM=android TAG=automaticScroll ./gradlew run
Feature: Automatic scroll app test

  #  CONFIG=./configs/automatic_scroll_config.properties PLATFORM=android TAG=scrollInDynamicLayer ./gradlew run
  @android @scrollInDynamicLayer
  Scenario: validating Scroll In Dynamic Layer functionality
    Given on landing page, I see the list of available apps in a dropdown list
    Then I should be able to scroll "down" in dynamic layer