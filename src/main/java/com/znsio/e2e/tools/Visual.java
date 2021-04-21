package com.znsio.e2e.tools;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.APPLITOOLS;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class Visual {
    private static final Logger LOGGER = Logger.getLogger(Visual.class.getName());
    private final String visualTestNotEnabledMessage = "Visual Test is not enabled";
    private final com.applitools.eyes.selenium.Eyes eyesOnWeb;
    private final com.applitools.eyes.appium.Eyes eyesOnApp;
    private final TestExecutionContext context;
    private final ScreenShotManager screenShotManager;
    private final String targetEnvironment = Runner.getTargetEnvironment();
    private final Map applitoolsConfig;
    private final boolean isEnableBenchmarkPerValidation;
    private final boolean isVerboseLoggingEnabled;
    private String applitoolsLogFileNameForWeb = Runner.NOT_SET;
    private String applitoolsLogFileNameForApp = Runner.NOT_SET;

    public Visual (String driverType, WebDriver innerDriver, String testName, boolean isVisualTestingEnabled) {
        LOGGER.info("Visual constructor: Driver type: " + driverType + ", testName: " + testName + ", isVisualTestingEnabled:  " + isVisualTestingEnabled);
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        this.applitoolsConfig = Runner.initialiseApplitoolsConfiguration();
        this.isEnableBenchmarkPerValidation = Boolean.parseBoolean(String.valueOf(this.applitoolsConfig.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
        this.isVerboseLoggingEnabled = (boolean) getValueFromConfig(APPLITOOLS.ENABLE_VERBOSE_LOGS, true);
        String appName = this.applitoolsConfig.get(APPLITOOLS.APP_NAME) + "-";
        eyesOnApp = instantiateAppiumEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        appName += Platform.android;
        LOGGER.info("instantiateAppiumEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        com.applitools.eyes.appium.Eyes eyes = new com.applitools.eyes.appium.Eyes();

        eyes.setApiKey(String.valueOf(getValueFromConfig(APPLITOOLS.API_KEY)));
        eyes.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        eyes.setEnvName(targetEnvironment);
        eyes.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));
        eyes.setIsDisabled(!isVisualTestingEnabled);

        applitoolsLogFileNameForApp = getApplitoolsLogFileNameFor("app");
        eyes.setLogHandler(new FileLogger(applitoolsLogFileNameForApp, true, isVerboseLoggingEnabled));

        if (isVisualTestingEnabled) {
            eyes.open(innerDriver, appName, testName);
        }
        LOGGER.info("instantiateAppiumEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        appName += Platform.web;
        LOGGER.info("instantiateWebEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        com.applitools.eyes.selenium.Eyes eyes = new com.applitools.eyes.selenium.Eyes();
        eyes.setApiKey(String.valueOf(getValueFromConfig(APPLITOOLS.API_KEY)));
        eyes.setBatch((BatchInfo) getValueFromConfig(APPLITOOLS.BATCH_NAME));
        eyes.setEnvName(targetEnvironment);
        eyes.setMatchLevel((MatchLevel) getValueFromConfig(APPLITOOLS.DEFAULT_MATCH_LEVEL, MatchLevel.STRICT));
        eyes.setIsDisabled(!isVisualTestingEnabled);

        eyes.setSendDom((boolean) getValueFromConfig(APPLITOOLS.SEND_DOM, true));
        eyes.setStitchMode(StitchMode.valueOf(String.valueOf(getValueFromConfig(APPLITOOLS.STITCH_MODE, StitchMode.CSS)).toUpperCase()));
        eyes.setForceFullPageScreenshot((boolean) getValueFromConfig(APPLITOOLS.TAKE_FULL_PAGE_SCREENSHOT, true));

        applitoolsLogFileNameForWeb = getApplitoolsLogFileNameFor("web");
        eyes.setLogHandler(new FileLogger(applitoolsLogFileNameForWeb, true, isVerboseLoggingEnabled));
        if (isVisualTestingEnabled) {
            eyes.open(innerDriver, appName, testName, (RectangleSize) getValueFromConfig(APPLITOOLS.RECTANGLE_SIZE));
        }
        LOGGER.info("instantiateWebEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    private Object getValueFromConfig (String key, Object defaultValue) {
        return (null == applitoolsConfig.get(key)) ? defaultValue : applitoolsConfig.get(key);
    }

    private Object getValueFromConfig (String key) {
        return getValueFromConfig(key, null);
    }

    @NotNull
    private String getApplitoolsLogFileNameFor (String appType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        String eyesLogFile = scenarioLogDir + File.separator + "deviceLogs" + File.separator + "applitools-" + appType + ".log";
        return eyesLogFile;
    }

    public Visual checkWindow (String fromScreen, String tag) {
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

        screenShotManager.takeScreenShot(formattedTagName);
        return this;
    }

    @NotNull
    private String getFormattedTagName (String fromScreen, String tag) {
        return fromScreen + " : " + tag;
    }

    public Visual check (String fromScreen, String tag, SeleniumCheckSettings checkSettings) {
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

        screenShotManager.takeScreenShot(formattedTagName);
        return this;
    }

    public Visual checkWindow (String fromScreen, String tag, MatchLevel level) {
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

        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    public Visual takeScreenshot (String fromScreen, String tag) {
        screenShotManager.takeScreenShot(getFormattedTagName(fromScreen, tag));
        return this;
    }

    public void handleTestResults (String userPersona) {
        getVisualResultsFromWeb(userPersona);
        getVisualResultsFromApp(userPersona);
    }

    private String getVisualResultsFromWeb (String userPersona) {
        LOGGER.info("getVisualResultsFromWeb: user: " + userPersona);
        TestResults visualResults = eyesOnWeb.close(false);
        String reportUrl = handleTestResults(visualResults);
        String message = String.format("Web Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        LOGGER.info(message);
        LOGGER.info("Applitools logs available here: " + applitoolsLogFileNameForWeb);
        ReportPortal.emitLog(message, "DEBUG", new Date(), new File(applitoolsLogFileNameForWeb));
        return reportUrl;
    }

    private String getVisualResultsFromApp (String userPersona) {
        LOGGER.info("getVisualResultsFromApp: user: " + userPersona);
        TestResults visualResults = eyesOnApp.close(false);
        String reportUrl = handleTestResults(visualResults);
        String message = String.format("App Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        LOGGER.info(message);
        LOGGER.info("Applitools logs available here: " + applitoolsLogFileNameForApp);
        ReportPortal.emitLog(message, "DEBUG", new Date(), new File(applitoolsLogFileNameForApp));
        return reportUrl;
    }

    private String handleTestResults (TestResults result) {
        LOGGER.info("\t\t" + result);
        LOGGER.info("\t\tmatched = " + result.getMatches()
                + ", mismatched = " + result.getMismatches()
                + ", missing = " + result.getMissing()
                + ", isNew: " + result.isNew()
                + ", isPassed: ");
        LOGGER.info("Visual Testing results available here: " + result.getUrl());
        boolean hasMismatches = result.getMismatches() != 0;
        LOGGER.info("Visual testing differences found? - " + hasMismatches);
        return result.getUrl();
    }
}
