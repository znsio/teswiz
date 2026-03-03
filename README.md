[![](https://badges.frapsoft.com/os/v3/open-source.svg)](https://github.com/znsio/teswiz)
[![GitHub stars](https://img.shields.io/github/stars/znsio/teswiz.svg?style=flat)](https://github.com/znsio/teswiz/stargazers)
[ ![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=flat )](https://github.com/znsio/teswiz/pulls)
[![GitHub forks](https://img.shields.io/github/forks/znsio/teswiz.svg?style=social&label=Fork)](https://github.com/znsio/teswiz/network)

## Latest release status:
[![0.0.86](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)

## Latest successful build id:
[![Latest Commit](https://img.shields.io/badge/commit-a298f7d-blue.svg)](https://jitpack.io/#znsio/teswiz)

## 🚨 Breaking Changes

### From Version `1.0.13` onward

As part of package restructuring, context-related classes have moved to a new package.

#### ❗ Required Update in Imports

Replace:

```java
import com.context.SessionContext;
import com.context.TestExecutionContext;
```

With:

```java
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
```

# NOTE

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

Test can run on local browsers / devices, or against any cloud provider, such as TestMu AI (formerly LambdaTest), HeadSpin, BrowserStack, SauceLabs, pCloudy.

## Cloud provider notes

### TestMu AI (formerly LambdaTest)

* Supported config/capability samples in this repo:
  * `configs/theapp/theapp_lambdatest_web_config.properties`
  * `configs/theapp/theapp_lambdatest_android_config.properties`
  * `configs/theapp/theapp_lambdatest_ios_config.properties`
  * `caps/theapp/theapp_lambdatest_web_capabilities.json`
  * `caps/theapp/theapp_lambdatest_android_capabilities.json`
  * `caps/theapp/theapp_lambdatest_ios_capabilities.json`
* Web runs use W3C-safe capabilities, with LambdaTest-specific keys inside `LT:Options`.
* Mobile runs use LambdaTest-specific keys inside `lt:options`.
* `network` and `appProfiling` are read from capability files (not hardcoded by framework).

### BrowserStack

* Web runs use `bstack:options` / `browserstackOptions` mapping for BrowserStack-specific options.
* Native app uploads:
  * For iOS uploads (`.ipa` and `.zip`), upload command includes `ios_keychain_support=true`.
  * For non-iOS uploads (for example `.apk`), that flag is not added.

## Tech stack used

* **JDK 17**
* cucumber-jvm (https://cucumber.io)
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

## [Trouble downloading teswiz from jitpack.io?](docs/teswizDownloadIssue.md)

### Contact [Anand Bagmar](https://twitter.com/BagmarAnand) for help or if you face issues using teswiz
