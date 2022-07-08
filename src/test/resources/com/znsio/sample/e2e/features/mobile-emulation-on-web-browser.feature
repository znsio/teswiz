@mobileEmulation
Feature: Mobile Emulation on Web browser

  @multiuser-web
  Scenario: Verify Mobile Emulation View on 2 different browsers
    Given "someone" starts "images-web" on "chrome-mobile1"
    And "someone-else" starts "bing-web" on "firefox-tab2"
    When "someone" searches for "bear"
    Then "someone-else" searches for "tiger"