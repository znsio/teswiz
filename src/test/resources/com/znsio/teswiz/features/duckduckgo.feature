@browserstack
Feature: duckduckgo test

#  CONFIG=./configs/duckduckgo/duckduckgo_local_config.properties PLATFORM=android TAG="@duckduckgo and @contextSwitch" ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/duckduckgo/duckduckgo_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=android TAG="@duckduckgo and @contextSwitch and @browserstack" ./gradlew run
  @duckduckgo @contextSwitch @android
  Scenario: Validate context switching between native and web view context
    Given I launch the duckduckgo browser
    When I cancel changing the default browser
    Then I can see the default text in the webview
    And I can switch back to the native view and enter the teswiz url
