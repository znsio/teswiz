@applitools
Feature: Scenarios for "Applitools"

  #IS_VISUAL=true CONFIG=./configs/applitools/applitools_local_web_config.properties ./gradlew run
  @web @figma
  Scenario: Compare Applitools important pages with Figma design
    Given I have my Figma design with app name "Applitools website", test name "Applitools Full Pages" and baseline name "Applitools Full Pages_1506" available in Applitools
    And I visually check the "integrations page" at "https://applitools.com/platform/integrations/"
    And I visually check the "what's new page" at "https://applitools.com/platform/whats-new/"

  #IS_VISUAL=true CONFIG=./configs/applitools/applitools_local_web_config.properties ./gradlew run
  @web
  Scenario: Compare Applitools important pages
    Given I go the application page
    And I visually check the "integrations page" at "https://applitools.com/platform/integrations/"
    And I visually check the "what's new page" at "https://applitools.com/platform/whats-new/"

