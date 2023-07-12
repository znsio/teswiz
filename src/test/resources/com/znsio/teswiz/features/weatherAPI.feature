@weatherAPI @api
Feature: Weather API
# CONFIG=./configs/api_local_config.properties TAG=weatherAPI PLATFORM=api ./gradlew run

  Scenario: Validate current temperature for location - New Delhi
    Given I send GET request with location coordinates
    Then temperature of that location should be in range 0 and 55 C

  Scenario: Verify we are unable to fetch temperature for more than 16 days
    Given I send GET request with location coordinates and invalid forecast days
    Then error message should be "Forecast days is invalid. Allowed range 0 to 16."

  Scenario Outline: Validate wind speed for location - Mumbai, Delhi, Kolkata, Chennai
    Given I send GET request with valid location coordinates <latitude> and <longitude>
    Then wind speed of that location should be in range 0 and 90 kmph
    Examples:
    | latitude | longitude |
    | "18.987807" | "72.836447" |
    | "28.651952" | "77.231495" |
    | "22.562627" | "88.363044" |
    | "13.084622" | "80.248357" |

  Scenario: Verify the temperature for city - "San Francisco" using location coordinates
    Given I send GET request for city "San Francisco"
    When I fetch latitude and longitude using city name
    Then wind direction should be less than 360
