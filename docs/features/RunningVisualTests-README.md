# Applitools configuration
To run Visual Tests against dedicated Applitools instance, add a property serverUrl in applitools_config.json.
Ex: "serverUrl": "https://eyesapi.applitools.com"
* By default, the free public Applitools cloud will be used

To provide proxy information for Applitools, add/update the value of the APPLITOOLS_PROXY_KEY property in the applitools_config.json 
NOTE: If proxy should be set, what is the environment variable specifying the proxy?

Example:

    APPLITOOLS_PROXY_KEY=HTTP_PROXY


**To enable Applitools Ultrafast Grid, follow these steps:**
* In applitools_config.json, set`useUFG": true`
* In applitools_config.json, set `testConcurrency": 5` to the appropriate concurrency level as per your Applitools
  license
* In RunCukesTest file, or any file where you have your custom hooks, add the following lines:

```
import com.applitools.eyes.selenium.*;
import com.applitools.eyes.visualgrid.model.*;
```

In beforeScenario, add the specific browser and device configurations to `Configuration` and add that to the
TestExecutionContext - `context` as shown below:
```
@Before
public void beforeTestScenario (Scenario scenario) {
    LOGGER.info(String.format("ThreadID: %d: in overridden beforeTestScenario%n", Thread.currentThread().getId()));
    Configuration ufgConfig = new Configuration();
    ufgConfig.addBrowser(1024, 1024, BrowserType.CHROME);
    ufgConfig.addBrowser(1024, 1024, BrowserType.FIREFOX);
    ufgConfig.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
    ufgConfig.addDeviceEmulation(DeviceName.OnePlus_7T_Pro, ScreenOrientation.LANDSCAPE);
    context.addTestState(APPLITOOLS.UFG_CONFIG, ufgConfig);
}
```

IF you have `useUFG` set to `true`, and if you do not specify the Ultrafast Grid configuration, then teswiz has a
default set of browser and devices specified which will be used for Visual Validation. The default configuration is
shown below:

```
  ufgConfig.addBrowser(1024, 1024, BrowserType.CHROME);
  ufgConfig.addBrowser(1024, 1024, BrowserType.FIREFOX);
  ufgConfig.addBrowser(1024, 1024, BrowserType.SAFARI);
  ufgConfig.addBrowser(1024, 1024, BrowserType.EDGE_CHROMIUM);
  ufgConfig.addBrowser(1600, 1200, BrowserType.CHROME);
  ufgConfig.addBrowser(1600, 1200, BrowserType.FIREFOX);
  ufgConfig.addBrowser(1600, 1200, BrowserType.SAFARI);
  ufgConfig.addBrowser(1600, 1200, BrowserType.EDGE_CHROMIUM);
  ufgConfig.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
  ufgConfig.addDeviceEmulation(DeviceName.iPad_Pro, ScreenOrientation.LANDSCAPE);
  ufgConfig.addDeviceEmulation(DeviceName.Nexus_5X, ScreenOrientation.PORTRAIT);
  ufgConfig.addDeviceEmulation(DeviceName.Nexus_6P, ScreenOrientation.LANDSCAPE);
```

**To enable Applitools Native Mobile Layout, follow these steps:**
* In `applitools_config.json`, set `"useNML": true`
* In your custom hooks file, add the platform-specific native mobile device targets to `TestExecutionContext`
* teswiz will apply that configuration only to `appEyes`

Example:

```java
import com.applitools.eyes.visualgrid.model.AndroidMultiDeviceTarget;
import com.applitools.eyes.visualgrid.model.IosMultiDeviceTarget;

private void addApplitoolsNMLConfigurationToContext() {
    if (Platform.iOS.equals(Runner.getPlatform())) {
        context.addTestState(APPLITOOLS.NML_CONFIG, new IosMultiDeviceTarget[]{
                IosMultiDeviceTarget.iPhone_14(),
                IosMultiDeviceTarget.iPhone_14_Pro_Max()
        });
    } else if (Platform.android.equals(Runner.getPlatform())) {
        context.addTestState(APPLITOOLS.NML_CONFIG, new AndroidMultiDeviceTarget[]{
                AndroidMultiDeviceTarget.Galaxy_S25(),
                AndroidMultiDeviceTarget.Galaxy_S25_Ultra(),
                AndroidMultiDeviceTarget.Pixel_9()
        });
    }
}
```

`NML_CONFIG` can contain one or many `IosMultiDeviceTarget` or `AndroidMultiDeviceTarget` values. teswiz will apply
all items present in the array to `appEyes`.

# Implementing Visual Validation using Applitools Visual AI
From your screen methods, whenever you want to do visual validation, you can call one of these methods:

Example:
  
    visually.checkWindow(SCREEN_NAME, "enterMeetingDetails");

    visually.check(SCREEN_NAME, "entered login details",
                       Target.window().fully().layout(userNameElement, passwordElement));

## Web engine notes

The checked-in sample config files default to:

* `WEB_ENGINE=selenium`

Override it at runtime when you want to validate the same web flow on a Playwright engine:

