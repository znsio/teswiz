package com.znsio.e2e.tools;

import com.applitools.eyes.*;
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
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.APPLITOOLS;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.runner.Runner;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.znsio.e2e.runner.Runner.NOT_SET;
import static com.znsio.e2e.runner.Runner.USER_NAME;
import static com.znsio.e2e.runner.Setup.*;

public class Visual {
    private static final Logger LOGGER = Logger.getLogger(Visual.class.getName());
    private static final String DEFAULT_APPLITOOLS_SERVER_URL = "https://eyesapi.applitools.com";
    private static final String DEBUG = "DEBUG";
    private final String visualTestNotEnabledMessage = "Visual Test is not enabled";
    private final com.applitools.eyes.selenium.Eyes eyesOnWeb;
    private final com.applitools.eyes.appium.Eyes eyesOnApp;
    private final TestExecutionContext context;
    private final ScreenShotManager screenShotManager;
    private final String targetEnvironment = Runner.getTargetEnvironment();
    private final Map applitoolsConfig;
    private final boolean isEnableBenchmarkPerValidation;
    private final boolean isVerboseLoggingEnabled;
    private final WebDriver innerDriver;
    private final int DEFAULT_UFG_CONCURRENCY = 5;
    private String applitoolsLogFileNameForWeb = NOT_SET;
    private String applitoolsLogFileNameForApp = NOT_SET;

