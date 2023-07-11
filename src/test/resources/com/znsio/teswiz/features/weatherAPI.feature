@weatherAPI @api
Feature: Weather API
# CONFIG=./configs/weatherAPI_local_config.properties TAG=weatherAPI PLATFORM=api ./gradlew run

  Scenario: Validate current temperature for given location
    Given I send GET request with valid location coordinates
    When I query key "current_weather" in response
    Then I verify temperature of that location in range 0 and 55 C

  Scenario: Validate temperature forecast above 16 days throws error
    Given I send GET request with valid location coordinates and invalid forecast days
    Then I verify error reason "Forecast days is invalid. Allowed range 0 to 16."