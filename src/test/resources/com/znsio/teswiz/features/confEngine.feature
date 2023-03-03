@confengine @dev
Feature: Create a personal schedule

#  CONFIG=./configs/confengine_local_config.properties PLATFORM=android TAG=confengine ./gradlew run
  @android
  Scenario: See the schedule of an upcoming conference
    Given I see the list of conferences
