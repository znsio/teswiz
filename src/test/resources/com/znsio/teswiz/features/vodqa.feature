@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using valid credentials
    When I scroll from one to another element point on vertical swiping screen
    Then Element text should be visible

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeLeft PLATFORM=android ./gradlew run
  @android @swipeLeft
  Scenario: validating swipe left functionality
    Given I login to vodqa application using valid credentials
    When I swipe left on "carousel" screen
    Then I see tile element "2" on the screen


  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeRight PLATFORM=android ./gradlew run
  @android @swipeRight
  Scenario: validating swipe right functionality
    Given I login to vodqa application using valid credentials
    When I swipe right on "carousel" screen
    Then I see tile element "3" on the screen

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeByPercentageAttributes PLATFORM=android ./gradlew run
  @android @swipeByPercentageAttributes
  Scenario: validating Swipe by percentage Attributes functionality
    Given I login to vodqa application using valid credentials
    When I swipe at percentage height 60 from percentage width 10 to percentage width 70 on "carousel" screen
    Then I see tile element "3" on the screen