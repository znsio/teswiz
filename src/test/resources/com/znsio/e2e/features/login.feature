@login
Feature: Test valid and invalid login

  @android @web @invalidLogin @theapp
  Scenario: Verify error message on invalid login
    Given I login with invalid credentials - "znsio1", "invalid password"
    Then I try to login again with invalid credentials - "znsio2", "another invalid password"

  @android @echo @theapp
  Scenario: Verify error message on another invalid login
    Given I login with invalid credentials - "znsio2", "2nd invalid password"
    When I go back
    Then I can echo "how are you too?" in the message box

  @multiuser-android-web @theapp
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

  @multiuser-web @theapp
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "web"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

  @multiuser-android @2user @theapp
  Scenario: Orchestrating 2 users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    When "You" login with invalid credentials - "znsio2", "invalid password" on "android"
    Then "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

#  @multiuser-android @3user @theapp
#  Scenario: Orchestrating 3 users on different platforms as part of same test
#    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
#    And "you" login with invalid credentials - "znsio2", "invalid password" on "android"
#    And "someoneelse" login with invalid credentials - "znsio3", "invalid password" on "android"
#    When "I" login again with invalid credentials - "znsio3", "invalid password"
#    Then "you" login again with invalid credentials - "znsio4", "invalid password"