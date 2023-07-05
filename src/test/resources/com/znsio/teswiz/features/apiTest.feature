@weatherAPI
Feature: Weather API
# CONFIG=./configs/weatherAPI_local_config.properties TAG=weatherAPI PLATFORM=api ./gradlew run
  @api
  Scenario: Validate GET current weather from Weather API
    Given GET request is sent to the weather API with valid location coordinates
    Then we verify weather of that location in response