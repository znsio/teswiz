# Writing the first test

## Pre-requisites:

        1. Intellij (install cucumber JVM plugin)
        2. Java JDK 17 (JAVA_HOME Path Set)
        3. Appium version (minimum 1.20.0)
        4. Android studio (ANDROID_HOME Path Set)

## Flow understanding

    # Feature Files:
            Tests are written in the declarative style, depicting the business functionality. There should be no
            "UI actions" in the scenarios. 
            Depending on the platform your scenario is implemented for, you would need to add
            @android / @iOS / @web @windows tags. You can add custom tags on the scenarios to help run tests 
            independently, and also group them appropriately in the reports.
    
    # Steps File:
            These are the classes where the steps are defined for the features files. These classes will call the methods
            from BL to perform the particular step. The driver for the particular context is also created in BL. Context is
            a user persona for which tests are written. Loggers can be added for every step method.
    
    # Business Layer:
            These classes are the logic layer of the suite where the business logic is returned to perfrom certain operation.
            BL contains the call for screens and services required to perform the operation. Also, assertions can be added for
            screens.

    # Screens:
            Screens are the classes that represents one particular screen or snippet of application. These are abstract 
            classes which is extended according to the platform in which tests going to execute in a sub-directory.
            Screen classs also have visual checks to implement visual testing with navigation and the locators on the page.

    # Services: 
            These are the files that contain the API service calls required for pre-requisite test data. 
    
    # Caps: 
            This directory contains the capabilities file contains details required to create a Appium 
            driver for android, iOS or windows device. It also could have details about the cloud-based
            device farm.

    # Configs: 
            The config.properties file has configuration details related to the execution of the tests such as 
            browser, parallel instances, enabling visual testing, running in CI etc. 

## Steps to writing and configure the first test case using teswiz:

![Steps for Writing The First Test Case](StepsToWriteFirstTestInTeswiz.png)

1. Changes to be done in config files, Add my_web_config.properties file in ./configs directory as
   per the project or assignment requirement.\
   Refer to an existing config and capabilities file and update the value of platform.\
   ````
   PLATFORM=android/web
   ````
   Update the properties of the particular web that you want to open in the\
    ````
    ENVIRONMENT_CONFIG_FILE=./src/test/resources/environments.json
    BASE_URL_FOR_WEB=BASE_URL_WEB 
2. Changes in the environments.json \
   Update the BASE_URL_WEB in this file to launch the base url for the application.\
   Copy the content of already existing local_configs file and update the value of platform and
   BASE_URL_OF_WEB to web and your url key from environments.json file.
3. Updates in the build.gradle \
   Check the below path of the config.properties for local run.\
   Check the path for the properties file for the cloud provider configuration
   (./configs/pcloudy_config.properties) if running from local to the cloud devices
   when passing the environment variable RUN_IN_CI = true.
   ![Configuration File Setup](ConfigFileConfiguration.png)
4. Add required test data in ./src/test/resources/testData.json such as user details.
5. Create a feature file in ./src/test/resources/com/znsio/sample/e2e/features directory.
   Ex: myFirstTest.feature
6. Add feature and scenario in it.\
   Note: Scenario should be in declarative\
   For example:\
   ````
    Scenario: User should be able to change the mic settings
            Given I sign in as a registered "Host"
            And I start an instant meeting
            When I Unmute myself
            Then I should be able to Mute myself
7. Add a test step for the same in ./src/test/java/com/znsio/sample/e2e/steps.
8. Add the required BL methods in ./src/test/java/com/znsio/sample/e2e/businessLayer.
9. Add the required Screens for BL in ./src/test/java/com/znsio/sample/e2e/screen.
10. Terminal run commands \
    (i). Running in local from terminal
   ````    
     PLATFORM=android TAG=@firstTest ./gradlew clean run 
   ````

(ii). Running in local to the cloud devices (Mobile)

   ````
     PLATFORM=android TAG=@firstTest RUN_IN_CI = true ./gradlew clean run 
   ````
    
    
