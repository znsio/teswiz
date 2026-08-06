[![](https://badges.frapsoft.com/os/v3/open-source.svg)](https://github.com/znsio/teswiz)
[![GitHub stars](https://img.shields.io/github/stars/znsio/teswiz.svg?style=flat)](https://github.com/znsio/teswiz/stargazers)
[ ![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=flat )](https://github.com/znsio/teswiz/pulls)
[![GitHub forks](https://img.shields.io/github/forks/znsio/teswiz.svg?style=social&label=Fork)](https://github.com/znsio/teswiz/network)

## Latest release status:
[![0.0.86](https://jitpack.io/v/znsio/teswiz.svg)](https://jitpack.io/#znsio/teswiz)
[![CI](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/Build_And_Run_Unit_Tests_CI.yml)
[![CodeQL](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/znsio/teswiz/actions/workflows/codeql-analysis.yml)

## Latest successful build id:
[![Latest Commit](https://img.shields.io/badge/commit-f9aa68b-blue.svg)](https://jitpack.io/#znsio/teswiz)

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

If you need to force a fresh dependency download, pass the Gradle property:

`./gradlew clean build -PforceUpdate=true`

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

teswiz also supports:
* Applitools Native Mobile Layout through `useNML`
* Applitools visual validation for web on Selenium Java and the Playwright web engines
* Verifying application with Figma designs using the explicit step
  `I have my Figma design with app name "...", test name "..." and baseline name "..." available in Applitools`
* Explicit web-engine selection through `WEB_ENGINE`:
  * web engines are first-class and explicit
  * Selenium Java continues to work as before
  * Playwright web execution is selectable through the configured web engine and the matching screen implementation

## Architecture Notes

teswiz keeps a small stable Java orchestration surface in `com.znsio.teswiz.runner`:

* `Driver`
* `Drivers`
* `Runner`
* `Setup`
* `Visual`

These remain the main framework entry points for orchestration, scenario lifecycle, and framework-facing behavior.

The dual-engine web support is intentionally organized behind internal support packages:

* `com.znsio.teswiz.session`
  * persona session metadata and registries
* `com.znsio.teswiz.config.browser`
  * browser config loading, Playwright config resolution, and migration reporting
* `com.znsio.teswiz.config.app`
  * app artifact path resolution, version detection, and download handling shared by Android, iOS, and Windows setup flows
* `com.znsio.teswiz.config.capability`
  * capability lookup and device-farm capability-file persistence shared by runner and provider setup flows
* `com.znsio.teswiz.mobile.session`
  * Appium device-session registry and internal mobile session state
* `com.znsio.teswiz.mobile.device`
  * local mobile device and simulator availability setup used by Appium orchestration
* `com.znsio.teswiz.mobile.server`
  * Appium local server lifecycle and remote hub URL normalization
* `com.znsio.teswiz.mobile.provider`
  * mobile cloud provider adapters for setup, cleanup, report-link, and provider-specific Appium behavior
* `com.znsio.teswiz.web`
  * shared web engine concepts such as `WebEngine`
* `com.znsio.teswiz.web.browser`
  * browser-engine orchestration that routes between Selenium and Playwright web engines
* `com.znsio.teswiz.web.provider`
  * provider-aware web session adapters for local and cloud execution metadata
* `com.znsio.teswiz.web.provider.selenium`
  * Selenium-specific cloud capability builders extracted from `runner` while keeping `runner` as the stable orchestration delegate
* `com.znsio.teswiz.web.selenium`
  * Selenium web engine runtime internals
* `com.znsio.teswiz.web.playwright`
  * Playwright TS worker bridge, driver, and session internals
* `com.znsio.teswiz.screen`
  * runtime screen resolution and user-invokable screen contract verification/reporting infrastructure

For `WEB_ENGINE=playwright-ts`, teswiz uses the same Java BL and screen contracts, while the framework-owned `playwright-ts` module bridge delegates to worker-side TypeScript screen modules for Playwright-native behavior.
Those test-owned TypeScript screen modules belong under `src/test/resources/playwright/screens`.

* `com.znsio.teswiz.reporting`
  * scenario metadata publishing and engine-specific artifact reporting helpers
* `com.znsio.teswiz.visual`
  * Playwright-specific visual support helpers

For client projects, the intent is:

* keep depending on the stable `runner` entry points unless explicitly documented otherwise
* treat the packages above as internal implementation packages that may evolve as dual-engine support grows
* keep user-authored sample/app test assets in `src/test`, while framework runtime and user-invokable verification/reporting infrastructure ship from `src/main`

The architecture diagram and flow are documented in [`docs/Architecture-README.md`](docs/Architecture-README.md).

To explicitly validate discovered screen implementations against their shared contracts, run:

`./gradlew verifyScreenContracts`

This command is intentionally separate from normal test execution so teams can check screen compliance on demand while adding new Selenium, Playwright, or mobile screen implementations.
For `playwright-ts`, this verification now calls out missing TypeScript screen modules separately from missing Java screen implementations.
To also flag missing target combinations more explicitly, run:

`./gradlew verifyScreenContracts -PincludeMissingScreenTargets=true`

That stricter mode is opt-in so the default verification stays focused on implementation correctness instead of every uncovered platform/engine combination.
Run focused Gradle verification commands serially on the same checkout. Parallel independent Gradle invocations against the same workspace can produce misleading compile/test failures because they share build outputs and intermediate state.

To report missing screen implementations for the currently supported target combinations, run:

`./gradlew reportMissingScreenContracts`

For Selenium, Playwright, and mixed-platform runs, teswiz now publishes shared scenario artifacts into the same report flow:

* session metadata as `scenario-session-metadata.json`
* Selenium browser logs when they are registered for the session
* Playwright trace archives
* Playwright console logs
* Playwright HAR files when generated by the worker
* Appium device logs when they are registered for the session

Web cloud/session handling is also now routed through provider adapters so BrowserStack, LambdaTest, HeadSpin, and local execution can be represented consistently in session metadata without changing user-facing scenario style.
For BrowserStack and LambdaTest web runs, teswiz now normalizes provider-native session ids and report URLs through the shared web session metadata path so Selenium and Playwright web sessions can feed the same reporting flow.

On the mobile side, extraction has started the same way:

* cloud report-link publication is provider-based
* mobile cloud setup and cleanup routing are now owned by the internal mobile provider package
* LambdaTest mobile capability shaping is now owned by the internal mobile provider package
* LambdaTest mobile app upload command/response handling is now owned by the internal mobile provider package
* BrowserStack mobile capability shaping is now owned by the internal mobile provider package
* HeadSpin mobile capability shaping is now owned by the internal mobile provider package
* pCloudy mobile capability shaping is now owned by the internal mobile provider package
* Appium device-session registry and preferred mobile session model are now owned by the internal mobile session package
* local mobile device and simulator setup is now owned by the internal mobile device package
* Appium server lifecycle and remote hub URL normalization are now owned by the internal mobile server package
* `AppiumDriverManager` and runner setup classes still remain the stable orchestration-facing delegates
* app artifact path resolution, version detection, and download handling are now owned by the internal config app package while `runner.DeviceSetup` remains the stable delegate
* capability lookup and device-farm capability-file persistence are now owned by the internal config capability package while `runner.DeviceSetup` remains the stable delegate

Reports will be uploaded to reportportal.io, that you would need to setup separately, and provide the server details in
src/test/resources/reportportal.properties file or provide the path to the file using this environment
variable: `REPORT_PORTAL_FILE`

For web cloud runs, teswiz now also emits provider report-link messages into ReportPortal when the session metadata
contains provider-native report details, so BrowserStack and LambdaTest Playwright runs expose direct execution links
alongside the shared scenario/session artifacts.

The generated Cucumber HTML report includes `WEB_ENGINE` in its execution metadata so the selected web engine is visible in the report.
For runs that publish `scenario-session-metadata.json`, the HTML report also aggregates session personas/platforms/engines/providers,
and when available, provider-native cloud session ids, report URLs, and normalized artifact URLs such as console logs,
network logs, video links, Playwright logs, command logs, and screenshots.

Test can run on local browsers / devices, or against any cloud provider, such as TestMu AI (formerly LambdaTest), HeadSpin, BrowserStack, SauceLabs, pCloudy.

## CI Batch Name Suffix for Applitools

To append a CI-specific suffix to the Applitools batch name, set `APPLITOOLS_BATCH_NAME_SUFFIX`.

Example for GitHub Actions:

`additional-env: "APPLITOOLS_BATCH_NAME_SUFFIX=' - #${{ github.run_number }}'"`

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
* Native app uploads:
  * If `CLOUD_UPLOAD_APP=true`, teswiz uploads the app to LambdaTest and uses the returned `lt://...` app id automatically.
  * If `CLOUD_UPLOAD_APP=false`, you must provide `APP_PATH=lt://...` as an environment variable, system property, or in the config file.
  * Do not use a local file path such as `temp/sampleApps/TheApp.ipa` when `CLOUD_UPLOAD_APP=false`.

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
