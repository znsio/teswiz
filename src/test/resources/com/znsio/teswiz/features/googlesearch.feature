@google-search
Feature: Scenarios for "google-search"

#  CONFIG=./configs/googlesearch/googlesearch_android_chrome_config.properties PLATFORM=android TAG="@google-search and @android-chrome" ./gradlew run
  @android-chrome @android
  Scenario: Google search results in local emulator using appium
    Given I search for "india" in "chrome-android"

#  CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY CONFIG=./configs/googlesearch/googlesearch_browserstack_android_chrome_config.properties PLATFORM=web TAG="@google-search and @android-chrome" ./gradlew run
  @browserstack @android-chrome @web
  Scenario: Google search results in android-browserstack using Selenium
    Given I search for "india" in "chrome-web"
