@theapp
Feature: Scenarios for "The App"

#  CONFIG=./configs/theapp_local_config.properties PLATFORM=web TAG="@theapp and @switchUser" ./gradlew run
#  CONFIG=./configs/theapp_local_config.properties PLATFORM=android TAG="@theapp and @switchUser" ./gradlew run
  @android @web @switchUser @theapp
  Scenario: Switch user persona
    And "I" login to TheApp with invalid credentials - "znsio1", "invalid password"
    When "I" switch my role to "You"
    Then "You" can login again with invalid credentials - "switched user", "switched user invalid password"

#  CONFIG=./configs/theapp_local_config.properties PLATFORM=web TAG="@theapp and @invalidLogin1" ./gradlew run
#  CONFIG=./configs/theapp_local_config.properties PLATFORM=android TAG="@theapp and @invalidLogin1" ./gradlew run
  @android @web @invalidLogin @invalidLogin1 @theapp
  Scenario: Verify error message on invalid login
    Given I login with invalid credentials - "znsio1", "invalid password"
    Then I try to login again with invalid credentials - "znsio2", "another invalid password"

#  CONFIG=./configs/theapp_local_config.properties PLATFORM=web TAG="@theapp and @invalidLogin2" ./gradlew run
#  CONFIG=./configs/theapp_local_config.properties PLATFORM=android TAG="@theapp and @invalidLogin2" ./gradlew run
  @android @web @invalidLogin @invalidLogin2 @theapp
  Scenario: Another Verify error message on invalid login test
    Given I login with invalid credentials - "anotheruser1", "invalid password"
    Then I try to login again with invalid credentials - "anotheruser2", "another invalid password"

#  CONFIG=./configs/theapp_local_config.properties PLATFORM=android TAG=theapp ./gradlew run
  @android @echo @theapp
  Scenario: Verify error message on another invalid login
    Given I login with invalid credentials - "znsio2", "2nd invalid password"
    When I go back
    Then I can echo "how are you too?" in the message box

#  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android-web and @theapp" ./gradlew run
  @multiuser-android-web @theapp
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

#  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android-web and @theapp" ./gradlew run
  @multiuser-android-web @theapp
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "I", "invalid password" on "android"
    And "You" login with invalid credentials - "yoy", "invalid password" on "web"
    And "They" login with invalid credentials - "they", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"
    Then "They" login again with invalid credentials - "znsio5", "invalid password"

#  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-web and @theapp" ./gradlew run
  @multiuser-web @theapp
  Scenario: Orchestrating multiple users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "web"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

#  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android and @theapp and @2user" ./gradlew run
  @multiuser-android @2user @theapp
  Scenario: Orchestrating 2 users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    When "You" login with invalid credentials - "znsio2", "invalid password" on "android"
    Then "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "You" login again with invalid credentials - "znsio4", "invalid password"

#  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android and @theapp and @3user" ./gradlew run
  @multiuser-android @3user @theapp
  Scenario: Orchestrating 3 users on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "you" login with invalid credentials - "znsio2", "invalid password" on "android"
    And "someoneelse" login with invalid credentials - "znsio3", "invalid password" on "android"
    When "I" login again with invalid credentials - "znsio3", "invalid password"
    Then "you" login again with invalid credentials - "znsio4", "invalid password"

  #CONFIG=./configs/theapp_local_config.properties TAG="fileupload and @theapp" PLATFORM=web ./gradlew run
  @web @fileupload
  Scenario: Verify file upload
    Given I am on file upload page
    When I upload the "image" file
    Then File is uploaded successfully

    #  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-web and @theapp and @switchUser"  PLATFORM=web ./gradlew run
  @multiuser-web @theapp @web @switchUser
  Scenario: Orchestrating multiple users with changing user persona on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "web"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "You" changed to "We"
    Then "We" login again with invalid credentials - "znsio4", "invalid password"

       #  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android-web and @theapp and @switchUser"  PLATFORM=android ./gradlew run
  @multiuser-android-web @theapp @android @switchUser
  Scenario: Orchestrating multiple users with changing user persona on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "web"
    When "You" changed to "We"
    Then "We" login again with invalid credentials - "znsio4", "invalid password"


       #  CONFIG=./configs/theapp_local_config.properties TAG="@multiuser-android and @theapp and @switchUser"  PLATFORM=android ./gradlew run
  @multiuser-android @theapp @android @switchUser
  Scenario: Orchestrating multiple users with changing user persona on different platforms as part of same test
    Given "I" login with invalid credentials - "znsio1", "invalid password" on "android"
    And "You" login with invalid credentials - "znsio2", "invalid password" on "android"
    When "You" changed to "We"
    Then "We" login again with invalid credentials - "znsio4", "invalid password"