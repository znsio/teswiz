@helloWorld @prod

Feature: HelloWorld tests
#  CONFIG=./configs/helloworld/helloworld_local_ios_config.properties TAG="@helloWorld" PLATFORM=iOS ./gradlew run
#  CONFIG=./configs/helloworld/helloworld_headspin_ios_config.properties CLOUD_KEY=$HEADSPIN_CLOUD_KEY TAG="@helloWorld and @headspin" PLATFORM=iOS ./gradlew run
  @iOS @headspin
  Scenario: As a guest user, I should be able to generate random numbers and see the thumbs up icon
    Given I make the number random "2" times
#    And I simulate additional differences
#    Then I can see the Thumbs Up icon
