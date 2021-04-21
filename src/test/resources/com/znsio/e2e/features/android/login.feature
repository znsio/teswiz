Feature: Test valid and invalid login

  @android
  Scenario: Verify error message on invalid login
    When I login with invalid credentials - "znsio1", "invalid password"
#        Then I see the error "Invalid login credentials, please try again"
#        When I dismiss the alert
#        And I login with invalid credentials - "znsio", "znsio"
#        Then I see the error "Invalid login credentials, please try again"

