@vodqa
#  CONFIG=./configs/vodqa/vodqa_local_config.properties PLATFORM=android TAG=vodqa ./gradlew run
Feature: Vodqa test

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties PLATFORM=android TAG=scrollUsing2Points ./gradlew run
  #  RUN_IN_CI=true CONFIG=./configs/vodqa/vodqa_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=android TAG="@scrollUsing2Points and @browserstack" ./gradlew run
  @android @scrollUsing2Points
  Scenario: Validating scroll functionality using 2 points
    Given I login to vodqa application using valid credentials
    When I scroll from one to another element point on vertical swiping screen
    Then Element text "Jasmine" should be visible

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties PLATFORM=android TAG=tapInMiddleOfScreen ./gradlew run
  @android @tapInMiddleOfScreen
  Scenario: User tap in the middle of the screen
    Given I login to vodqa application using valid credentials
    When I tap in the middle of the screen
    Then I am able to move from "Samples List" page to next page

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties PLATFORM=android TAG=scrollDownByScreenSize ./gradlew run
  @android @scrollDownByScreenSize
  Scenario: Validate that user is able to scroll down by screen size
    Given I login to vodqa application using valid credentials
    When I scroll down by screen size on vertical swiping screen
    Then Element text "Jasmine" should be visible

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@swipeByPercentageAttributes PLATFORM=android ./gradlew run
  @android @swipeByPercentageAttributes
  Scenario: validating Swipe by percentage Attributes functionality
    Given I login to vodqa application using valid credentials
    When I swipe at 60 percent height from 10 percent width to 70 percent width on "carousel" screen
    Then I am able to see element with text "3" on the screen

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties PLATFORM=android TAG=vodqaContextSwitch ./gradlew run
  @vodqaContextSwitch @android
  Scenario: Validate context switching between native and web view context
    Given I login to vodqa application using valid credentials
    Then I am able to view hacker news login button inside web view section
    And I am able to view section header by navigating inside native view section

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@appInBackground PLATFORM=android ./gradlew run
  @android @appInBackground
  Scenario: Put app in background
    Given I login to vodqa application using valid credentials
    Then App should work in background for 5 sec

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@scrollVertically PLATFORM=android ./gradlew run
  @android @scrollVertically
  Scenario: Validate that user is able to scroll vertically by screen percentage
    Given I login to vodqa application using valid credentials
    When I scroll vertically from 60 percent height to 20 percent height and 50 percent width
    Then Element text "Jasmine" should be visible


  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@longPress PLATFORM=android ./gradlew run
  @android @longPress
  Scenario: Validate that user is able to long press on an element
    Given I login to vodqa application using valid credentials
    When I long press on element
    Then long pressed popup should be visible

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@dragAndDrop PLATFORM=android ./gradlew run
  @android @dragAndDrop
  Scenario: Validate that user is able to drag and drop
    Given I login to vodqa application using valid credentials
    When I drag the circle object to the drop target
    Then I am able to view "Circle dropped" message

    #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@doubleTap PLATFORM=android ./gradlew run
  @android @doubleTap
  Scenario: Validate that user is able to double tap on an element
    Given I login to vodqa application using valid credentials
    Then I should be able to double tap on an element

  #  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@pinchAndZoom PLATFORM=android ./gradlew run
  @android @pinchAndZoom
  Scenario: Validate that user is able to pinch and zoom on particular element
    Given I login to vodqa application using valid credentials
    Then I should be able to pinch and zoom in on an element
    And I should be able to pinch and zoom out on an element

#  CONFIG=./configs/vodqa/vodqa_local_config.properties TAG=@multiTouch PLATFORM=android ./gradlew run
  @android @multiTouch
  Scenario: Validate that user is able to multi touch on an element
    Given I login to vodqa application using valid credentials
    Then I should be able set both sliders to value 50 by multi touch action
