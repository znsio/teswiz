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
* Remove previous Appium 1.x installation if any.
* Before you begin, please ensure that you have Node.js and npm (Node Package Manager) installed on your system. You can download and install them from the official Node.js website: <br>
  https://nodejs.org

### Install Appium 2
  * You can install Appium 2.x and all relevant drivers, plugins and utilities using the [package.json](../package.json)

        npm install
  * Wait for the installation process to complete. npm will download and install the required packages into a folder named node_modules in your project's directory.

## Verifying the installation:

Once the installation is finished, check the output of `npm list`

You should see the following:

### Plugins
    appium-device-farm
    appium-dashboard
    @appium/relaxed-caps-plugin --> plugin for relaxing appium's requirement for vendor prefixes on capabilities

### Utilities
    go-ios --> A [cross platform installation](https://github.com/danielpaulus/go-ios), this is an operating system independent implementation of iOS device features. You can run UI tests, launch or kill apps, install apps etc. with it. MacOS users will need to install this driver to unzip files.
    @appium/doctor --> A utility to check the status of your appium installation

### Drivers
    appium-xcuitest-driver --> iOS driver
    appium-uiautomator2-driver --> Android driver

### To verify appium installation is successful, run

    appium-doctor

There should be no errors reported

## Setting Variables for Project Setup
Add appiumServerPath to the capabilities.json file:

    "appiumServerPath": "./node_modules/appium/build/lib/main.js"

## Install and configure Appium Inspector

Latest [Appium Inspector](https://github.com/appium/appium-inspector/releases) supporting Appium 2

Appium Inspector [Capability guidelines and examples](https://appium.io/docs/en/2.0/guides/caps/)

