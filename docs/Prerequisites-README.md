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
        * Install appium 2.0
          *  ```npm install```
* Appium Desktop App is a great way to identify locators, and the recorder is quite helpful to quickly identify multiple
  locators for your tests - https://github.com/appium/appium-desktop/releases/tag/v1.20.2. You can also use Katalon
  Studio for locator identification (especially helpful for Windows platform)
* To verify appium installation is successful, run
  `appium-doctor` - it should not report any errors
* To install reportportal on local machine, refer to https://reportportal.io/installation. (Docker setup is the easiest way to proceed).

