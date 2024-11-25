@validatePDF
Feature: PDF validation using Applitools

  # CONFIG=./configs/pdf/pdf_local_web_config.properties PLATFORM=web TAG=@validatePDF ./gradlew run
  @web
  Scenario: Validate a pdf document as part of a web scenario
    Given "I" launch the app
    Then I validate all pages of the pdf document "src/test/resources/pdf/Teswiz.pdf"
    And I validate page numbers "1,2,4,7" of the pdf document "src/test/resources/pdf/Teswiz.pdf"

  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG=@standalone ./gradlew run
  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG="@standalone and @allpages" ./gradlew run
  @pdf @standalone @allpages
  Scenario: Validate a standalone pdf document
    Given I validate the standalone pdf document "src/test/resources/pdf/Teswiz.pdf"

  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG=@standalone ./gradlew run
  # CONFIG=./configs/pdf/local_pdf_config.properties PLATFORM=pdf TAG="@standalone and @specificpages" ./gradlew run
  @pdf @standalone @specificpages
  Scenario: Validate specific pages of the standalone pdf document
    Given I validate page numbers "1,2,4,7" of standalone pdf document "src/test/resources/pdf/Teswiz.pdf"
