package com.znsio.e2e.tools;

import com.appium.utils.Variable;
import com.applitools.eyes.FileLogger;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.runner.Runner;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Date;

public class Visual {
    private final String visualTestNotEnabledMessage = "Visual Test is not enabled";
    private final com.applitools.eyes.selenium.Eyes eyesOnWeb;
    private final com.applitools.eyes.appium.Eyes eyesOnApp;
    private final TestExecutionContext context;
    private final ScreenShotManager screenShotManager;
    private final RectangleSize viewportSize = new RectangleSize(1024, 800);
    private final String applitoolsApiKey = Variable.getOverriddenStringValue("APPLITOOLS_API_KEY", Runner.NOT_SET);
    private final String targetEnvironment = Runner.getTargetEnvironment();

    public Visual (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        System.out.printf("Visual constructor: Driver type: '%s', appName: '%s', testName: '%s', isVisualTestingEnabled: '%s'%n", driverType, appName, testName, isVisualTestingEnabled);
        this.context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        this.screenShotManager = (ScreenShotManager) context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER);
        eyesOnApp = instantiateAppiumEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
        eyesOnWeb = instantiateWebEyes(driverType, innerDriver, appName, testName, isVisualTestingEnabled);
    }

    private com.applitools.eyes.appium.Eyes instantiateAppiumEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.WEB_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        System.out.println("instantiateAppiumEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        com.applitools.eyes.appium.Eyes eyes = new com.applitools.eyes.appium.Eyes();
        eyes.setApiKey(applitoolsApiKey);
        eyes.setBatch(Runner.getApplitoolsBatchName());
        eyes.setLogHandler(new FileLogger(getApplitoolsLogFileNameFor("app"), true, true));
//        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setEnvName(targetEnvironment);
        eyes.setIsDisabled(!isVisualTestingEnabled);
        eyes.setMatchLevel(MatchLevel.STRICT);
        if (isVisualTestingEnabled) {
            eyes.open(innerDriver, appName, testName);
        }
        System.out.println("instantiateAppiumEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    @NotNull
    private String getApplitoolsLogFileNameFor (String appType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        String eyesAppLogFile = scenarioLogDir + File.separator + "deviceLogs" + File.separator + "applitools-" + appType + ".log";
        return eyesAppLogFile;
    }

    private com.applitools.eyes.selenium.Eyes instantiateWebEyes (String driverType, WebDriver innerDriver, String appName, String testName, boolean isVisualTestingEnabled) {
        if (driverType.equals(Driver.APPIUM_DRIVER)) {
            isVisualTestingEnabled = false;
        }
        System.out.println("instantiateWebEyes: isVisualTestingEnabled: " + isVisualTestingEnabled);
        com.applitools.eyes.selenium.Eyes eyes = new com.applitools.eyes.selenium.Eyes();
        eyes.setApiKey(applitoolsApiKey);
        eyes.setBatch(Runner.getApplitoolsBatchName());
        eyes.setLogHandler(new FileLogger(getApplitoolsLogFileNameFor("web"), true, true));
//        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setEnvName(targetEnvironment);
        eyes.setIsDisabled(!isVisualTestingEnabled);
        eyes.setMatchLevel(MatchLevel.STRICT);
        if (isVisualTestingEnabled) {
            eyes.open(innerDriver, appName, testName, viewportSize);
        }
        System.out.println("instantiateWebEyes: eyes.getIsDisabled(): " + eyes.getIsDisabled());
        return eyes;
    }

    public Visual checkWindow (String fromScreen, String tag) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        System.out.printf("checkWindow: fromScreen: '%s', tag: '%s'%n", fromScreen, formattedTagName);
        System.out.println("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        System.out.println("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());
        eyesOnWeb.checkWindow(formattedTagName);
        eyesOnApp.checkWindow(formattedTagName);
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
        eyesOnWeb.check(formattedTagName, checkSettings);
        eyesOnApp.check(formattedTagName, checkSettings);
        screenShotManager.takeScreenShot(formattedTagName);
        return this;
    }

    public Visual checkWindow (String fromScreen, String tag, MatchLevel level) {
        String formattedTagName = getFormattedTagName(fromScreen, tag);
        System.out.printf("checkWindow: fromScreen: '%s', MatchLevel: '%s', tag: '%s'%n", fromScreen, level, formattedTagName);
        System.out.println("checkWindow: eyesOnWeb.getIsDisabled(): " + eyesOnWeb.getIsDisabled());
        System.out.println("checkWindow: eyesOnApp.getIsDisabled(): " + eyesOnApp.getIsDisabled());
        eyesOnWeb.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
        eyesOnApp.check(getFormattedTagName(fromScreen, tag), Target.window().matchLevel(level));
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
        ReportPortal.emitLog(message, "DEBUG", new Date());
        return reportUrl;
    }

    private String getVisualResultsFromApp (String userPersona) {
        System.out.println("getVisualResultsFromApp: user: " + userPersona);
        TestResults visualResults = eyesOnApp.close(false);
        String reportUrl = handleTestResults(visualResults);
        String message = String.format("App Visual Testing Results for user persona: '%s' :: '%s'", userPersona, reportUrl);
        System.out.println(message);
        ReportPortal.emitLog(message, "DEBUG", new Date());
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
