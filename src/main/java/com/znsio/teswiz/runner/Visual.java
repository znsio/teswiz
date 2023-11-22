package com.znsio.teswiz.runner;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.AppiumCheckSettings;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.ClassicRunner;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.znsio.teswiz.entities.APPLITOOLS;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.exceptions.VisualTestSetupException;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.ScreenShotManager;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.znsio.teswiz.runner.Runner.*;

public class Visual {
    private static final Logger LOGGER = Logger.getLogger(Visual.class.getName());
    private static final String DEFAULT_APPLITOOLS_SERVER_URL = "https://eyesapi.applitools.com";
    private final com.applitools.eyes.selenium.Eyes eyesOnWeb;
    private final com.applitools.eyes.appium.Eyes eyesOnApp;
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final ScreenShotManager screenShotManager;
    private final String targetEnvironment = Runner.getTargetEnvironment();
    private final Map applitoolsConfig;
    private final boolean isEnableBenchmarkPerValidation;
    private final boolean isVerboseLoggingEnabled;
    private final WebDriver innerDriver;
    private static final int DEFAULT_UFG_CONCURRENCY = 5;
    private final String userPersona;
    private static final String VISUAL_TESTING_DIFFERENCES_FOUND = "Visual testing differences " +
                                                                   "found? - " + "%s";
    private static final String APP_CHECK_WINDOW_TIME_TAKEN = "%s :%s:: App: checkWindow: Time " +
                                                              "taken: %d" + " sec ";
    private static final String WEB_CHECK_WINDOW_TIME_TAKEN = "%s :%s:: Web: checkWindow: Time " +
                                                              "taken: %d" + " sec ";
    private String applitoolsLogFileNameForWeb = NOT_SET;
    private EyesRunner seleniumEyesRunner;

