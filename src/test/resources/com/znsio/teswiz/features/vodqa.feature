@vodqa

Feature: Vodqa test

  @android
  Scenario: Scrolling using fromPoint and toPoint
    Given I login to vodqa application using credentials
    When I click on Vertical Swiping
    And I scroll to element "Jasmine"
    Then "Jasmine" element should be visible