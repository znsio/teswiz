Feature: Check heartbeat thread management

#  CONFIG=./configs/theapp/theapp_local_web_config.properties PLATFORM=web TAG="@heartbeat" ./gradlew run
  @web @heartbeat
  Scenario: Reports should get created correctly when heartbeat threads are created
    Given "I" login to TheApp with invalid credentials - "znsio1", "invalid password"
    When I start a heartbeat for "guest-1"
    And I start a heartbeat for "guest-2"
    And I wait for "20" seconds
    Then I can see the started heartbeat for "guest-1"
    And I can see the started heartbeat for "guest-2"
