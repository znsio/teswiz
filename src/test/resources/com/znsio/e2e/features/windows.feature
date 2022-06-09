@notepad @windows
Feature: Test scenarios for the Notepad application on Windows

  Scenario: I should be able to type text in Notepad
    Given I have launched Notepad application
    Then I should be able to type "Hi, The Windows test is working"