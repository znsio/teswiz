@weatherAPI @prod
Feature: Weather API

  @web
  Scenario: Validate GET current weather from Weather API
    Given GET request is sent to the weather API with valid latitude '28.6139' and longitude '77.2090'
    Then we verify weather of that location in response