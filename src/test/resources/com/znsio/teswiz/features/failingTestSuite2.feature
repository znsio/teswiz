@api @negateResults
Feature: Failing test suite - feature 1

  @failing
  Scenario: Softly Fail 9
    Given I softly fail "9"

  @failing
  Scenario: Hard Fail 33
    Given I fail hard "33"

  @failing
  Scenario: Pass 45
    Given I pass "45"
    Given I now pass 45

  Scenario: Pass 66
    Given I pass "66"

  @skipStep @wip
  Scenario: Skip test 98
    Given I fail hard "98"
    And I pass "98"

  @skipStep @wip @failing
  Scenario: Skip test 79
    Given I fail hard "79"
    And I pass "79"

