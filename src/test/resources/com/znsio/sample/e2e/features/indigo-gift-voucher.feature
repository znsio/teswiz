@indigoVoucher
Feature: Buy Indigo gift vouchers

  @voucher @web
  Scenario: To personalize and preview an Indigo gift voucher with invalid Promo code
    Given I personalise "1" Gift voucher of "3000"
    When I provide a invalid promocode
    Then I can purchase the gift voucher at origional price

