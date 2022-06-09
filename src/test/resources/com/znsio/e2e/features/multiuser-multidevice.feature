@multidevice
Feature: Test valid and invalid login

  @multiuser-android @calculator
  Scenario: Verify 2 different calculator apps orchestration
    Given "I" start "Calculator2-Android"
    And "you" start "Calculator-Android"
    And "you" select "2"
    And "you" press "plus"
    And "I" select "5"
    And "you" press "plus"

  @multiuser-android @calculator-theapp
  Scenario: Verify 2 different apps orchestration
    Given "you" start "Calculator-Android"
    And "I" start "theapp-Android"
    When "you" select "2"
    And "you" press "plus"
    And "I" login with invalid credentials - "znsio5", "invalid password"
    Then "you" select "5"

  @multiuser-web
  Scenario: Verify 2 different website orchestration
    Given "someone" starts "images-web" on "chrome"
    And "someone-else" starts "bing-web" on "firefox"
    When "someone" searches for "bear"
    Then "someone-else" searches for "tiger"

  @multiuser-android-web
  Scenario: Verify 2 different apps and 2 web sites orchestration
    Given "you" start "Calculator-Android"
    And "I" start "theapp-Android"
    And "someone" starts "images-web" on "chrome"
    And "someone-else" starts "bing-web" on "firefox"
    Then "I" login with invalid credentials - "znsio5", "invalid password"
    When "someone" searches for "bear"
    And "someone-else" searches for "tiger"
    And "you" select "2"
    And "you" press "plus"
    And "you" select "5"

