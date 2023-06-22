[![0.0.3](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![0.0.3](https://jitci.com/gh/znsio/teswiz/svg)](https://jitci.com/gh/znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)

# ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Breaking changes in Latest teswiz v0.0.80![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png)

Below is the list of the breaking changes, and the corresponding new implementation starting from teswiz latest teswiz.

## Method name and implementation changes

There are some method name and implementation changes as listed below:

| Purpose                                                                                            | ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Old ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) | ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) New ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) |
|:---------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| To put App in Background for number of Seconds                                                     | putAppInBackground(int time)                                                                                          | putAppInBackgroundFor(int numberOfSeconds)                                                                            |
| Method Selects Device Notification from Notification Drawer                                        | selectNotification()	                                                                                                 | selectNotificationFromNotificationDrawer()                                                                            |
| Scroll In Dynamic Layer method is using Direction Enum instead of a String Parameter               | scrollInDynamicLayer(String direction)                                                                                | scrollInDynamicLayer(Direction direction)                                                                             |

## New Additions

There is a new method added:

| Purpose                                                                                            | ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) New ![#c5f015](https://placehold.co/15x15/c5f015/c5f015.png) |
|:---------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| A new method is added for swipe by passing the screen height and width in percentage as parameters | swipeByPassingPercentageAttributes(int percentScreenHeight, int fromPercentScreenWidth, int toPercentScreenWidth)     |

## Updated Usage Of Appium Driver in Methods
1. setWebViewContext()
2. setNativeAppContext()
3. scroll(Point fromPoint, Point toPoint) , scrollVertically() , scrollDownByScreenSize()
4. tapOnMiddleOfScreenOnDevice()
5. swipeLeft() , swipeRight() , swipe(int height, int fromWidth, int toWidth)

## References:
1. For appium2.0 : https://javadoc.io/doc/io.appium/java-client/8.0.0-beta/deprecated-list.html
2. For selenium 4: https://www.selenium.dev/selenium/docs/api/java/deprecated-list.html

## Logging to ReportPortal

To make it easy to log to ReportPortal, the following new methods have been added:

```
        ReportPortalLogger.logDebugMessage("debugMessage");
        ReportPortalLogger.logInfoMessage("infoMessage");
        ReportPortalLogger.logWarningMessage("warningMessage");
        ReportPortalLogger.attachFileInReportPortal("message", new File("fileName"));
```

[//]: # (```mermaid)

[//]: # (flowchart TD)

[//]: # (  id1[allDrivers.createDriverFor&#40;...&#41;]--has changed to---id2&#40;[Drivers.createDriverFor&#40;...&#41;]&#41;)

[//]: # (  style id1 fill:#f9f)

[//]: # (  style id2 fill:#bbf)

[//]: # (```)

[//]: # ()

[//]: # (```mermaid)

[//]: # (flowchart LR)

[//]: # (  [Runner.platform]--is now changed to---id2&#40;Runner.getPlatform&#40;&#41;&#41;;)

[//]: # (  style id1 fill:#f9f)

[//]: # (  style id2 fill:#bbf)

[//]: # (```)

[//]: # (```mermaid)

[//]: # (flowchart LR;)

[//]: # (  [Runner.platform] -->|is now changed to| [Runner.getPlatform&#40;&#41;])

[//]: # (  style id1 fill:#f9f)

[//]: # (  style id2 fill:#bbf)

[//]: # (```)

# What is this repository about?

This repository implements automated tests for Android & iOS apps, specified using cucumber-jvm and intelligently run
them against

* Android
* iOS
* Windows Apps
* Web

Applitools (https://applitools.com/) Visual AI, and Applitools Ultrafast Grid (https://applitools.com/product-ultrafast-test-cloud/) is integrated with this framework, to provide
Visual AI testing as part of functional automation.

Reports will be uploaded to reportportal.io, that you would need to setup separately, and provide the server details in
src/test/resources/reportportal.properties file or provide the path to the file using this environment
variable: `REPORT_PORTAL_FILE`

Test can run on local browsers / devices, or against any cloud provider, such as HeadSpin, BrowserStack, SauceLabs, pCloudy.

## Tech stack used

* cucumber-jvm (https://cucumber.io)
* AppiumTestDistribution (https://github.com/AppiumTestDistribution/AppiumTestDistribution) -manages Android and iOS
  devices, and Appium
* Appium (https://appium.io)
* WebDriver (https://selenium.dev)
* reportportal.io (https://reportportal.io)
* Applitools (https://applitools.com)
* Build tool: gradle 7.3.3
* cucumber-reporting (https://github.com/damianszczepanik/cucumber-reporting)

## [Getting started using teswiz](docs/GettingStartedUsingTeswiz-README.md)

## [Configuring the test execution](docs/ConfiguringTestExecution-README.md)

## [Running the sample tests](docs/SampleTests-README.md)

## Additional configurations

### [Running Visual Tests using Applitools Visual AI](docs/RunningVisualTests-README.md)

### [Functional/Feature Coverage](docs/FeatureCoverage-README.md)

### [Configuration parameters](docs/ConfigurationParameters-README.md)

## [Troubleshooting / FAQs](docs/FAQs-README.md)

### Contact [Anand Bagmar](https://twitter.com/BagmarAnand) for help or if you face issues using teswiz