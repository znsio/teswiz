@weatherAPI @api
Feature: Weather API
# CONFIG=./configs/weatherAPI_local_config.properties TAG=weatherAPI PLATFORM=api ./gradlew run

  Scenario: Validate current temperature for given location
    Given I send GET request with valid location coordinates
    When I query key "current_weather" in response
    Then I verify temperature of that location in range 0 and 55 C

  Scenario: Validate weather code for given location
    Given I send GET request with valid location coordinates
    When I query key "current_weather" in response
    Then I verify "weathercode" is 96