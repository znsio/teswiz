## 1.0.32
- Add tests for EnvironmentIssueFilter and its opt-out flag
- 🔄 Update README with latest commit ID - 0c574e4
- Wire EnvironmentIssueFilter into RestAssuredService, on by default
- 🔄 Update README with latest commit ID - a83cb28
- Track docs/images/ despite the blanket *.png gitignore rule
- Fix stale org references and broken dependency guidance across docs
- Add DocumentationLinksTest to guard against broken doc links
- Fix broken links across README and docs
- 🔄 Update README with latest commit ID - 3c7981e
- Refactor publishRelease.sh into small, named functions
- Retry Jitpack build trigger once if the tag isn't visible yet
- Trigger Jitpack build right after tag push, before the slow fat jar upload
- 🔄 Update README with latest commit ID - 5fa27c1

## 1.0.31
- Correct api/implementation split: reflection/weaving isn't a real API leak
- 🔄 Update README with latest commit ID - c25dadc

## 1.0.30
- 🔄 Update README with latest commit ID - 4b96e86
- Fix flaky BrowserStack executor command test after RestAssured migration
- Publish teswiz as both a thin jar and a shaded "all" fat jar
- Stop building the 486MB shadow jar as part of assemble/build
- Migrate HTTP client from Unirest to RestAssured
- 🔄 Update README with latest commit ID - f8c27a6

## 1.0.29
- 🔄 Update README with latest commit ID - 238ffe7
- Fix screen resolver to support consumer-defined package namespaces Update Playwright docs to reflect configurable screen-contract package convention
- 🔄 Update README with latest commit ID - c5cf675

## 1.0.28
- 🔄 Update README with latest commit ID - c07f8f3
- Align Playwright version and fix BrowserStack capabilities
- 🔄 Update README with latest commit ID - 011650c
- Use layout algo to compare the "Vertical Swiping Screen After Scroll" screen
- Gracefully handle WebDriverException during teardown
- BrowserStack requires both browserstack.playwrightVersion and client.playwrightVersion to match the local client's Playwright library version exactly
- Align BrowserStack Playwright capabilities version and add diagnostics
- Improve release badge styling and add worker diagnostics
- 🔄 Update README with latest commit ID - ab5305a
- Fix Playwright TS worker page.goto timeout error
- 🔄 Update README with latest commit ID - 7788d4c
- Fix release version script and sync package-lock.json
- Sync package-lock.json with package.json
- Migrate Playwright-TS screens to native selectors
- Migrate GoogleSearch and Indigo Playwright-Java screens to native APIs
- Migrate JioMeet Playwright-Java screens to native APIs
- feat: add native playwright-java screen context and convert theapp screens
- fix: use plain version tags in publishRelease

## v1.0.27
- refactor: prompt for unit-test execution in publishRelease
- docs: remove outdated repository notice from README
- feat: add skip-unit-tests flag to publishRelease script
- Update CI workflows to restrict push events to the main branch only
- 🔄 Update README with latest commit ID - 83d51a9
- Add first-class Playwright Java and Playwright TS web engines to teswiz (#2)
- 🔄 Update README with latest commit ID - 407d445
- Update environment variable for Applitools API key in build.gradle
- 🔄 Update README with latest commit ID - a237f60
- Create codeql.yml
- 🔄 Update README with latest commit ID - db402e1
- Fix GitHub Action permission issues
- Add publishRelease.sh automation script
- Point GitHub workflows, badges, and build configs to anandbagmar fork
- Add temporarily moved notice to README.md
- fix(deps): patch @appium/support, ajv, @babel/core, diff transitive vulnerabilities
- Remove JioMeet-Lite binary path from browser configuration for Electron apps
- fix(deps): reduce vulnerability backlog in teswiz
- fix(deps): reduce high and critical npm vulnerabilities in teswiz

# Changelog

## v1.0.26

### Driver enhancements

- Added a reusable page source dump helper that writes `dom-dump-<index>.html` for web and `hierarchy-dump-<index>.xml` for Android/iOS into the active scenario log directory.
- Added per-scenario dump indexing so repeated calls create sequential files that reset with each scenario.
- Reorganized driver import statements for clarity.

### Tests

- Updated the TheApp invalid-login scenario to execute the second login attempt again so the web flow covers the reusable invalid-login path.

## v1.0.25

### Runner

- Auto-include the `com.znsio.teswiz.steps` package as a second `--glue` entry so reusable built-in steps (`FigmaSteps`, `CommonSteps`, etc.) are discoverable in consuming projects without extra glue configuration.

### Driver enhancements

- Added a maximum scroll attempts limit and improved element visibility checks.

### Figma and Applitools

- Added the missing step to navigate to the application page in the Figma design scenario.
- Updated UFG device emulation to iPhone 15 Pro in the UFG configuration.
- Removed unused driver imports and driver creation call in the Figma step.

## v1.0.24

### Driver enhancements

- Enhanced click methods and context switching functionality in the driver.

### Build and dependency updates

- Updated `appium-xcuitest-driver` to version `11.13.0`.
- Updated Gradle to `9.6.0`.
- Updated all Java dependencies.
- Updated GitHub Actions to v7.
- Refreshed npm/node packages.

## v1.0.23

### Build and dependency updates

- Changed dependency download behavior so `forceUpdate` now defaults to `false`.
- Added a Gradle override so `-PforceUpdate=true` forces a fresh jar download when needed.
- Updated the README guidance to document the Gradle property-based override.
- Kept `build.gradle.sample` aligned with the new default and override flow.

## v1.0.22

### Build and dependency updates

- Updated the Gradle wrapper to `9.5.1`.
- Improved Gradle launcher script compatibility in `gradlew` and `gradlew.bat`.
- Refreshed npm dependencies in `package.json` and regenerated `package-lock.json`.
- Updated Gradle plugin and dependency versions in `build.gradle`.

## v1.0.21

### Visual testing

- Fixed Applitools mobile capability setup for Appium visual runs so it uses the initialized Applitools configuration map correctly.
- Scoped Applitools mobile capability injection to runs where both `IS_VISUAL=true` and `useNML=true`.
- Simplified proxy settings initialization in `Visual.addMobileCapabilitiesToTestContextForVisualTesting`.

## v1.0.20

### Visual testing

- Added support for Native Mobile Layout configuration through `useNML`.
- Added platform-specific NML device configuration support from test context for Android and iOS visual runs.
- Updated visual setup so NML supports one or many configured mobile device targets.

### Figma and Applitools baseline comparison

- Replaced the old scenario-tag-based `@eyes-...` baseline flow with an explicit Cucumber step:
  `I have my Figma design with app name "...", test name "..." and baseline name "..." available in Applitools`
- Added new `TestExecutionContext` keys for:
  - `APPLITOOLS_FIGMA_APP_NAME`
  - `APPLITOOLS_FIGMA_TEST_NAME`
  - `APPLITOOLS_FIGMA_BASELINE_ENV_NAME`
- Updated `Visual` to use the explicit Figma-provided app name, test name, and baseline environment name before creating Applitools sessions.
- Added validation so partial or blank Figma/Applitools naming input fails fast with `VisualTestSetupException`.

### Tests

- Added focused unit tests for Figma/Applitools context resolution in `VisualTest`.
- Added focused unit tests for storing Figma naming details in `FigmaStepsTest`.

### Documentation

- Updated `README.md` and `docs/features/RunningVisualTests-README.md` to document:
  - `useNML`
  - the explicit Figma/Applitools naming step
  - the sample `applitools.feature` usage
- Removed stale documentation references to the old `@eyes-...` approach.
