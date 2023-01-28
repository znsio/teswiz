package com.znsio.e2e.tools;

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
import com.epam.reportportal.service.ReportPortal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.znsio.e2e.runner.Runner.*;
import static com.znsio.e2e.runner.Setup.*;

public class Visual {
    private static final Logger LOGGER = Logger.getLogger(Visual.class.getName());
    private static final String DEFAULT_APPLITOOLS_SERVER_URL = "https://eyesapi.applitools.com";
    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
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
    private final String userPersona;
    private String applitoolsLogFileNameForWeb = NOT_SET;
    private String applitoolsLogFileNameForApp = NOT_SET;
    private EyesRunner seleniumEyesRunner;
    // private ClassicRunner appiumEyesRunner;

    public Visual(String driverType, Platform platform, WebDriver innerDriver, String testName, String userPersona, String appName, boolean isVisualTestingEnabled) {
        LOGGER.info(
                "Visual constructor: Driver type: " + driverType + ", platform: " + platform.name() + ", testName: " + testName + ", isVisualTestingEnabled:  " + isVisualTestingEnabled);
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                                    .getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        this.applitoolsConfig = Runner.getApplitoolsConfiguration();
        this.isEnableBenchmarkPerValidation = Boolean.parseBoolean(String.valueOf(this.applitoolsConfig.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
        this.innerDriver = innerDriver;
        this.isVerboseLoggingEnabled = getValueFromConfig(APPLITOOLS.SHOW_LOGS, true);
        this.userPersona = userPersona;
        appName = appName.equalsIgnoreCase(DEFAULT) ? (String) this.applitoolsConfig.get(APPLITOOLS.APP_NAME) : appName;
        eyesOnApp = instantiateAppiumEyes(driverType, platform, innerDriver, appName, testName, isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, platform, innerDriver, appName, testName, isVisualTestingEnabled);
    }

    private boolean getValueFromConfig(String key, boolean defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : Boolean.parseBoolean(String.valueOf(applitoolsConfig.get(key)));
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes(String driverType, Platform platform, WebDriver innerDriver, String appName, String testName,
                                                                  boolean isVisualTestingEnabled) {
        if(driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        LOGGER.info("instantiateAppiumEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        // appiumEyesRunner = new ClassicRunner();
        // appiumEyesRunner.setDontCloseBatches(true);
        com.applitools.eyes.appium.Eyes appEyes = new com.applitools.eyes.appium.Eyes();

        appEyes.setServerUrl(getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        appEyes.setApiKey(getApplitoolsAPIKey(isVisualTestingEnabled));
        appEyes.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        appEyes.setBranchName(String.valueOf(getValueFromConfig(BRANCH_NAME)));
        appEyes.setEnvName(targetEnvironment);
        appEyes.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));
        appEyes.setIsDisabled(!isVisualTestingEnabled);

        applitoolsLogFileNameForApp = getApplitoolsLogFileNameFor("app");
        appEyes.setLogHandler(new FileLogger(applitoolsLogFileNameForApp, true, isVerboseLoggingEnabled));

        // todo - enhancements - https://applitools.com/docs/topics/general-concepts/visual-test-best-practices.html?Highlight=setMatchTimeout
        // add setIgnoreCaret, setHideScrollbars, setIgnoreDisplacements
        appEyes.addProperty(APP_NAME, appName);
        appEyes.addProperty("USER_PERSONA", userPersona);
        appEyes.addProperty(BRANCH_NAME, String.valueOf(getValueFromConfig(BRANCH_NAME)));
        appEyes.addProperty(PLATFORM, platform.name());
        appEyes.addProperty(RUN_IN_CI, String.valueOf(getValueFromConfig(RUN_IN_CI)));
        appEyes.addProperty(TARGET_ENVIRONMENT, String.valueOf(getValueFromConfig(TARGET_ENVIRONMENT)));
        appEyes.addProperty("USER_NAME", USER_NAME);

        try {
            appEyes.open(innerDriver, appName + "-" + platform, testName);
            LOGGER.info("instantiateAppiumEyes: Is Applitools Visual Testing enabled? - " + !appEyes.getIsDisabled());
        } catch(IllegalArgumentException e) {
            throw new InvalidTestDataException(String.format("Exception in instantiating Applitools for Apps: '%s;", e.getMessage(), e));
        }

        return appEyes;
    }

    private String getApplitoolsAPIKey(boolean isVisualTestingEnabled) {
        return isVisualTestingEnabled? getValueFromConfig(APPLITOOLS.API_KEY, null) : getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET);
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes(String driverType, Platform platform, WebDriver innerDriver, String appName, String testName,
                                                                 boolean isVisualTestingEnabled) {
        if(driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        LOGGER.info("instantiateWebEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        boolean isUFG = getValueFromConfig(APPLITOOLS.USE_UFG, false);

        int ufgConcurrency = getValueFromConfig(APPLITOOLS.CONCURRENCY, DEFAULT_UFG_CONCURRENCY);
        seleniumEyesRunner = isUFG ? new VisualGridRunner(ufgConcurrency) : new ClassicRunner();
        seleniumEyesRunner.setDontCloseBatches(true);

        com.applitools.eyes.selenium.Eyes webEyes = new com.applitools.eyes.selenium.Eyes(seleniumEyesRunner);
        Configuration configuration = webEyes.getConfiguration();
        configuration.setServerUrl(getValueFromConfig(APPLITOOLS.SERVER_URL, DEFAULT_APPLITOOLS_SERVER_URL));
        configuration.setApiKey(getValueFromConfig(APPLITOOLS.API_KEY, NOT_SET));
        configuration.setApiKey(getApplitoolsAPIKey(isVisualTestingEnabled));
        configuration.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));

        configuration.setBranchName(String.valueOf(getValueFromConfig(BRANCH_NAME)));
        configuration.setEnvironmentName(targetEnvironment);
        configuration.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));

        configuration.setDisableBrowserFetching(getValueFromConfig(APPLITOOLS.DISABLE_BROWSER_FETCHING, true));
        configuration.setSendDom(getValueFromConfig(APPLITOOLS.SEND_DOM, true));
        configuration.setStitchMode(StitchMode.valueOf(String.valueOf(getValueFromConfig(APPLITOOLS.STITCH_MODE, StitchMode.CSS))
                                                             .toUpperCase()));
        configuration.setForceFullPageScreenshot(getValueFromConfig(APPLITOOLS.TAKE_FULL_PAGE_SCREENSHOT, true));

        addBrowserAndDeviceConfigForUFG(isUFG, configuration);

        webEyes.setConfiguration(configuration);

        applitoolsLogFileNameForWeb = getApplitoolsLogFileNameFor("web");
        webEyes.setIsDisabled(!isVisualTestingEnabled);
        webEyes.setLogHandler(new FileLogger(applitoolsLogFileNameForWeb, true, isVerboseLoggingEnabled));

        webEyes.addProperty(APP_NAME, appName);
        webEyes.addProperty("USER_PERSONA", userPersona);
        webEyes.addProperty(BRANCH_NAME, String.valueOf(getValueFromConfig(BRANCH_NAME)));
        webEyes.addProperty(PLATFORM, platform.name());
        webEyes.addProperty(RUN_IN_CI, String.valueOf(getValueFromConfig(RUN_IN_CI)));
        webEyes.addProperty(TARGET_ENVIRONMENT, String.valueOf(getValueFromConfig(TARGET_ENVIRONMENT)));
        webEyes.addProperty("USER_NAME", USER_NAME);

        RectangleSize setBrowserViewPortSize = getBrowserViewPortSize(driverType, innerDriver);
        LOGGER.info("Using browser dimensions for Applitools: " + setBrowserViewPortSize);

        try {
            webEyes.open(innerDriver, appName + "-" + platform, testName, setBrowserViewPortSize);
            LOGGER.info("instantiateWebEyes:  Is Applitools Visual Testing enabled? - " + !webEyes.getIsDisabled());
        } catch(IllegalArgumentException e) {
            throw new InvalidTestDataException(String.format("Exception in instantiating Applitools for Web: '%s;", e.getMessage(), e));
        }
        return webEyes;
    }

    private String getValueFromConfig(String key, String defaultValue) {
        return (null == applitoolsConfig.get(key) || applitoolsConfig.get(key).equals("null")) ? defaultValue : String.valueOf(applitoolsConfig.get(key));
    }

    private Object getValueFromConfig(String key) {
        return applitoolsConfig.get(key);
    }

    private Object getValueFromConfig(String key, Object defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : applitoolsConfig.get(key);
    }

    @NotNull
    private String getApplitoolsLogFileNameFor(String appType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        return scenarioLogDir + File.separator + "deviceLogs" + File.separator + "applitools-" + appType + ".log";
    }

    private int getValueFromConfig(String key, int defaultValue) {
        Object valueFromConfig = applitoolsConfig.get(key);
        return (null == valueFromConfig) ? defaultValue : convertValueFromConfigToInt(valueFromConfig);
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
        RectangleSize providedBrowserViewPortSizeFromConfig = (RectangleSize) getValueFromConfig(APPLITOOLS.RECTANGLE_SIZE);
        int providedBrowserViewPortSizeFromConfigHeight = providedBrowserViewPortSizeFromConfig.getHeight();
        int providedBrowserViewPortSizeFromConfigWidth = providedBrowserViewPortSizeFromConfig.getWidth();
        LOGGER.info("Provided browser dimensions: " + providedBrowserViewPortSizeFromConfig);

        if(driverType.equals(Driver.APPIUM_DRIVER)) {
            return providedBrowserViewPortSizeFromConfig;
        } else {
            JavascriptExecutor js = (JavascriptExecutor) innerDriver;
            Dimension actualBrowserSize = innerDriver.manage()
                                                     .window()
                                                     .getSize();
            LOGGER.info("Actual browser dimensions: " + actualBrowserSize);
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
            return (int) ((Double.parseDouble(String.valueOf(valueFromConfig))));
        }
    }

    @NotNull
    private Configuration defaultApplitoolsUFGConfig(Configuration ufgConfig) {
        String applitoolsUFGConfigMessage = "Using browser & device configuration provided for Applitools Ultrafast Grid";
        if(null == ufgConfig) {
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

    public Visual checkWindow(String fromScreen, String tag) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info("checkWindow: fromScreen: " + fromScreen + ", tag: " + formattedTagName);
        LOGGER.info("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        LOGGER.info("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.checkWindow(formattedTagName);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: Web: checkWindow: Time taken: " + webDuration.getSeconds() + " sec ");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.checkWindow(formattedTagName);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
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
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: Web: checkWindow: Time taken: " + webDuration.getSeconds() + " sec ");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: App: checkWindow: Time taken: " + appDuration.getSeconds() + " sec ");
        }

        screenShotManager.takeScreenShot(innerDriver, formattedTagName);
        return this;
    }

    public Visual check(String fromScreen, String tag, AppiumCheckSettings checkSettings) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        LOGGER.info("check: fromScreen: " + fromScreen + ", tag: " + formattedTagName);
        LOGGER.info("check: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        LOGGER.info("check: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(formattedTagName, checkSettings);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + " :" + tag + ":: Web: checkWindow: Time taken: " + webDuration.getSeconds() + " sec ");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
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
        eyesOnWeb.check(getFormattedTagName(fromScreen, tag), Target.window()
                                                                    .matchLevel(level));
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + ":" + tag + ":: Web: checkWindow with MatchLevel: " + level.name() + ": Time taken: " + webDuration.getSeconds() + " sec");
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(getFormattedTagName(fromScreen, tag), Target.window()
                                                                    .matchLevel(level));
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if(isEnableBenchmarkPerValidation) {
            LOGGER.info(fromScreen + ":" + tag + ":: App: checkWindow with MatchLevel: " + level.name() + ": Time taken: " + appDuration.getSeconds() + " sec");
        }

        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    public void handleTestResults(String userPersona, String driverType) {
        switch(driverType) {
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

    public Visual takeScreenshot(String fromScreen, String tag) {
        screenShotManager.takeScreenShot(innerDriver, getFormattedTagName(fromScreen, tag));
        return this;
    }

    private void getVisualResultsFromWeb(String userPersona) {
        if(eyesOnWeb.getIsDisabled()) {
            return;
        }
        LOGGER.info("getVisualResultsFromWeb: user: " + userPersona);
        eyesOnWeb.closeAsync();
        TestResultsSummary allTestResults = seleniumEyesRunner.getAllTestResults(false);
        checkVisualTestResults(allTestResults, userPersona, "web", applitoolsLogFileNameForWeb);
    }

    private void checkVisualTestResults(TestResultsSummary allTestResults, String userPersona, String onPlatform, String applitoolsLogFileName) {
        if(null != allTestResults) {
            for(TestResultContainer allTestResult : allTestResults) {
                TestResults result = allTestResult.getTestResults();
                checkEachTestVisualResults(userPersona, onPlatform, allTestResult.getBrowserInfo(), result);
            }
            LOGGER.info("Applitools logs available here: " + applitoolsLogFileName);
        }
    }

    private void checkEachTestVisualResults(String userPersona, String onPlatform, RenderBrowserInfo browserInfo, TestResults result) {
        HashMap resultMap = new HashMap();
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

        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writerWithDefaultPrettyPrinter()
                         .writeValueAsString(resultMap);
        } catch(JsonProcessingException e) {
            LOGGER.error("ERROR parsing Applitools results as a map\n" + e.getMessage());
        }
        String message = String.format("'%s' Visual Testing Results for user persona: '%s' :: Test: '%s'\n'%s'", onPlatform, userPersona, context.getTestName(), json);
        LOGGER.info(message);
        ReportPortal.emitLog(message, INFO, new Date());

        boolean areVisualDifferenceFound = result.getStatus()
                                                 .equals(TestResultsStatus.Unresolved) || result.getStatus()
                                                                                                .equals(TestResultsStatus.Failed);
        LOGGER.info("Visual testing differences found? - " + areVisualDifferenceFound);
        if (areVisualDifferenceFound) {
            ReportPortal.emitLog("Visual testing differences found? - " + areVisualDifferenceFound, WARN, new Date());
        } else {
            ReportPortal.emitLog("Visual testing differences found? - " + areVisualDifferenceFound, INFO, new Date());
        }
        long threadId = Thread.currentThread()
                              .getId();
        SoftAssertions softly = Runner.getSoftAssertion(threadId);
        softly.assertThat(areVisualDifferenceFound)
              .as(String.format("Visual differences for user persona: '%s' on '%s' found in test: '%s'. See results here: ", userPersona, onPlatform,
                                context.getTestName()) + result.getUrl())
              .isFalse();
    }

    private void getVisualResultsFromApp(String userPersona) {
        if(eyesOnApp.getIsDisabled()) {
            return;
        }
        LOGGER.info("getVisualResultsFromApp: user: " + userPersona);
        TestResults allTestResults = eyesOnApp.close(false);
        checkEachTestVisualResults(userPersona, "app", null, allTestResults);
        // TestResultsSummary allTestResults = appiumEyesRunner.getAllTestResults(false);
        // checkVisualTestResults(allTestResults, userPersona, "app", applitoolsLogFileNameForApp);
    }
}
