@weatherAPI @api
Feature: Weather data based on location Coordinates
# CONFIG=./configs/api_local_config.properties TAG=weatherAPI PLATFORM=api ./gradlew run

  @windDirectionScenario @workflow
  Scenario: Verify the wind direction for city - "San Francisco" using location coordinates
    Given I fetch location coordinates for "San Francisco"
    When I fetch weather data for the location coordinates
    Then wind direction should be less than 360