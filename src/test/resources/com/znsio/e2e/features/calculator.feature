@calculator
Feature: Calculator test

  @android
  Scenario: Calculations
    Given I start the calculator
    When I select "2"
    And I press "plus"
    When I select "5"
    And I press "equals"
    Then I should see "7"