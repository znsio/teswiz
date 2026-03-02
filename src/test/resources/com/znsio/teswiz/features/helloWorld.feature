@helloworld @prod

Feature: HelloWorld tests
#  CONFIG=./configs/helloworld/helloworld_local_ios_config.properties TAG="@helloWorld" PLATFORM=iOS ./gradlew run
#  CONFIG=./configs/helloworld/helloworld_headspin_ios_config.properties CLOUD_KEY=$HEADSPIN_CLOUD_KEY TAG="@helloWorld and @headspin" PLATFORM=iOS ./gradlew run
#  RUN_IN_CI=true CONFIG=./configs/helloWorld/helloworld_browserstack_ios_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=iOS TAG="@helloworld and @browserstack" ./gradlew run
  @iOS @headspin @browserstack
  Scenario: As a guest user, I should be able to generate random numbers and see the thumbs up icon
    Given I make the number random "2" times
#    And I simulate additional differences
#    Then I can see the Thumbs Up icon
