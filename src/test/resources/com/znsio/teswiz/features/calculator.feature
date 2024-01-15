@browserstack
Feature: Calculator test

#  CONFIG=./configs/calculator/calculator_local_config.properties PLATFORM=android TAG=calculator ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/calculator/calculator_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=android TAG="@calculator and @browserstack" ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/calculator/calculator_pcloudy_config.properties CLOUD_USERNAME=$PCLOUDY_CLOUD_USERNAME CLOUD_KEY=$PCLOUDY_CLOUD_KEY PLATFORM=android TAG="@calculator and @mobilab" ./gradlew run
  @android @calculator @mobilab
  Scenario: Addition in Calculator
    Given I start the calculator
    When I select "2"
    And I press "plus"
    When I select "5"
    And I press "equals"
    Then I should see "7"


#  CONFIG=./configs/calculator/new_calculator_local_config.properties PLATFORM=android TAG=new_calculator ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/calculator/new_calculator_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=android TAG="@new_calculator and @browserstack" ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/calculator/new_calculator_pcloudy_config.properties CLOUD_USERNAME=$PCLOUDY_CLOUD_USERNAME CLOUD_KEY=$PCLOUDY_CLOUD_KEY PLATFORM=android TAG="@new_calculator and @mobilab1" ./gradlew run
  @android @new_calculator @mobilab1
  Scenario: Addition in New Calculator
    Given I start the new calculator
    When I select "2" in the new calculator
    And I press "plus" in the new calculator
    When I select "5" in the new calculator
    And I press "equals" in the new calculator
    Then I should see "7"