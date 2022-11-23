@indigo
Feature: Search for flight options

#  CONFIG=./configs/indigo_local_config.properties PLATFORM=web TAG=searchFlights ./gradlew run
  @web @searchFlights
  Scenario: Search for one way ticket from Pune to Delhi for single passenger
    Given I search for a "one-way" ticket from "Pune" to "Delhi" for "1" adult passenger

#  CONFIG=./configs/indigo_local_config.properties PLATFORM=web TAG=giftVoucher ./gradlew run
  @web @giftVoucher
  Scenario Outline: Customise and preview an Indigo gift voucher with invalid Promo code
    Given I want to personalize "1" gift voucher of INR "10000" for "<receipient>" with message "<personalisedMessage>"
    When I provide the receiver and sender details and "deny" the terms & conditions
    Then I cannot purchase the gift voucher

    Examples:
    | receipient | personalisedMessage |
    | my friend | Safe travels  |
