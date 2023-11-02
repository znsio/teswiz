@dineout
Feature: Search for restaurants

#  CONFIG=./configs/dineout/dineout_local_config.properties PLATFORM=android TAG=dineout ./gradlew run
#  CONFIG=./configs/dineout/dineout_local_config.properties PLATFORM=web TAG=dineout ./gradlew run
  @web @android @searchRestaurants
  Scenario: Search for one way ticket from Pune to Delhi for single passenger
    Given I am in "Mumbai"
    When I search for "mexican" cuisine restaurants
    And I filter for restaurants with "Dineout Pay" offers
    Then I can reserve a table in the first restaurant displayed
