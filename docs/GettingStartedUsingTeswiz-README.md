## Getting Started, or how can you use teswiz?
It is very easy to use teswiz in your framework.
Follow these steps:
1. Setup the prerequisites mentioned below [https://github.com/znsio/teswiz#prerequisites]
1. Using your favorite IDE (I use IntelliJ Idea Community Edition), create a new Java-Gradle project
1. Copy build.gradle.sample file to your newly created project's root folder and rename it to build.gradle
1. Create capabilities.json in some folder - ex: ./caps - refer to src/test/resources/com/znsio/e2e/features/android/caps/theapp_local_capabilities.json
1. Create config.properties in some folder - ex: ./configs and provide default values - refer to src/test/resources/com/znsio/e2e/features/android/configs/theapp_local_config.properties
1. Update the **run** task in build.gradle with appropriate values for config.properties, pathToStepDef, pathToFeaturesDir, pathToLogProperties
1. Refer to the **Running the tests** section

## Prerequisites

* JDK
    * **Minimum JDK version: 11**
    * **Set JAVA_HOME environment variable**
    * You can install JDK from here: https://adoptopenjdk.net/
* Setup the Android environment for test execution:
    * **Set ANDROID_HOME environment variable**
    * **Refer to this post for instructions how to automatically setup your environment - https://applitools.com/blog/automatic-appium-setup/**
    * Additional References:
        * Setup Android Command-line tools and SDK - https://developer.android.com/studio#command-tools
        * Install appium - https://appium.io
* Appium Desktop App is a great way to identify locators, and the recorder is quite helpful to quickly identify multiple
  locators for your tests - https://github.com/appium/appium-desktop/releases/tag/v1.20.2. You can also use Katalon
  Studio for locator identification (especially helpful for Windows platform)
* To verify appium installation is successful, run
  `appium-doctor` - it should not report any errors
* To install reportportal on local machine, refer to https://reportportal.io/installation. (Docker setup is the easiest way to proceed).

