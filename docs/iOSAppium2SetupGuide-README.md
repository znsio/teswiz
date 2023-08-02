# Appium 2 – iOS Setup

<span style="font-size: larger;">This guide will walk you through the steps to set up Appium 2 for iOS.</span> 

1. Remove previous Appium 1.x installation if any.
## Install Appium 2

2. iOS support requires several dependencies to be installed before it can be used. You can easily install these dependencies using the package.json file provided.

* Before you begin, please ensure that you have Node.js and npm (Node Package Manager) installed on your system. You can download and install them from the official Node.js website: <br>
https://nodejs.org

* Navigate to the project's root directory
* Install the project dependencies using npm. Below This command will read the package.json file and install all the required dependencies listed in it.
<br>
`npm install`

* Wait for the installation process to complete. npm will download and install the required packages into a folder named node_modules in your project's directory.

* Once the installation is finished, Refer Step 

**Note: If you have used the npm install command above, you can skip the following individual installation steps.**
3. Install Appium 2 using the following command:
- ```npm intall appium```



## Install Plugins





4. To install the Appium Device Farm and Appium Dashboard plugins, run the following commands.

* `appium plugin install --source=npm appium-device-farm`

* `appium plugin install --source=npm appium-dashboard`


5. Install plugin for Relaxing Appium's requirement for vendor prefixes on capabilities.

* `appium plugin install relaxed-caps`

   `appium plugin install --source=npm @appium/relaxed-caps-plugin`



## Install Drivers



6. Cross Platform installation, [click here](https://github.com/danielpaulus/go-ios), this is an operating system independent implementation of iOS device features. You can run UI tests, launch or kill apps, install apps etc. with it.
   MacOS users will need to install this driver to unzip files. To install the driver, run the following command
* `npm install go-ios`


7. The XCUITest driver is used to automate iOS apps that are written using Xcode. To install the driver, run the following command:

* `appium driver install xcuitest`

8. Verify XCUITest Driver installation path.
   ![XCUITest-driver-path.png](XCUITest-driver-path.png)

## Setting Variables for Project Setup
9. Add **APPIUM_JS_PATH** configuration in config.properties file, you can follow the format commonly used in properties files:
`APPIUM_JS_PATH=./node_modules/appium/lib/main.js`

10. Set the **usePrebuiltWDA** capability to false in a capabilities file, you can use the following format:
<br>`"usePrebuiltWDA": false
    `

## Install and configure Appium Inspector



11. Latest [Appium Inspector](https://github.com/appium/appium-inspector/releases) supporting Appium 2

12. Appium Inspector [Capability guidelines and examples](https://appium.io/docs/en/2.0/guides/caps/)



## Start Appium



13. To start Appium, run the following command:

* `appium --relaxed-security --allow-cors --use-plugins="appium-dashboard,relaxed-caps,device-farm" -pa /wd/hub --plugin-device-farm-platform=both`

Please keep in mind that Teswiz will start the Appium 2 server and handle the necessary configurations.

![Launch-Appium-with-installed-plugins.png](Launch-Appium-with-installed-plugins.png)

## Connect your device or simulator



14. Once Appium is started, you can connect your iOS device or simulator.



15. Run following command to get Simulator/Device details:

* `xcrun simctl list | grep Booted`

![Booted-device-details.png](Booted-device-details.png)









## Connect Appium Inspector



16. You can connect Appium Inspector to your Appium session by opening the Appium Inspector app and entering the URL of your Appium server.

![Sample-Desired-Capabilites.png](Sample-Desired-Capabilites.png)


![Appium-Inspector-connected.png](Appium-Inspector-connected.png)






You are now ready to start automating your iOS apps!

For more information on Appium 2, please see the [Appium documentation.](https://appium.io/docs/en/2.0/) 

 