    public Visual(String driverType, Platform platform, WebDriver innerDriver, String testName,
                  String userPersona, String appName) {
        boolean isVisualTestingEnabled = Runner.isVisualTestingEnabled();
        LOGGER.info(String.format(
                "Visual constructor: Driver type: %s, platform: %s, testName: %s, " +
                "isVisualTestingEnabled:  %s",
                driverType, platform.name(), testName, isVisualTestingEnabled));
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        long threadId = Thread.currentThread().getId();
        this.softly = Runner.getSoftAssertion(threadId);
        this.screenShotManager = (ScreenShotManager) context.getTestState(
                TEST_CONTEXT.SCREENSHOT_MANAGER);
        this.applitoolsConfig = Runner.getApplitoolsConfiguration();
        this.isEnableBenchmarkPerValidation = Boolean.parseBoolean(String.valueOf(
                this.applitoolsConfig.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
        this.innerDriver = innerDriver;
        this.isVerboseLoggingEnabled = getValueFromConfig(APPLITOOLS.SHOW_LOGS, true);
        this.userPersona = userPersona;
        appName = appName.equalsIgnoreCase(DEFAULT) ? (String) this.applitoolsConfig.get(
                APPLITOOLS.APP_NAME) : appName;
        eyesOnApp = instantiateAppiumEyes(driverType, platform, innerDriver, appName, testName,
                                          isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, platform, innerDriver, appName, testName,
                                       isVisualTestingEnabled);
    }

    private boolean getValueFromConfig(String key, boolean defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : Boolean.parseBoolean(
                String.valueOf(applitoolsConfig.get(key)));
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes(String driverType,
                                                                  Platform platform,
                                                                  WebDriver innerDriver,
                                                                  String appName, String testName,
                                                                  boolean isVisualTestingEnabled) {
        if(driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        LOGGER.info(String.format("instantiateAppiumEyes: isVisualTestingEnabled: %s",
                                  isVisualTestingEnabled));
        com.applitools.eyes.appium.Eyes appEyes = new com.applitools.eyes.appium.Eyes();

        appEyes.setServerUrl(
                getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        appEyes.setApiKey(getApplitoolsAPIKey(isVisualTestingEnabled));
        appEyes.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        appEyes.setBranchName(String.valueOf(getValueFromConfig(Setup.BRANCH_NAME)));
        appEyes.setEnvName(targetEnvironment);
        appEyes.setMatchLevel(
                (MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));
        appEyes.setIsDisabled(!isVisualTestingEnabled);

        String applitoolsLogFileNameForApp = getApplitoolsLogFileNameFor("app");
        appEyes.setLogHandler(
                new FileLogger(applitoolsLogFileNameForApp, true, isVerboseLoggingEnabled));

        appEyes.setIgnoreCaret(true);
        appName = appName + "-" + platform;
        appEyes.addProperty("USER_PERSONA", userPersona);
        appEyes.addProperty("HOST_NAME", Runner.getHostName());
        appEyes.addProperty(Setup.BRANCH_NAME,
                            String.valueOf(getValueFromConfig(Setup.BRANCH_NAME)));
        appEyes.addProperty(Setup.PLATFORM, platform.name());
        appEyes.addProperty(Setup.RUN_IN_CI, String.valueOf(getValueFromConfig(Setup.RUN_IN_CI)));
        appEyes.addProperty(Setup.TARGET_ENVIRONMENT,
                            String.valueOf(getValueFromConfig(Setup.TARGET_ENVIRONMENT)));
        appEyes.addProperty("USER_NAME", USER_NAME);
        appEyes.setIgnoreDisplacements(getValueFromConfig(APPLITOOLS.IGNORE_DISPLACEMENT, true));
        try {
            String proxyUrl = (String) applitoolsConfig.get(APPLITOOLS.PROXY_URL);
            if (null != proxyUrl) {
                LOGGER.info(String.format("Set proxyUrl for appEyes: %s", proxyUrl));
                appEyes.setProxy(new ProxySettings(proxyUrl));
            }
            appEyes.open(innerDriver, appName, testName);
            LOGGER.info(String.format(
                    "instantiateAppiumEyes: Is Applitools Visual Testing enabled? - %s",
                    !appEyes.getIsDisabled()));
        } catch(IllegalArgumentException e) {
            String message = String.format(
                    "Exception in instantiating Applitools for App: '%s', Closing driver instance",
                    e.getMessage());
            LOGGER.error(message);
            innerDriver.quit();
            throw new VisualTestSetupException(message, e);
        }

        return appEyes;
    }

    private String getApplitoolsAPIKey(boolean isVisualTestingEnabled) {
        return isVisualTestingEnabled ? getValueFromConfig(APPLITOOLS.API_KEY, null)
                                      : getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET);
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes(String driverType,
                                                                 Platform platform,
                                                                 WebDriver innerDriver,
                                                                 String appName, String testName,
                                                                 boolean isVisualTestingEnabled) {
        if(driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        LOGGER.info(String.format("instantiateWebEyes: isVisualTestingEnabled: %s",
                                  isVisualTestingEnabled));
        boolean isUFG = getValueFromConfig(APPLITOOLS.USE_UFG, false);

        int ufgConcurrency = getValueFromConfig(APPLITOOLS.CONCURRENCY, DEFAULT_UFG_CONCURRENCY);
        seleniumEyesRunner = isUFG ? new VisualGridRunner(ufgConcurrency) : new ClassicRunner();
        seleniumEyesRunner.setDontCloseBatches(true);

        com.applitools.eyes.selenium.Eyes webEyes = new com.applitools.eyes.selenium.Eyes(
                seleniumEyesRunner);
        Configuration configuration = webEyes.getConfiguration();
        configuration.setServerUrl(
                getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        configuration.setApiKey(getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET));
        configuration.setApiKey(getApplitoolsAPIKey(isVisualTestingEnabled));
        configuration.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));

        configuration.setBranchName(String.valueOf(getValueFromConfig(Setup.BRANCH_NAME)));
        configuration.setEnvironmentName(targetEnvironment);
        configuration.setMatchLevel(
                (MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));

        configuration.setDisableBrowserFetching(
                getValueFromConfig(APPLITOOLS.DISABLE_BROWSER_FETCHING, true));
        configuration.setSendDom(getValueFromConfig(APPLITOOLS.SEND_DOM, true));
        configuration.setStitchMode(StitchMode.valueOf(
                String.valueOf(getValueFromConfig(APPLITOOLS.STITCH_MODE, StitchMode.CSS))
                      .toUpperCase()));
        configuration.setForceFullPageScreenshot(
                getValueFromConfig(APPLITOOLS.TAKE_FULL_PAGE_SCREENSHOT, true));

        addBrowserAndDeviceConfigForUFG(isUFG, configuration);

        webEyes.setConfiguration(configuration);

        applitoolsLogFileNameForWeb = getApplitoolsLogFileNameFor("web");
        webEyes.setIsDisabled(!isVisualTestingEnabled);
        webEyes.setLogHandler(
                new FileLogger(applitoolsLogFileNameForWeb, true, isVerboseLoggingEnabled));

        appName = appName + "-" + platform;
        webEyes.addProperty("USER_PERSONA", userPersona);
        webEyes.addProperty("HOST_NAME", Runner.getHostName());
        webEyes.addProperty(Setup.BRANCH_NAME,
                            String.valueOf(getValueFromConfig(Setup.BRANCH_NAME)));
        webEyes.addProperty(Setup.PLATFORM, platform.name());
        webEyes.addProperty(Setup.RUN_IN_CI, String.valueOf(getValueFromConfig(Setup.RUN_IN_CI)));
        webEyes.addProperty(Setup.TARGET_ENVIRONMENT,
                            String.valueOf(getValueFromConfig(Setup.TARGET_ENVIRONMENT)));
        webEyes.addProperty("USER_NAME", USER_NAME);

        RectangleSize setBrowserViewPortSize = getBrowserViewPortSize(driverType, innerDriver);
        LOGGER.info(String.format("Using browser dimensions for Applitools: %s",
                                  setBrowserViewPortSize));

        try {
            String proxyUrl = (String) applitoolsConfig.get(APPLITOOLS.PROXY_URL);
            if (null != proxyUrl) {
                LOGGER.info(String.format("Set proxyUrl for webEyes: %s", proxyUrl));
                webEyes.setProxy(new ProxySettings(proxyUrl));
            }
            webEyes.open(innerDriver, appName, testName, setBrowserViewPortSize);
            LOGGER.info(
                    String.format("instantiateWebEyes:  Is Applitools Visual Testing enabled? - %s",
                                  !webEyes.getIsDisabled()));
        } catch(IllegalArgumentException | EyesException e) {
            String message = String.format(
                    "Exception in instantiating Applitools for Web: '%s', Closing Web-driver " +
                    "instance",
                    e.getMessage());
            LOGGER.error(message);
            innerDriver.quit();
            throw new VisualTestSetupException(message, e);

        }
        return webEyes;
    }

    private String getValueFromConfig(String key, String defaultValue) {
        return (null == applitoolsConfig.get(key) || applitoolsConfig.get(key).equals("null"))
               ? defaultValue : String.valueOf(applitoolsConfig.get(key));
    }

    private Object getValueFromConfig(String key) {
        return applitoolsConfig.get(key);
    }

    private Object getValueFromConfig(String key, Object defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : applitoolsConfig.get(key);
    }

    @NotNull
    private String getApplitoolsLogFileNameFor(String appType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + context.getTestStateAsString(
                TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        return String.format("%s%sdeviceLogs%sapplitools-%s.log", scenarioLogDir, File.separator,
                             File.separator, appType);
    }

    private int getValueFromConfig(String key, int defaultValue) {
        Object valueFromConfig = applitoolsConfig.get(key);
        return (null == valueFromConfig) ? defaultValue
                                         : convertValueFromConfigToInt(valueFromConfig);
    }

    private void addBrowserAndDeviceConfigForUFG(boolean isUFG, Configuration configuration) {
        if(isUFG) {
            Configuration ufgConfig = (Configuration) context.getTestState(APPLITOOLS.UFG_CONFIG);
            ufgConfig = defaultApplitoolsUFGConfig(ufgConfig);
            List<RenderBrowserInfo> browsersInfo = ufgConfig.getBrowsersInfo();
            browsersInfo.forEach(configuration::addBrowser);
        }
    }

    private RectangleSize getBrowserViewPortSize(String driverType, WebDriver innerDriver) {
        RectangleSize providedBrowserViewPortSizeFromConfig = (RectangleSize) getValueFromConfig(
                APPLITOOLS.RECTANGLE_SIZE);
        int providedBrowserViewPortSizeFromConfigHeight =
                providedBrowserViewPortSizeFromConfig.getHeight();
        int providedBrowserViewPortSizeFromConfigWidth =
                providedBrowserViewPortSizeFromConfig.getWidth();
        LOGGER.info(String.format("Provided browser dimensions: %s",
                                  providedBrowserViewPortSizeFromConfig));

        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            return providedBrowserViewPortSizeFromConfig;
        } else {
            JavascriptExecutor js = (JavascriptExecutor) innerDriver;
            if (Runner.getPlatform().equals(Platform.electron)) {
                Set<String> windowHandles = innerDriver.getWindowHandles();
                if (windowHandles.size() > 0) {
                    innerDriver.switchTo().window((String) windowHandles.toArray()[0]);
                }
            } else {
                Dimension actualBrowserSize = innerDriver.manage().window().getSize();
                LOGGER.info(String.format("Actual browser dimensions: %s", actualBrowserSize));
            }
            Long actualHeight = (Long) js.executeScript("return (window.innerHeight);");
            Long actualWidth = (Long) js.executeScript("return (window.innerWidth);");

            if(providedBrowserViewPortSizeFromConfigHeight > actualHeight.intValue() || providedBrowserViewPortSizeFromConfigWidth > actualWidth.intValue()) {
                return new RectangleSize(actualWidth.intValue(), actualHeight.intValue());
            } else {
                return providedBrowserViewPortSizeFromConfig;
            }
        }
    }

    private int convertValueFromConfigToInt(Object valueFromConfig) {
        try {
            return Integer.parseInt(String.valueOf(valueFromConfig));
        } catch(NumberFormatException e) {
            return (int) (Double.parseDouble(String.valueOf(valueFromConfig)));
        }
    }

    @NotNull
    private Configuration defaultApplitoolsUFGConfig(Configuration ufgConfig) {
        String applitoolsUFGConfigMessage = "Using browser & device configuration provided for " +
                                            "Applitools Ultrafast Grid";
        if(null == ufgConfig) {
            applitoolsUFGConfigMessage = "Using default browser & device configuration for " +
                                         "Applitools Ultrafast Grid: ";
            ufgConfig = new Configuration();
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
        }
        LOGGER.info(applitoolsUFGConfigMessage);
        ReportPortalLogger.logDebugMessage(applitoolsUFGConfigMessage + ufgConfig);
        return ufgConfig;
    }

    public Visual checkWindow(String fromScreen, String tag) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info(String.format("checkWindow: fromScreen: %s, tag: %s", fromScreen,
                                  formattedTagName));
        LOGGER.info(String.format("checkWindow: eyesOnWeb.getIsDisabled(): %s",
                                  eyesOnWeb.getIsDisabled()));
        LOGGER.info(String.format("checkWindow: eyesOnApp.getIsDisabled(): %s",
                                  eyesOnApp.getIsDisabled()));

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.checkWindow(formattedTagName);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(WEB_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      webDuration.getSeconds()));
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.checkWindow(formattedTagName);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(APP_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      appDuration.getSeconds()));
        }

        screenShotManager.takeScreenShot(innerDriver, formattedTagName);
        return this;

    }