* `WEB_ENGINE=playwright-java`
* `WEB_ENGINE=playwright-ts`

Internally, Selenium web runs continue to use the Selenium Eyes path, while Playwright web runs now use the official Applitools Playwright SDK path for the selected Playwright engine.
This keeps Selenium behavior stable while making Playwright visual runs first-class Eyes sessions instead of screenshot-only image submissions.

* `WEB_ENGINE=selenium`
  * uses the Selenium Applitools SDK path
  * supports Ultrafast Grid (`useUFG`) as documented above
* `WEB_ENGINE=playwright-java`
  * uses the official Applitools Playwright Java SDK path
  * publishes engine-aware app, test, and batch naming for web visual runs
  * adds `WEB_ENGINE`, `BROWSER_NAME`, `CONFIGURED_VIEWPORT_SIZE`, and `EFFECTIVE_VIEWPORT_SIZE` as Applitools custom properties
  * preserves teswiz Figma naming override, batch identity, baseline environment selection, logs, and visual result handling
  * supports `useUFG=true` with the same teswiz `UFG_CONFIG` browser/device target model used by Selenium web runs
* `WEB_ENGINE=playwright-ts`
  * uses the Playwright TS worker-backed official Applitools Playwright SDK path
  * publishes engine-aware app, test, and batch naming for web visual runs
  * adds `WEB_ENGINE`, `BROWSER_NAME`, `CONFIGURED_VIEWPORT_SIZE`, and `EFFECTIVE_VIEWPORT_SIZE` as Applitools custom properties
  * preserves teswiz Figma naming override, batch identity, baseline environment selection, logs, and visual result handling
  * supports `useUFG=true` with the same teswiz `UFG_CONFIG` browser/device target model used by Selenium web runs
  * supports `checkWindow(...)`, `checkWindow(..., MatchLevel)`, and window-based `visually.check(...)` flows for the current Playwright web screen implementations
  * does not yet support Selenium-specific constructs such as frame-based visual checks, region-based web checks, floating regions, dynamic regions, or accessibility regions
  * runs as Playwright-native Eyes sessions and now fans out UFG renders from the same teswiz target config model as Selenium

# Using explicit Figma / Applitools naming

If you want to compare against an existing Figma-published Applitools baseline, use the explicit step below:

```gherkin
Given I have my Figma design with app name "Applitools", test name "Important pages" and baseline name "vodqa_screens" available in Applitools
```

When this step is called, teswiz stores the provided app name, test name, and baseline environment name in the
`TestExecutionContext`.

If all three values are valid, teswiz uses them when creating the Applitools session for both:
* `webEyes`
* `appEyes`

If any one of the three values is missing or blank, the test fails with a `VisualTestSetupException`.

See the sample feature for a working end-to-end example:
[`src/test/resources/com/znsio/teswiz/features/applitools.feature`](../../src/test/resources/com/znsio/teswiz/features/applitools.feature)

Example from that feature:

```gherkin
@applitools
Feature: Scenarios for "Applitools"

  #IS_VISUAL=true CONFIG=./configs/applitools/applitools_local_web_config.properties ./gradlew run
  @web @figma
  Scenario: Compare Applitools important pages with Figma design
    Given I have my Figma design with app name "Applitools website", test name "Applitools Full Pages" and baseline name "Applitools Full Pages_1506" available in Applitools
    And I visually check the "integrations page" at "https://applitools.com/platform/integrations/"
    And I visually check the "what's new page" at "https://applitools.com/platform/whats-new/"
```

# Running the tests with Applitools Visual AI

To enable Visual test automation using Applitools Visual AI, follow the steps below:
* In build.gradle, provide your APPLITOOLS_API_KEY:

  > environment "APPLITOOLS_API_KEY", System.getenv("TESWIZ_APPLITOOLS_API_KEY")

* Enable visual validation by setting `IS_VISUAL=true` in either of:
  * the config file, or
  * from the command line - ex: `CONFIG=./configs/jiomeet_local_config.properties IS_VISUAL=true ./gradlew run`, or
  * as an environment variable

For web runs, the report metadata in the generated Cucumber HTML report includes `WEB_ENGINE`, and when scenario session metadata is available it also includes aggregated `SESSION_PERSONAS`, `SESSION_PLATFORMS`, `SESSION_ENGINES`, `SESSION_PROVIDERS`, and provider-side session artifact links such as `SESSION_PROVIDER_REPORT_URLS`, `SESSION_PROVIDER_CONSOLE_LOG_URLS`, and `SESSION_PROVIDER_NETWORK_LOG_URLS`.
This helps visual report consumers understand mixed persona, platform, engine, and provider coverage for the run.

For Applitools itself, web visual sessions also now carry engine-aware metadata directly in Eyes:
* engine-aware app naming for Playwright web runs
* engine-aware test naming for Playwright web runs
* engine-aware batch naming for web runs
* custom properties including `WEB_ENGINE`, `BROWSER_NAME`, `CONFIGURED_VIEWPORT_SIZE`, and `EFFECTIVE_VIEWPORT_SIZE`
