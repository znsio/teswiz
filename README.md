[![0.0.3](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![0.0.3](https://jitci.com/gh/znsio/teswiz/svg)](https://jitci.com/gh/znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)


# Breaking changes in v0.0.72:

Below is the list of the breaking changes, and the corresponding new implementation starting from teswiz v0.0.72.

| Purpose                                         | Old | New |
|:------------------------------------------------| :--- | :--- |
| Create/Allocate Driver                          | `allDrivers.createDriverFor(...)` | ***`Drivers.createDriverFor(...)`*** |
| Accessing platform                              | `Runner.platform` | **`Runner.getPlatform()`** |
| Getting platform for user                       | `allDrivers.getPlatformForUser(userPersona)` | **`Drivers.getPlatformForUser(userPersona)`** |
| Retrieving ALL_DRIVERS from TestExecutioContext | `TEST_CONTEXT.ALL_DRIVERS` | ** Not required ** |

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

Applitools (https://applitools.com/) Visual AI, and Applitools Ultrafast Grid (https://applitools.com/product-ultrafast-test-cloud/) is integrated with this framework, to provide Visual AI testing as part of functional automation.

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