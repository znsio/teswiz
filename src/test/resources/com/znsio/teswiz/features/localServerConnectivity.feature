@google-search
Feature: Scenarios for testing local app connectivity when running from BrowserStack

#  CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY CONFIG=./configs/googlesearch/googlesearch_browserstack_android_chrome_config.properties PLATFORM=web TAG="@google-search and @android-chrome" ./gradlew run
  @browserstack @web-chrome @web
  Scenario: Check local app connectivity from android-browserstack using Selenium
    Given I search for "india" in "chrome-web"
    Given I connect to local App's "products" endpoint

