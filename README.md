[![](https://badges.frapsoft.com/os/v3/open-source.svg)](https://github.com/znsio/teswiz)
[![GitHub stars](https://img.shields.io/github/stars/znsio/teswiz.svg?style=flat)](https://github.com/znsio/teswiz/stargazers)
[ ![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=flat )](https://github.com/znsio/teswiz/pulls)
[![GitHub forks](https://img.shields.io/github/forks/znsio/teswiz.svg?style=social&label=Fork)](https://github.com/znsio/teswiz/network)

## Latest release status:
[![0.0.86](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)

## Latest successful build id:
[![Latest Commit](https://img.shields.io/badge/commit-3564dd18c6-blue.svg)](https://jitpack.io/#znsio/teswiz)

# NOTE
  
    Use appium-device-farm v8.4.7-rc.8 or later

    Use JDK v17 or higher

# To Build
`./gradlew clean build`

# What is this repository about?

This repository implements automated tests for Android & iOS apps, specified using cucumber-jvm and intelligently run
them against

* Android
* iOS
* Windows Apps
* Web 
* Electron

Applitools (https://applitools.com/) Visual AI, and Applitools Ultrafast Grid (https://applitools.com/product-ultrafast-test-cloud/) is integrated with this framework, to provide
Visual AI testing as part of functional automation.

Reports will be uploaded to reportportal.io, that you would need to setup separately, and provide the server details in
src/test/resources/reportportal.properties file or provide the path to the file using this environment
variable: `REPORT_PORTAL_FILE`

Test can run on local browsers / devices, or against any cloud provider, such as HeadSpin, BrowserStack, SauceLabs, pCloudy.

## Tech stack used

* **JDK 17**
* cucumber-jvm (https://cucumber.io)
* AppiumTestDistribution (https://github.com/AppiumTestDistribution/AppiumTestDistribution) -manages Android and iOS
  devices, and Appium
* Appium 2.x (https://appium.io) 
  * https://javadoc.io/doc/io.appium/java-client/8.0.0-beta/deprecated-list.html
* Selenium WebDriver 4.x (https://selenium.dev)
  * https://www.selenium.dev/selenium/docs/api/java/deprecated-list.html 
* reportportal.io (https://reportportal.io)
* Applitools (https://applitools.com)
* Build tool: gradle v8
* cucumber-reporting (https://github.com/damianszczepanik/cucumber-reporting)

## [Prerequisites](docs/Prerequisites-README.md)

## [Getting started using teswiz](docs/GettingStartedUsingTeswiz-README.md)

## [Configuring the test execution](docs/ConfiguringTestExecution-README.md)

## [Running the sample tests](docs/SampleTests-README.md)

## [Writing the first test](docs/WritingFirstTest-README.md)

## [Setting up the Hard Gate](./docs/HardGate.md)

## Additional configurations

### [Running Visual Tests using Applitools Visual AI](docs/RunningVisualTests-README.md)

### [Functional/Feature Coverage](docs/FeatureCoverage-README.md)

### [Configuration parameters](docs/ConfigurationParameters-README.md)

### [Add Auto Logging Using AspectJ](docs/AspectJLogging-README.md)

### [Setting up docker containers](docs/DockerSetup-README.md)

### [Logging to ReportPortal](docs/ReportPortal-README.md)

## [BREAKING CHANGES from v0.0.81](docs/BreakingChanges-README.md)

## [Troubleshooting / FAQs](docs/FAQs-README.md)

### Contact [Anand Bagmar](https://twitter.com/BagmarAnand) for help or if you face issues using teswiz
