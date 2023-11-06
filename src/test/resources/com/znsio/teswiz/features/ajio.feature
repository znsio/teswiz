@ajio @prod @guestUser
Feature: Ajio tests
#  CONFIG=./configs/ajio/ajio_local_android_config.properties TAG="@ajio and @imagesearch" PLATFORM=android ./gradlew run
#  CONFIG=./configs/ajio/ajio_local_ios_config.properties TAG="@ajio and @imagesearch" PLATFORM=iOS ./gradlew run
  @android @single-app @imagesearch @iOS
  Scenario: As a guest user, I should be able to look for a product using image search and  prepare a cart
    Given  I search for products using "image"
    When I add the product to the cart
    Then I should see the product in the cart

# CONFIG=./configs/ajio/ajio_local_android_config.properties TAG=@flick PLATFORM=android ./gradlew run
  @android @flick
  Scenario: As a guest user, I should be able to flick and see images in product details
    Given I open "Jackets" from "Topwear" section for "Men"
    When I select the first result
    Then I should be able to perform flick and view images

# CONFIG=./configs/ajio/ajio_local_ios_config.properties TAG="@multiuser-iOS and @ajio" PLATFORM=iOS ./gradlew run
# CONFIG=./configs/ajio/ajio_ios_browserstack_config.properties TAG="@multiuser-iOS and @ajio" CLOUD_KEY=<cloud_key> CLOUD_USERNAME=<cloud_username> PLATFORM=iOS RUN_IN_CI=true PLATFORM=iOS ./gradlew run
  @searchItem @multiuser-iOS @ajio
  Scenario: As a guest user, I should be able to add an item to cart
    Given "I" search "beauty" item on "iOS"
    And "You" search "jeans" item on "iOS"
    When "I" select first item
    And "You" select first item
    Then "I" add item to cart
    Then "You" add item to cart

# CONFIG=./configs/ajio/ajio_local_ios_config.properties TAG="@single-app and @ajio" PLATFORM=iOS ./gradlew run
  @searchItem @iOS @single-app @ajio
  Scenario: As a guest user, I should be able to add an item to cart
    Given "I" search "beauty" item on "iOS"
    When "I" select first item
    Then "I" add item to cart
