@multidevice
Feature: Test valid and invalid login

  @multiuser-android
  Scenario: Verify 2 different calculator apps orchestration
    Given "I" start "Calculator2-Android"
    And "you" start "Calculator-Android"
    And "you" select "2"
    And "you" press "plus"
    And "I" select "5"
    And "you" press "plus"

  @multiuser-android @wip
  Scenario: Verify 2 different apps orchestration
    Given "I" start "theapp-Android"
    And "you" start "Calculator-Android"
    And "you" select "2"
    And "you" press "plus"
    When "I" login with invalid credentials - "znsio5", "invalid password"
    Then "you" select "5"

  @multiuser-web
  Scenario: Verify 2 different website orchestration
    Given "someone" starts "images-web" on "chrome"
    And "someone-else" starts "bing-web" on "firefox"
    When "someone" searches for "bear"
    Then "someone-else" searches for "tiger"
