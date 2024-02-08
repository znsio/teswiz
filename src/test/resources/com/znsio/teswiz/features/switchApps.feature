Feature: Switch applications in same device/driver context

#  CONFIG=./configs/calculator/calculator_local_config.properties PLATFORM=android TAG=switchAppsOnDevice ./gradlew run
  @android @switchAppsOnDevice
  Scenario: Switch between Calculator, YouTube and back to Calculator
    Given I start the calculator
    And I select "2"
    When I switch to theapp
    And I login to the switched TheApp with invalid credentials - "znsio1", "invalid password"
    Then I switch back to the calculator
    And I select "5"
    And I force stop theapp
