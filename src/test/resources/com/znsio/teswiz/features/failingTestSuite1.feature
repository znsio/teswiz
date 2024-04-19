@api @negateResults
Feature: Failing test suite - feature 1

  @failing
  Scenario: Softly Fail 1
    Given I softly fail "1"

  @failing
  Scenario: Hard Fail 3
    Given I fail hard "3"

  @failing
  Scenario: Pass 4
    Given I pass "4"
    Given I now pass 4

  Scenario: Softly Fail 6
    Given I softly fail "6"

  @failing
  Scenario: Skip test 7
    Given I fail hard "7"
    And I pass "7"

  @failing
  Scenario: Pass 534
    Given I pass "534"

  Scenario: Pass 53
    Given I pass "53"


