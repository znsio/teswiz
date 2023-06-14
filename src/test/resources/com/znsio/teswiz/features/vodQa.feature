@vodqa
Feature: Scenarios for "vodqa"

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqaContextSwitch ./gradlew run
  @vodqaContextSwitch @android
  Scenario: Validate context switching between native and web view
    Given I login to vodqa application using valid credentials
    When I enter into hacker news under webview section
    Then I am able to see hacker news login option in web view
    When I enter into native view section
    Then I am able to retrieve the native view section elements

