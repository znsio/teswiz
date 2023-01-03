# Writing the first tests

## Pre-requisites:

        1. Have admin rights  
        2. Intellij (install cucumber JVM plugin)
        3. Java JDK 11  
        4. Gradle 7.1
        5. Appium
        6. Android studio with all paths set
        7. GIT
        8. Docker 
        9. Create a fork and Clone the repository from - https://github.com/znsio/teswiz.

## Flow understanding

    # Feature Files:    
            These are the files that contains the tests in BDD format with the tags related to 
            platform on which the tests will be running. Tags such as @android or @web etc. 
            Also, there can be more tags related to features can be added in feature level.
    
    # Steps File:
            These are files that have the definition of the steps that is used in feature files. 
            LOGGER can be added in each method. From steps file, there would be a call made to BL.
    
    # Business Layer:
            These files have the logic to verify the assertions or business logics to verify. 
            From the BL, there will be call made to screens or the APIs. This will be the logical 
            layer for the suite. For each screen there would be a separate BL. 
    
    # Screens: 
            These are the files there will be the screen navigation and locators for each page will
            be here. The methods shouldn’t have void return type. 

    # Services: 
            These are the files that contain the API service calls required for pre-requisite data.
    
    # Caps: 
            This directory contains the capabilities required for android devices such as app 
            activity and app package.

    # Configs: 
            This directory has the information about the configuration related to browser, parallel
            tests, enabling visual testing, running in CI etc. 

## Steps to Writing and Configure the first test case using teswiz:

![Steps for Writing The First Test Case](StepsToWriteFirstTestInTeswiz.png)

1. Create a feature file in ./src/test/resources/com/znsio/sample/e2e/features directory.
   Ex: myFirstTest.feature
2. Add Feature and scenario in it.\
   Note: Scenario should be in declarative\
   For example:\
   ````
   Feature: Writing My First Test Using Teswiz
   Scenario:
      Given I am login with valid credential
      And I can see the user dashboard
      Then I can see the user details as per login user 
3. Add A test step for the same in ./src/test/java/com/znsio/sample/e2e/steps
4. Add the required BL methods in ./src/test/java/com/znsio/sample/e2e/businessLayer
5. Add the required Screens for BL in ./src/test/java/com/znsio/sample/e2e/screen
6. Changes to be done in config files, Add my_web_config.properties file in ./configs directory as
   per the project or assignment requirement.\
   Copy the content of already existing local_configs file and update the value of platform.\
   ````
   PLATFORM=android/web
   ````
   Update the properties of the particular web that you want to open in the\
    ````
    ENVIRONMENT_CONFIG_FILE=./src/test/resources/environments.json
    BASE_URL_FOR_WEB=BASE_URL_WEB 
7. Changes in the environments.json \
   Update the BASE_URL_WEB in this file to launch the base url for the application.\
   Copy the content of already existing local_configs file and update the value of platform and
   BASE_URL_OF_WEB to web and your url key from environments.json file.
8. Updates in the build.gradle \
   Check the below path of the config.properties for local run.\
   Check the path for the properties file for the cloud provider configuration
   (./configs/pcloudy_config.properties) if running from local to the cloud devices
   when passing the environment variable RUN_IN_CI = true.

![Configuration File Setup](ConfigFileConfiguration.png)

9. Terminal Run commands \
   (i). Running in local from terminal 
   ````    
     PLATFORM=android TAG=@firstTest ./gradlew clean run 
   ````
   (ii). Running in local to the cloud devices (Mobile) 
    ````
     PLATFORM=android TAG=@firstTest RUN_IN_CI = true ./gradlew clean run 
   ````
    
    
            