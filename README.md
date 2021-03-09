[![0.0.3](https://jitpack.io/v/znsio/unified-e2e.svg)](https://jitpack.io/#znsio/unified-e2e)
[![0.0.3](https://jitci.com/gh/znsio/unified-e2e/svg)](https://jitci.com/gh/znsio/unified-e2e)


# What is this repository about?

This repository implements automated tests for Android & iOS apps, specified using cucumber-jvm and
intelligently run them against

* Android
* iOS
* Windows Apps
* Web

Applitools is integrated with this framework, to provide Visual AI testing as part of functional automation.

Reports will be uploaded to reportportal.io, that you would need to setup separately, and provide the server details in
src/test/resources/reportportal.properties file or provide the path to the file using this environment variable: `REPORT_PORTAL_FILE`

## Tech stack used

* cucumber-jvm (https://cucumber.io)
* AppiumTestDistribution (https://github.com/AppiumTestDistribution/AppiumTestDistribution) -manages Android and iOS
  devices, and Appium
* Appium (https://appium.io)
* WebDriver (https://selenium.dev)
* reportportal.io (https://reportportal.io)
* Applitools (https://applitools.com)

## Prerequisites

* Install JDK and set JAVA_HOME environment variable
* Setup Android Command-line tools and SDK and set ANDROID_HOME environment variable
* Install appium - https://applitools.com/blog/automatic-appium-setup/
* Appium Desktop App is a great way to identify locators, and the recorder is quite helpful to quickly identify multiple
  locators for your tests - https://github.com/appium/appium-desktop/releases/tag/v1.20.2. You can also use Katalon Studio for locator identification (especially helpful for Windows platform)
* To verify appium installation is successful, run
  `appium-doctor` - it should not report any errors
* Install reportportal (Docker setup is the easiest way to proceed: https://reportportal.io/installation)
* Set `APPLITOOLS_API_KEY` as an environment variable

## Running the tests

### Run on Android

To run all the tests against the Android platform, run the following command:

    Platform=android ./gradlew clean cucumber

#### Run on Local devices:
The framework, by default, automatically figures out if there are multiple devices connected to the machine, and if so, will run the tests in
parallel

#### Run on Device Farm:
To enable running the tests on pCloudy's Mobilab, the following additional environment variables need to be provided:

* `RunOnCloud=true` - Default is `false`
* `CLOUD_USER` - Mobilab username
* `CLOUD_KEY` - Mobilab password

Sample command:

    Platform=android RunOnCloud=true CLOUD_USER=myusername CLOUD_KEY=abcd1234abcd ./gradlew clean cucumber

For other cloud configurations, refer here: https://github.com/AppiumTestDistribution/AppiumTestDistribution

### Run on iOS

    Platform=windows ./gradlew clean cucumber

### Run on Windows

    Platform=windows ./gradlew clean cucumber

### Run on Web

    Platform=web ./gradlew clean cucumber

### Running Real Meeting simulations
The framework now supports running multiuser scenarios.

You can run these tests as below:

#### To run tests on **android & web** platforms

    Tag=@multiuser-android-web
**_Current restriction - 1 android device & max 2 web browsers_**

#### To run tests on **web** platforms

    Tag=@multiuser-web-web 
**_Current restriction - max 2 web browsers_**

## Additional configurations

### Running the tests with Applitools Visual AI
To enable Applitools Visual Testing in your test execution, the following additional environment variables need to be provided:
* `Visual=true` - to enable Visual Testing using Applitools
* `APPLITOOLS_API_KEY=<API_KEY>` - Sets the API key as provided by Applitools

### Running a subset of tests:
To run a subset of tests, for a given platform, the following additional environment variables need to be provided:
* `Tag=@schedule` - This will run all tests tagged with the platform name provided, except tests tagged as "@wip"
* `Tag="@schedule and @signup"` - This will run all tests tagged with the name **schedule AND signup** for the platform name provided, except tests tagged as "@wip"
* `Tag="@schedule or @signup"` - This will run all tests tagged with the name **schedule OR signup** for the platform name provided, except tests tagged as "@wip"

Sample commands:

    Platform=android Tag=@schedule ./gradlew clean cucumber`

    Platform=android Tag="@schedule and @signup" ./gradlew clean cucumber`

    Platform=android Tag="@schedule or @signup" ./gradlew clean cucumber`

### Using a different apk for execution (Android):
To run tests using a specific apk (instead of the one specified in caps/capabilities.json, OR, caps/mobilab_capabilties.json, the following additional environment variable need to be provided:
* `AppPath='<path to apk>'`

Sample command:

    AppPath=~/Downloads/MyLatestApp.apk Platform=android ./gradlew clean cucumber

## Troubleshooting / FAQs

### Setting Environment Variables:

You can set environment variables

From Mac OSX or Linux:

    export Platform=android

From Windows:

    set Platform=android