    public Visual(String driverType, WebDriver innerDriver, String testName, boolean isVisualTestingEnabled) {
        LOGGER.info("Visual constructor: Driver type: " + driverType + ", testName: " + testName + ", isVisualTestingEnabled:  " + isVisualTestingEnabled);
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        this.applitoolsConfig = Runner.initialiseApplitoolsConfiguration();
        this.isEnableBenchmarkPerValidation = Boolean.parseBoolean(String.valueOf(this.applitoolsConfig.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
        this.innerDriver = innerDriver;
        this.isVerboseLoggingEnabled = getValueFromConfig(APPLITOOLS.SHOW_LOGS, true);
        String appName = this.applitoolsConfig.get(APPLITOOLS.APP_NAME) + "-";
        eyesOnApp = instantiateAppiumEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes(String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        appName += Platform.android;
        LOGGER.info("instantiateAppiumEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        com.applitools.eyes.appium.Eyes eyes = new com.applitools.eyes.appium.Eyes();

        eyes.setServerUrl(getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        eyes.setApiKey(getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET));
        eyes.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        eyes.setEnvName(targetEnvironment);
        eyes.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));
        eyes.setIsDisabled(!isVisualTestingEnabled);

        applitoolsLogFileNameForApp = getApplitoolsLogFileNameFor("app");
        eyes.setLogHandler(new FileLogger(applitoolsLogFileNameForApp, true, isVerboseLoggingEnabled));

        eyes.addProperty(BRANCH_NAME, String.valueOf(getValueFromConfig(BRANCH_NAME)));
        eyes.addProperty(PLATFORM, String.valueOf(getValueFromConfig(PLATFORM)));
        eyes.addProperty(RUN_IN_CI, String.valueOf(getValueFromConfig(RUN_IN_CI)));
        eyes.addProperty(TARGET_ENVIRONMENT, String.valueOf(getValueFromConfig(TARGET_ENVIRONMENT)));

        if (isVisualTestingEnabled) {
            eyes.open(innerDriver, appName, testName);
        }
        LOGGER.info("instantiateAppiumEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes(String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        appName += Platform.web;
        LOGGER.info("instantiateWebEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        boolean isUFG = getValueFromConfig(APPLITOOLS.USE_UFG, false);

        int ufgConcurrency = getValueFromConfig(APPLITOOLS.CONCURRENCY, DEFAULT_UFG_CONCURRENCY);
        EyesRunner runner = isUFG ? new VisualGridRunner(ufgConcurrency) : new ClassicRunner();
        context.addTestState(TEST_CONTEXT.EYES_RUNNER, runner);

        com.applitools.eyes.selenium.Eyes eyes = new com.applitools.eyes.selenium.Eyes(runner);
        Configuration configuration = eyes.getConfiguration();
        configuration.setServerUrl(getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        configuration.setApiKey(getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET));
        configuration.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        configuration.setEnvironmentName(targetEnvironment);
        configuration.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));

        configuration.setSendDom(getValueFromConfig(APPLITOOLS.SEND_DOM, true));
        configuration.setStitchMode(StitchMode.valueOf(String.valueOf(getValueFromConfig(APPLITOOLS.STITCH_MODE, StitchMode.CSS)).toUpperCase()));
        configuration.setForceFullPageScreenshot(getValueFromConfig(APPLITOOLS.TAKE_FULL_PAGE_SCREENSHOT, true));

        addBrowserAndDeviceConfigForUFG(isUFG, configuration);

        eyes.setConfiguration(configuration);

        applitoolsLogFileNameForWeb = getApplitoolsLogFileNameFor("web");
        eyes.setIsDisabled(!isVisualTestingEnabled);
        eyes.setLogHandler(new FileLogger(applitoolsLogFileNameForWeb, true, isVerboseLoggingEnabled));

        eyes.addProperty(BRANCH_NAME, String.valueOf(getValueFromConfig(BRANCH_NAME)));
        eyes.addProperty(PLATFORM, String.valueOf(getValueFromConfig(PLATFORM)));
        eyes.addProperty(RUN_IN_CI, String.valueOf(getValueFromConfig(RUN_IN_CI)));
        eyes.addProperty(TARGET_ENVIRONMENT, String.valueOf(getValueFromConfig(TARGET_ENVIRONMENT)));
        eyes.addProperty("USER_NAME", USER_NAME);

        RectangleSize setBrowserViewPortSize = getBrowserViewPortSize(driverType, innerDriver);
        LOGGER.info("Using browser dimensions for Applitools: " + setBrowserViewPortSize);

        eyes.open(innerDriver, appName, testName, setBrowserViewPortSize);
        LOGGER.info("instantiateWebEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    private RectangleSize getBrowserViewPortSize(String driverType, WebDriver innerDriver) {
        RectangleSize providedBrowserViewPortSizeFromConfig = (RectangleSize) getValueFromConfig(APPLITOOLS.RECTANGLE_SIZE);
        int providedBrowserViewPortSizeFromConfigHeight = providedBrowserViewPortSizeFromConfig.getHeight();
        int providedBrowserViewPortSizeFromConfigWidth = providedBrowserViewPortSizeFromConfig.getWidth();
        LOGGER.info("Provided browser dimensions: " + providedBrowserViewPortSizeFromConfig);

        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            return providedBrowserViewPortSizeFromConfig;
        } else {
            JavascriptExecutor js = (JavascriptExecutor) innerDriver;
            Dimension actualBrowserSize = innerDriver.manage().window().getSize();
            LOGGER.info("Actual browser dimensions: " + actualBrowserSize);
            Long actualHeight = (Long) js.executeScript("return (window.innerHeight);");
            Long actualWidth = (Long) js.executeScript("return (window.innerWidth);");

            if (providedBrowserViewPortSizeFromConfigHeight > actualHeight.intValue() || providedBrowserViewPortSizeFromConfigWidth > actualWidth.intValue()) {
                return new RectangleSize(actualWidth.intValue(), actualHeight.intValue());
            } else {
                return providedBrowserViewPortSizeFromConfig;
            }
        }
    }

    private void addBrowserAndDeviceConfigForUFG(boolean isUFG, Configuration configuration) {
        if (isUFG) {
            Configuration ufgConfig = (Configuration) context.getTestState(APPLITOOLS.UFG_CONFIG);
            ufgConfig = defaultApplitoolsUFGConfig(ufgConfig);
            List<RenderBrowserInfo> browsersInfo = ufgConfig.getBrowsersInfo();
            browsersInfo.forEach(configuration::addBrowser);
        }
    }

    @NotNull
    private Configuration defaultApplitoolsUFGConfig(Configuration ufgConfig) {
        String applitoolsUFGConfigMessage = "Using browser & device configuration provided for Applitools Ultrafast Grid";
        if (null == ufgConfig) {
            applitoolsUFGConfigMessage = "Using default browser & device configuration for Applitools Ultrafast Grid: ";
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
        ReportPortal.emitLog(applitoolsUFGConfigMessage + ufgConfig, DEBUG, new Date());
        return ufgConfig;
    }

    private int getValueFromConfig(String key, int defaultValue) {
        Object valueFromConfig = applitoolsConfig.get(key);
        return (null == valueFromConfig) ? defaultValue : convertValueFromConfigToInt(valueFromConfig);
    }

    private int convertValueFromConfigToInt(Object valueFromConfig) {
        try {
            return Integer.parseInt(String.valueOf(valueFromConfig));
        } catch (NumberFormatException e) {
            return (int) ((Double.parseDouble(String.valueOf(valueFromConfig))));
        }
    }

    private boolean getValueFromConfig(String key, boolean defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : Boolean.parseBoolean(String.valueOf(applitoolsConfig.get(key)));
    }

    private String getValueFromConfig(String key, String defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : String.valueOf(applitoolsConfig.get(key));
    }

    private Object getValueFromConfig(String key, Object defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : applitoolsConfig.get(key);
    }

    private Object getValueFromConfig(String key) {
        return applitoolsConfig.get(key);
    }

    @NotNull
    private String getApplitoolsLogFileNameFor(String appType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        return scenarioLogDir + File.separator + "deviceLogs" + File.separator + "applitools-" + appType + ".log";
    }

    public Visual checkWindow(String fromScreen, String tag) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info("checkWindow: fromScreen: " + fromScreen + ", tag: " + formattedTagName);
        LOGGER.info("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        LOGGER.info("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.checkWindow(formattedTagName);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: Web: checkWindow: Time taken: " + webDuration.getSeconds() + " sec ");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.checkWindow(formattedTagName);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: App: checkWindow: Time taken: " + appDuration.getSeconds() + " sec ");
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
        LOGGER.info("check: fromScreen: " + fromScreen + ", tag: " + formattedTagName);
        LOGGER.info("check: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        LOGGER.info("check: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(formattedTagName, checkSettings);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: Web: checkWindow: Time taken: " + webDuration.getSeconds() + " sec ");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: App: checkWindow: Time taken: " + appDuration.getSeconds() + " sec ");
        }

        screenShotManager.takeScreenShot(innerDriver, formattedTagName);
        return this;
    }

    public Visual checkWindow(String fromScreen, String tag, MatchLevel level) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info("checkWindow: fromScreen: " + fromScreen + ", MatchLevel: " + level + ", tag: " + formattedTagName);
        LOGGER.info("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        LOGGER.info("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());


        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + ":" + tag + ":: Web: checkWindow with MatchLevel: " + level.name() + ": Time taken: " + webDuration.getSeconds() + " sec");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + ":" + tag + ":: App: checkWindow with MatchLevel: " + level.name() + ": Time taken: " + appDuration.getSeconds() + " sec");
        }

        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    public Visual takeScreenshot(String fromScreen, String tag) {
        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    public void handleTestResults(String userPersona, String driverType) {
        switch (driverType) {
            case Driver.WEB_DRIVER:
                takeScreenshot(userPersona, "afterHooks");
                getVisualResultsFromWeb(userPersona);
                break;

            case Driver.APPIUM_DRIVER:
                takeScreenshot(userPersona, "afterHooks");
                getVisualResultsFromApp(userPersona);
                break;

            default:
                throw new InvalidTestDataException(String.format("Unexpected driver type: '%s'", driverType));
        }
    }

    private void getVisualResultsFromWeb(String userPersona) {
        if (eyesOnWeb.getIsDisabled()) {
            return;
        }
        LOGGER.info("getVisualResultsFromWeb: user: " + userPersona);
        TestResults visualResults = eyesOnWeb.close(false);
        if (null != visualResults) {
            String reportUrl = handleTestResults(userPersona, "web", visualResults);
            String message = String.format("Web Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
            LOGGER.info(message);
            LOGGER.info("Applitools logs available here: " + applitoolsLogFileNameForWeb);
            ReportPortal.emitLog(message, DEBUG, new Date(), new File(applitoolsLogFileNameForWeb));
        }
    }

    private void getVisualResultsFromApp(String userPersona) {
        if (eyesOnApp.getIsDisabled()) {
            return;
        }
        LOGGER.info("getVisualResultsFromApp: user: " + userPersona);
        TestResults visualResults = eyesOnApp.close(false);
        String reportUrl = handleTestResults(userPersona, "app", visualResults);
        String message = String.format("App Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        LOGGER.info(message);
        LOGGER.info("Applitools logs available here: " + applitoolsLogFileNameForApp);
        ReportPortal.emitLog(message, DEBUG, new Date(), new File(applitoolsLogFileNameForApp));
    }

    private String handleTestResults(String userPersona, String onPlatform, TestResults result) {
        String message = String.format("Visual Testing Results for user persona: '%s' onPlatform '%s'\n", userPersona, onPlatform);
        message += "\n\t\t" + result
                           + "\n\t\tmatched = " + result.getMatches()
                           + "\n\t\tmismatched = " + result.getMismatches()
                           + "\n\t\tmissing = " + result.getMissing()
                           + "\n\t\tisNew: " + result.isNew()
                           + "\n\t\tisPassed: " + result.isPassed()
                           + "\n\t\tResults url: " + result.getUrl();
        LOGGER.info(message);
        ReportPortal.emitLog(message, DEBUG, new Date());
        boolean hasMismatches = result.getMismatches() != 0;
        LOGGER.info("Visual testing differences found? - " + hasMismatches);
        long threadId = Thread.currentThread().getId();
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        softly.assertThat(hasMismatches).as(String.format("Visual differences for user persona: '%s' on '%s' found in test: '%s'. See results here: '%s'", userPersona, onPlatform, context.getTestName(), result.getUrl())).isFalse();
        return result.getUrl();
    }
}
