# Changelog

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
