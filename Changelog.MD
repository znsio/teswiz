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

- Updated `README.md` and `docs/RunningVisualTests-README.md` to document:
  - `useNML`
  - the explicit Figma/Applitools naming step
  - the sample `applitools.feature` usage
- Removed stale documentation references to the old `@eyes-...` approach.
