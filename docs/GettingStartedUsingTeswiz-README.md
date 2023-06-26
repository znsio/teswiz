## Getting Started, or how can you use teswiz?
It is very easy to use teswiz in your framework.
Follow these steps:
1. Setup the prerequisites mentioned below [https://github.com/znsio/teswiz#prerequisites]
1. Using your favorite IDE (I use IntelliJ Idea Community Edition), create a new Java-Gradle project
1. Copy build.gradle.sample file to your newly created project's root folder and rename it to build.gradle
1. For `android app` automation
   * Get APP_PACKAGE_NAME - example: `aapt dump badging temp/sampleApps/theapp.apk | grep package`
   * Get APP_ACTIVITY - example: `aapt dump badging temp/ajio-8-3-4.apk | grep activity`
1. For `web` automation
   * Add `<>_BASE_URL` in environments.json - example: `THEAPP_BASE_URL=http://the-internet.herokuapp.com`
   * Update `BASE_URL` with the above in config.properties - example: `BASE_URL=THEAPP_BASE_URL`
1. Create config.properties in some folder - ex: `./configs` and provide default values - refer to src/test/resources/com/znsio/e2e/features/android/configs/theapp_local_config.properties
1. Create capabilities.json in some folder - ex: `./caps` - refer to src/test/resources/com/znsio/e2e/features/android/caps/theapp_local_capabilities.json
1. Update `reportportal.properties` file
1. **Implement the test**
   1. Define your scenario in a feature file (`src/test/resources/<package_name>/<feature_dir>`)
   2. Create your step definitions (`src/test/java/<package_name>/steps`)
   3. Implement your business layer classes/methods (`src/test/java/<package_name>/businessLayer`)
   3. Implement your screen classes/methods (`src/test/java/<package_name>/screen`)
   4. [Setup Applitools Visual AI Testing](RunningVisualTests-README.md) 
2. Update the **run** task in build.gradle with appropriate values for config.properties, pathToStepDef, pathToFeaturesDir, pathToLogProperties
1. Refer to the **[Running the tests](SampleTests-README.md)** section

## Pre Requisites
* JDK
    * **Minimum JDK version: 11**
    * **Set JAVA_HOME environment variable**
    * You can install JDK from here: https://adoptopenjdk.net/
* Setup the Android environment for test execution:
    * **Set ANDROID_HOME environment variable**
    * **Refer to this post for instructions how to automatically setup your environment - https://applitools.com/blog/automatic-appium-setup/**
    * Additional References:
        * Setup Android Command-line tools and SDK - https://developer.android.com/studio#command-tools
        * Install appium 2.0 - http://appium.io/docs/en/2.0/quickstart/install/
        * Install appium-device-farm and appium-dashboard:
            * appium plugin install --source=npm appium-device-farm
            * appium plugin install --source=npm appium-dashboard
            * For additional information refer - https://github.com/AppiumTestDistribution/appium-device-farm
* Appium Desktop App is a great way to identify locators, and the recorder is quite helpful to quickly identify multiple
  locators for your tests - https://github.com/appium/appium-desktop/releases/tag/v1.20.2. You can also use Katalon
  Studio for locator identification (especially helpful for Windows platform)
* To verify appium installation is successful, run
  `appium-doctor` - it should not report any errors
* To install reportportal on local machine, refer to https://reportportal.io/installation. (Docker setup is the easiest way to proceed).

