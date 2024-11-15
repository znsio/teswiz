@api @cli
Feature: Test the calculator app (interactive CLI)

  # CONFIG=configs/api_local_config.properties TAG=@cli ./gradlew run
  Scenario: Test the calculator app
    Given I launch the interactive CLI for calculator
    When I add 2 numbers - 24 and 43
    And I subtract 2 numbers - 43 and 24
    And I see the invalid messages
    Then I can close the interactive CLI for calculator
