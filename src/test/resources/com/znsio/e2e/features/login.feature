@login
Feature: Test valid and invalid login

  @android @web
  Scenario: Verify error message on invalid login
    When I login with invalid credentials - "znsio1", "invalid password"
#        Then I see the error "Invalid login credentials, please try again"
#        When I dismiss the alert
#        And I login with invalid credentials - "znsio", "znsio"
#        Then I see the error "Invalid login credentials, please try again"

  @multiuser-android-web
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "you" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "you" login again with invalid credentials - "znsio4", "invalid password"

  @multiuser-android
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "you" login with invalid credentials - "znsio2", "invalid password" on "android"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "you" login again with invalid credentials - "znsio4", "invalid password"