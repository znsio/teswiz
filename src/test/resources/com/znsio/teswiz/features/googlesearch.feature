@google-search
Feature: Search for flight options

#  CONFIG=./configs/google_search_mobile_chorme_config.properties PLATFORM=android TAG="@google-search and @android-chrome" ./gradlew run
  @android-chrome @android
  Scenario: Search and verify google search results
    Given I search for "india" in "chrome-android"
