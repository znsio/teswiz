@weatherAPI @prod
Feature: WeatherAPI

  Scenario: Validate Data from Weather GET API
    Given GET request is sent to the weather API with valid 'latitude' and 'longitude'
    Then We Verify Weather of that Location in Response