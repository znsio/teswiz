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

# Implementing Visual Validation using Applitools Visual AI
From your screen methods, whenever you want to do visual validation, you can call one of these methods:

Example:
  
    visually.checkWindow(SCREEN_NAME, "enterMeetingDetails");

    visually.check(SCREEN_NAME, "entered login details",
                       Target.window().fully().layout(userNameElement, passwordElement));

# Running the tests with Applitools Visual AI

To enable Visual test automation using Applitools Visual AI, follow the steps below:
* In build.gradle, provide your APPLITOOLS_API_KEY:

  > environment "APPLITOOLS_API_KEY", System.getenv("teswiz_APPLITOOLS_API_KEY")

* Enable visual validation by setting `IS_VISUAL=true` in either of:
  * the config file, or
  * from the command line - ex: `CONFIG=./configs/jiomeet_local_config.properties IS_VISUAL=true ./gradlew run`, or
  * as an environment variable