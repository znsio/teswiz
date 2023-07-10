@google-search
Feature: Scenarios for "google-search"

#  CONFIG=./configs/google_search_mobile_chrome_config.properties PLATFORM=android TAG="@google-search and @android-chrome" ./gradlew run
  @android-chrome @android
  Scenario: Google search results
    Given I search for "india" in "chrome-android"
