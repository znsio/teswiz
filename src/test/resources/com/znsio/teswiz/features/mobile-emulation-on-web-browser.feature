@mobileEmulation
Feature: Mobile Emulation on Web browser

#  CONFIG=./configs/jiomeet_local_config.properties TAG="@mobileEmulation and @multiuser-web" ./gradlew run
  @multiuser-web
  Scenario: Verify Mobile Emulation View on 2 different browsers
    Given "someone" starts "images-web" on "chrome-mobile1"
    And "someone-else" starts "bing-web" on "firefox-tab2"
    When "someone" searches for "bear"
    Then "someone-else" searches for "tiger"

    #  CONFIG=./configs/android_emulator_config.properties TAG="@mobileEmulation and @android-chrome" ./gradlew run
  @android-chrome @android
  Scenario: Sample test to launch chrome on android emulator
    Given "I" starts "chrome-android" on "chrome"
    When "I" searches for "bear"