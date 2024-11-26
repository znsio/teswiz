@validatePDF
Feature: PDF validation using Applitools

  #  RUN_IN_CI=true CONFIG=./configs/theapp/pdf_web_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=web TAG="@validatePDF and @browserstack" ./gradlew run
  #  RUN_IN_CI=true CONFIG=./configs/theapp/pdf_android_browserstack_config.properties CLOUD_USERNAME=$BROWSERSTACK_CLOUD_USERNAME CLOUD_KEY=$BROWSERSTACK_CLOUD_KEY PLATFORM=web TAG="@validatePDF and @browserstack" ./gradlew run
  # CONFIG=./configs/pdf/pdf_local_web_config.properties PLATFORM=web TAG=@validatePDF ./gradlew run
  # CONFIG=./configs/pdf/pdf_local_android_config.properties PLATFORM=android TAG=@validatePDF ./gradlew run
  @web @android @browserstack
  Scenario: Validate a pdf document as part of a web scenario
    Given "I" go to Login
    Then I validate all pages of the pdf document "src/test/resources/pdf/Teswiz.pdf"
    And I validate page numbers "1,3" of the pdf document "src/test/resources/pdf/Teswiz.pdf"

  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG=@standalone ./gradlew run
  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG="@standalone and @allpages" ./gradlew run
  @pdf @standalone @allpages
  Scenario: Validate a standalone pdf document
    Given I validate the standalone pdf document "src/test/resources/pdf/Teswiz.pdf"

  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG=@standalone ./gradlew run
  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG="@standalone and @specificpages" ./gradlew run
  @pdf @standalone @specificpages
  Scenario: Validate specific pages of the standalone pdf document
    Given I validate page numbers "1,3" of standalone pdf document "src/test/resources/pdf/Teswiz.pdf"