    @NotNull
    private String getFormattedTagName(String fromScreen, String tag) {
        return fromScreen + " : " + tag;
    }

    public Visual check(String fromScreen, String tag, SeleniumCheckSettings checkSettings) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info(String.format("check: fromScreen: %s, tag: %s", fromScreen, formattedTagName));
        LOGGER.info(
                String.format("check: eyesOnWeb.getIsDisabled(): %s", eyesOnWeb.getIsDisabled()));
        LOGGER.info(
                String.format("check: eyesOnApp.getIsDisabled(): %s", eyesOnApp.getIsDisabled()));

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(formattedTagName, checkSettings);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(WEB_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      webDuration.getSeconds()));
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(APP_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      appDuration.getSeconds()));
        }

        screenShotManager.takeScreenShot(innerDriver, formattedTagName);
        return this;
    }

    public Visual check(String fromScreen, String tag, AppiumCheckSettings checkSettings) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info(String.format("check: fromScreen: %s, tag: %s", fromScreen, formattedTagName));
        LOGGER.info(
                String.format("check: eyesOnWeb.getIsDisabled(): %s", eyesOnWeb.getIsDisabled()));
        LOGGER.info(
                String.format("check: eyesOnApp.getIsDisabled(): %s", eyesOnApp.getIsDisabled()));

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(formattedTagName, checkSettings);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(WEB_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      webDuration.getSeconds()));
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(APP_CHECK_WINDOW_TIME_TAKEN, fromScreen, tag,
                                      appDuration.getSeconds()));
        }

        screenShotManager.takeScreenShot(innerDriver, formattedTagName);
        return this;
    }

    public Visual checkWindow(String fromScreen, String tag, MatchLevel level) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info(
                String.format("checkWindow: fromScreen: %s, MatchLevel: %s, tag: %s", fromScreen,
                              level, formattedTagName));
        LOGGER.info(String.format("checkWindow: eyesOnWeb.getIsDisabled(): %s",
                                  eyesOnWeb.getIsDisabled()));
        LOGGER.info(String.format("checkWindow: eyesOnApp.getIsDisabled(): %s",
                                  eyesOnApp.getIsDisabled()));


        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(
                    "%s:%s:: Web: checkWindow with MatchLevel: %s: Time taken: %d sec", fromScreen,
                    tag, level.name(), webDuration.getSeconds()));
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(String.format(
                    "%s:%s:: App: checkWindow with MatchLevel: %s: Time taken: %d sec", fromScreen,
                    tag, level.name(), appDuration.getSeconds()));
        }

        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    public void handleTestResults(String userPersona, String driverType) {
        switch(driverType) {
            case Driver.WEB_DRIVER:
                getVisualResultsFromWeb(userPersona);
                break;

            case Driver.APPIUM_DRIVER:
                getVisualResultsFromApp(userPersona);
                break;

            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected driver type: '%s'", driverType));
        }
    }

    public Visual takeScreenshot(String fromScreen, String tag) {
        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    private void getVisualResultsFromWeb(String userPersona) {
        if(Boolean.TRUE.equals(eyesOnWeb.getIsDisabled())) {
            return;
        }
        LOGGER.info(String.format("getVisualResultsFromWeb: user: %s", userPersona));
        eyesOnWeb.closeAsync();
        TestResultsSummary allTestResults = seleniumEyesRunner.getAllTestResults(false);
        checkVisualTestResults(allTestResults, userPersona, "web", applitoolsLogFileNameForWeb);
    }

    private void getVisualResultsFromApp(String userPersona) {
        if(Boolean.TRUE.equals(eyesOnApp.getIsDisabled())) {
            return;
        }
        LOGGER.info(String.format("getVisualResultsFromApp: user: %s", userPersona));
        TestResults allTestResults = eyesOnApp.close(false);
        checkEachTestVisualResults(userPersona, "app", null, allTestResults);
    }

    private void checkVisualTestResults(TestResultsSummary allTestResults, String userPersona,
                                        String onPlatform, String applitoolsLogFileName) {
        if(null != allTestResults) {
            for(TestResultContainer allTestResult : allTestResults) {
                TestResults result = allTestResult.getTestResults();
                checkEachTestVisualResults(userPersona, onPlatform, allTestResult.getBrowserInfo(),
                                           result);
            }
            LOGGER.info(String.format("Applitools logs available here: %s", applitoolsLogFileName));
        }
    }

    private void checkEachTestVisualResults(String userPersona, String onPlatform,
                                            RenderBrowserInfo browserInfo, TestResults result) {
        HashMap<String, Object> resultMap = parseVisualTestResults(browserInfo, result);

        logVisualTestResults(userPersona, onPlatform, resultMap);

        boolean areVisualDifferenceFound = result.getStatus()
                                                 .equals(TestResultsStatus.Unresolved) || result.getStatus()
                                                                                                .equals(TestResultsStatus.Failed);
        LOGGER.info(String.format(VISUAL_TESTING_DIFFERENCES_FOUND, areVisualDifferenceFound));
        softlyFailTestIfDifferencesFound(userPersona, onPlatform, result, areVisualDifferenceFound);
    }

    private void softlyFailTestIfDifferencesFound(String userPersona, String onPlatform, TestResults result, boolean areVisualDifferenceFound) {
        if(areVisualDifferenceFound) {
            ReportPortalLogger.logWarningMessage(
                    String.format(VISUAL_TESTING_DIFFERENCES_FOUND, areVisualDifferenceFound));
            if (Runner.shouldFailTestOnVisualDifference()) {
                softly.assertThat(areVisualDifferenceFound).as(String.format(
                        "Visual differences for user persona: '%s' on '%s' found in test: '%s'. See " +
                                "results here: ",
                        userPersona, onPlatform, context.getTestName()) + result.getUrl()).isFalse();
            } else {
                ReportPortalLogger.logInfoMessage("Not failing the tests because FAIL_TEST_ON_VISUAL_DIFFERENCE=false");
            }
        } else {
            ReportPortalLogger.logInfoMessage(
                    String.format(VISUAL_TESTING_DIFFERENCES_FOUND, areVisualDifferenceFound));
        }
    }

    private void logVisualTestResults(String userPersona, String onPlatform, HashMap<String, Object> resultMap) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap);
        } catch(JsonProcessingException e) {
            LOGGER.error(
                    String.format("ERROR parsing Applitools results as a map%n%s", e.getMessage()));
        }
        String message = String.format(
                "'%s' Visual Testing Results for user persona: '%s' :: Test: '%s'%n'%s'",
                onPlatform, userPersona, context.getTestName(), json);
        LOGGER.info(message);
        ReportPortalLogger.logDebugMessage(message);
    }

    @NotNull
    private static HashMap<String, Object> parseVisualTestResults(RenderBrowserInfo browserInfo, TestResults result) {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("Number of steps", result.getSteps());
        resultMap.put("Number of matches", result.getMatches());
        resultMap.put("Number of mismatches", result.getMismatches());
        resultMap.put("Number of missing", result.getMissing());
        resultMap.put("Number of strict matches", result.getStrictMatches());
        resultMap.put("Number of content matches", result.getContentMatches());
        resultMap.put("Number of layout matches", result.getLayoutMatches());
        resultMap.put("Number of no matches", result.getNoneMatches());
        resultMap.put("Result url", result.getUrl());
        resultMap.put("Status", result.getStatus());
        resultMap.put("Duration", result.getDuration());
        resultMap.put("Accessibility status", result.getAccessibilityStatus());
        resultMap.put("Is passed?", result.isPassed());
        resultMap.put("Is aborted?", result.isAborted());
        resultMap.put("Is new?", result.isNew());
        resultMap.put("Is difference?", result.isDifferent());
        if(null != browserInfo) {
            resultMap.put("Browser/Device info", browserInfo.toString());
        }
        return resultMap;
    }
}
