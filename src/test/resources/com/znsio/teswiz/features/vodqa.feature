@vodqa
Feature: Vodqa test

#  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
  @android
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using valid credentials
    When I scroll from one to another element point on vertical swiping screen
    Then Element text should be visible

  @android
  Scenario: User tap in the middle of the screen
    Given I login to vodqa application using valid credentials
    When I tap in the middle of the screen
    Then I am able to move from "Samples List" page to next page

  #  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=scrollDownByScreenSize ./gradlew run
  @android @scrollDownByScreenSize
  Scenario: Validate that user is able to scroll down by screen size
    Given I login to vodqa application using valid credentials
    When I scroll down by screen size on vertical swiping screen
    Then Element text should be visible

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeLeft PLATFORM=android ./gradlew run
  @android @swipeLeft
  Scenario: validating swipe left functionality
    Given I login to vodqa application using valid credentials
    When I swipe left on "carousel" screen
    Then I am able to see element with text "2" on the screen

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeRight PLATFORM=android ./gradlew run
  @android @swipeRight
  Scenario: validating swipe right functionality
    Given I login to vodqa application using valid credentials
    When I swipe right on "carousel" screen
    Then I am able to see element with text "3" on the screen

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@swipeByPercentageAttributes PLATFORM=android ./gradlew run
  @android @swipeByPercentageAttributes
  Scenario: validating Swipe by percentage Attributes functionality
    Given I login to vodqa application using valid credentials
    When I swipe at 60 percent height from 10 percent width to 70 percent width on "carousel" screen
    Then I am able to see element with text "3" on the screen

  #  CONFIG=./configs/vodqa_local_config.properties PLATFORM=android TAG=vodqaContextSwitch ./gradlew run
  @vodqaContextSwitch @android
  Scenario: Validate context switching between native and web view context
    Given I login to vodqa application using valid credentials
    Then I am able to view hacker news login button inside web view section
    And I am able to view section header by navigating inside native view section

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@appInBackground PLATFORM=android ./gradlew run
  @android @appInBackground
  Scenario: Put app in background
    Given I login to vodqa application using valid credentials
    Then App should work in background for 5 sec

  #  CONFIG=./configs/vodqa_local_config.properties TAG=@scrollVertically PLATFORM=android ./gradlew run
  @android @scrollVertically
  Scenario: Validate that user is able to scroll vertically by screen percentage
    Given I login to vodqa application using valid credentials
    When I scroll vertically from 20 percent height to 60 percent height and 50 percent width
    Then Element text should be visible