@mobileEmulation
Feature: Mobile Emulation on Web browser

#  CONFIG=./configs/jio/jiomeet_local_config.properties TAG="@mobileEmulation and @multiuser-web" ./gradlew run
  @multiuser-web
  Scenario: Verify Mobile Emulation View on 2 different browsers
    Given "someone" starts "images-web" on "chrome-mobile1"
    And "someone-else" starts "bing-web" on "chrome-mobile2"
    When "someone" searches for "bear"
    Then "someone-else" searches for "tiger"