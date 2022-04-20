@multidevice
Feature: Test valid and invalid login

  @multiuser-android
  Scenario: Verify error message on invalid login
    Given "I" start "Calculator2-Android"
    And "you" start "Calculator-Android"
    And "you" select "2"
    And "you" press "plus"
    And "I" select "5"
    And "you" press "plus"