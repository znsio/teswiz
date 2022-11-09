Feature: Amazon Test Scenario

  @web @amazon
  Scenario: User should be able to search and add the item to cart.
    Given I logged in to Amazon with valid credentials
    When I searched for "iPhone 13" and select first result
    And I add "iPhone 13" to the cart
    Then Cart should have searched item "iPhone 13"