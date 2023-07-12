@cryptoAPI @api
Feature: Crypto-Currency price data for last 24 hrs
# CONFIG=./configs/api_local_config.properties TAG=cryptoAPI PLATFORM=api ./gradlew run

  Scenario Outline: Validate price change in last 24 hrs for given crypto currencies
    Given I send GET request for crypto <symbol>
    Then price change should be less than <maxPriceChange>
    Examples:
    | symbol    | maxPriceChange |
    | "LTCUSDT" |       75       |
    | "ETHUSDT" |       60       |
    | "BNBUSDT" |       55       |
    | "XRPUSDT" |       80       |

  Scenario: Validate price change percentage for BTC
    Given I send GET request for crypto "BTCUSDT"
    Then price change percentage should be less than 20
