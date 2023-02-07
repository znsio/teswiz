@ajio @prod @guestUser
Feature: Ajio tests
#  CONFIG=./configs/ajio_local_config.properties TAG="@ajio and @imagesearch" PLATFORM=android ./gradlew run
  @android @single-app @imagesearch
  Scenario: As a guest user, I should be able to look for a product using image search and  prepare a cart
	Given  I search for products using "image"
	When I add the product to the cart
	Then I should see the product in the cart