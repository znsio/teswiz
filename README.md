[![0.0.3](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![0.0.3](https://jitci.com/gh/znsio/teswiz/svg)](https://jitci.com/gh/znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)

# Breaking changes in v0.0.72:

Below is the list of the breaking changes, and the corresponding new implementation starting from teswiz v0.0.72.

## Package name changes

The package naming has been made consistent - **com.znsio.teswiz**.

Accordingly, the following changes will need to be made in your existing tests.

| Purpose                              | Old                                             | New                                                |
|:-------------------------------------|:------------------------------------------------|:---------------------------------------------------|
| Runner (build.gradle)                | `mainClass = "com.znsio.e2e.runner.Runner"`     | `mainClass = "com.znsio.teswiz.runner.Runner"`     |
| Importing Runner (*.java)            | `import com.znsio.e2e.runner.Runner`            | `import com.znsio.teswiz.runner.Runner`            |
| Importing Platform (*.java)          | `import com.znsio.e2e.entities.Platform`        | `import com.znsio.teswiz.entities.Platform`        |
| Importing TEST_CONTEXT (*.java)      | `import com.znsio.e2e.entities.TEST_CONTEXT`    | `import com.znsio.teswiz.entities.TEST_CONTEXT`    |
| Importing Driver (*.java)            | `import com.znsio.e2e.tools.Driver`             | `import com.znsio.teswiz.runner.Driver`            |
| Importing Drivers (*.java)           | `import com.znsio.e2e.tools.Drivers`            | `import com.znsio.teswiz.runner.Drivers`           |
| Importing Visual (*.java)            | `import com.znsio.e2e.tools.Visual`             | `import com.znsio.teswiz.runner.Visual`            |
| Importing APPLITOOLS (*.java)        | `import com.znsio.e2e.entities.APPLITOOLS`      | `import com.znsio.teswiz.entities.APPLITOOLS`      |
| Importing waitFor (*.java)           | `import com.znsio.e2e.tools.Wait.waitFor`       | `import com.znsio.teswiz.tools.Wait.waitFor`       |
| Importing custom exceptions (*.java) | `import com.znsio.e2e.exceptions.*`             | `import com.znsio.teswiz.exceptions.*`             |
| Importing Randomizer (*.java)        | `import static com.znsio.e2e.tools.Randomizer;` | `import static com.znsio.teswiz.tools.Randomizer;` |
| Importing Hooks (*.java)             | `import com.znsio.e2e.steps.Hooks`              | `import com.znsio.teswiz.steps.Hooks`              |
| Platform (build.gradle)              | `mainClass = "com.znsio.e2e.runner.Runner"`     | `mainClass = "com.znsio.teswiz.runner.Runner"`     |

## Method name changes

There are some method name changes as listed below:

| Purpose                                          | Old                                                   | New                                                                          |
|:-------------------------------------------------|:------------------------------------------------------|:-----------------------------------------------------------------------------|
| Create/Allocate Driver                           | `allDrivers.createDriverFor(...)`                     | **`Drivers.createDriverFor(...)`**                                           |
| Accessing platform                               | `Runner.platform`                                     | **`Runner.getPlatform()`**                                                   |
| Getting platform for user                        | `allDrivers.getPlatformForUser(userPersona)`          | **`Runner.getPlatformForUser(userPersona)`**                                 |
| Retrieving ALL_DRIVERS from TestExecutionContext | `TEST_CONTEXT.ALL_DRIVERS`                            | ** Not required **                                                           |
| Retrieving test data                             | `public static Map getTestDataAsMap(String key)`      | **`public static Map<String, Object> getTestDataAsMap(String key)`**         |
| Getting the Driver for the current user          | `Runner.fetchDriver(Thread.currentThread().getId());` | **`Drivers.getDriverForCurrentUser(Thread.currentThread().getId());`**       | 
| Getting the Visual driver for the current user   | `Runner.fetchEyes(Thread.currentThread().getId());`   | **`Drivers.getVisualDriverForCurrentUser(Thread.currentThread().getId());`** |

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