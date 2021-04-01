package com.znsio.e2e.tools;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.APPLITOOLS;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class Visual {
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
        System.out.printf("Visual constructor: Driver type: '%s', testName: '%s', isVisualTestingEnabled: '%s'%n", driverType, testName, isVisualTestingEnabled);
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        this.applitoolsConfig = Runner.initialiseApplitoolsConfiguration();
        this.isEnableBenchmarkPerValidation = Boolean.parseBoolean(String.valueOf(this.applitoolsConfig.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
        this.isVerboseLoggingEnabled = (boolean) getValueFromConfig(APPLITOOLS.ENABLE_VERBOSE_LOGS, true);
        String appName = this.applitoolsConfig.get(APPLITOOLS.APP_NAME) + "-" + Runner.platform;
        eyesOnApp = instantiateAppiumEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        System.out.println("instantiateAppiumEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
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
        System.out.println("instantiateAppiumEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        System.out.println("instantiateWebEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
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
        System.out.println("instantiateWebEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
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
        System.out.printf("checkWindow: fromScreen: '%s', tag: '%s'%n", fromScreen, formattedTagName);
        System.out.println("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        System.out.println("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.checkWindow(formattedTagName);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: Web: checkWindow: Time taken: '%d' sec%n", fromScreen, tag, webDuration.getSeconds());
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.checkWindow(formattedTagName);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: App: checkWindow: Time taken: '%d' sec%n", fromScreen, tag, appDuration.getSeconds());
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
        System.out.printf("check: fromScreen: '%s', tag: '%s'%n", fromScreen, formattedTagName);
        System.out.println("check: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        System.out.println("check: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());

        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(formattedTagName, checkSettings);
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: Web: check: Time taken: '%d' sec%n", fromScreen, tag, webDuration.getSeconds());
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(formattedTagName, checkSettings);
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: App: check: Time taken: '%d' sec%n", fromScreen, tag, appDuration.getSeconds());
        }

        screenShotManager.takeScreenShot(formattedTagName);
        return this;
    }

    public Visual checkWindow (String fromScreen, String tag, MatchLevel level) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        System.out.printf("checkWindow: fromScreen: '%s', MatchLevel: '%s', tag: '%s'%n", fromScreen, level, formattedTagName);
        System.out.println("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        System.out.println("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());


        LocalDateTime webStart = LocalDateTime.now();
        eyesOnWeb.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime webFinish = LocalDateTime.now();
        Duration webDuration = Duration.between(webStart, webFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: Web: checkWindow with MatchLevel: '%s': Time taken: '%d' sec%n", fromScreen, tag, level.name(), webDuration.getSeconds());
        }

        LocalDateTime appStart = LocalDateTime.now();
        eyesOnApp.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        LocalDateTime appFinish = LocalDateTime.now();
        Duration appDuration = Duration.between(appStart, appFinish);
        if (isEnableBenchmarkPerValidation) {
            System.out.printf("'%s':'%s':: App: checkWindow with MatchLevel: '%s': Time taken: '%d' sec%n", fromScreen, tag, level.name(), appDuration.getSeconds());
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
        System.out.println("getVisualResultsFromWeb: user: " + userPersona);
        TestResults visualResults = eyesOnWeb.close(false);
        String reportUrl = handleTestResults(visualResults);
        String message = String.format("Web Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        System.out.println(message);
        System.out.println("Applitools logs available here: " + applitoolsLogFileNameForWeb);
        ReportPortal.emitLog(message, "DEBUG", new Date(), new File(applitoolsLogFileNameForWeb));
        return reportUrl;
    }

    private String getVisualResultsFromApp (String userPersona) {
        System.out.println("getVisualResultsFromApp: user: " + userPersona);
        TestResults visualResults = eyesOnApp.close(false);
        String reportUrl = handleTestResults(visualResults);
        String message = String.format("App Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        System.out.println(message);
        System.out.println("Applitools logs available here: " + applitoolsLogFileNameForApp);
        ReportPortal.emitLog(message, "DEBUG", new Date(), new File(applitoolsLogFileNameForApp));
        return reportUrl;
    }

    private String handleTestResults (TestResults result) {
        System.out.println("\t\t" + result);
        System.out.printf("\t\tmatched = %d, mismatched = %d, missing = %d, isNew: %s, isPassed: %s%n",
                result.getMatches(),
                result.getMismatches(),
                result.getMissing(),
                result.isNew(),
                result.isPassed());
        System.out.println("Visual Testing results available here: " + result.getUrl());
        boolean hasMismatches = result.getMismatches() != 0;
        System.out.println("Visual testing differences found? - " + hasMismatches);
        return result.getUrl();
    }
}
