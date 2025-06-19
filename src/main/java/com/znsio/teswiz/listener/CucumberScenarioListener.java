package com.znsio.teswiz.listener;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.AppiumServerManager;
import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.FileUtils;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CucumberScenarioListener implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger.getLogger(CucumberScenarioListener.class.getName());
//    private final AppiumDriverManager appiumDriverManager;
//    private final AppiumServerManager appiumServerManager;
    private final Map<String, Integer> numberOfExamplesForScenario = new HashMap<String, Integer>();
    private int runningScenarioNumber = 0;

    public CucumberScenarioListener() throws Exception {
        LOGGER.info(String.format("ThreadID: %d: CucumberScenarioListener%n", Thread.currentThread().getId()));
        setLog4jCompatibility();
        FileUtils.createDirectoryIn(Runner.USER_DIRECTORY, FileLocations.OUTPUT_DIRECTORY);

        // todo - for appium - done
//        CustomCapabilities.getInstance();
//        writeServiceConfig();
//        appiumServerManager = new AppiumServerManager();
//        appiumServerManager.startAppiumServer("127.0.0.1"); //Needs to be removed
//        appiumDriverManager = new AppiumDriverManager();
    }

    private void setLog4jCompatibility() {
        // Migrating from Log4j 1.x to 2.x - https://logging.apache.org/log4j/2.x/manual/migration.html
        System.setProperty("log4j1.compatibility", "true");
    }

//    private void writeServiceConfig() {
//        JSONObject serverConfig = CustomCapabilities.getInstance().getCapabilityObjectFromKey("serverConfig");
//        try (FileWriter writer = new FileWriter(new File(Runner.USER_DIRECTORY + SERVER_CONFIG_JSON))) {
//            writer.write(serverConfig.toString());
//            writer.flush();
//        } catch (IOException e) {
//            ExceptionUtils.getStackTrace(e);
//        }
//    }

//    private AppiumDriver allocateDeviceAndStartDriver(String scenarioName) {
//        AppiumDriver driver = AppiumDriverManager.getDriver();
//        if (driver == null || driver.getSessionId() == null) {
//            return appiumDriverManager.startAppiumDriverInstance(scenarioName);
//        } else {
//            return driver;
//        }
//    }

//    private String getCapabilityFor(org.openqa.selenium.Capabilities capabilities, String name) {
//        Object capability = capabilities.getCapability(name);
//        return null == capability ? "" : capability.toString();
//    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::runStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::scenarioStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::scenarioFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::runFinishedHandler);
    }

    private void runStartedHandler(TestRunStarted event) {
        LOGGER.info("runStartedHandler");
        LOGGER.info(String.format("ThreadID: %d: beforeSuite: %n", Thread.currentThread().getId()));
    }

    private void scenarioStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        Integer currentExampleRowNumberForScenario = updateCurrentExampleRowNumberForScenario(scenarioName);
        runningScenarioNumber++;

        LOGGER.info("Running Scenario #" + runningScenarioNumber + " '" + scenarioName + "' started");
        LOGGER.info("\tCurrent Example Row Number: " + currentExampleRowNumberForScenario);
        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioName + "-" + currentExampleRowNumberForScenario);

        String normalisedScenarioName = normaliseScenarioName(scenarioName);
        String scenarioLogDirectory = FileLocations.REPORTS_DIRECTORY + runningScenarioNumber + "-" + normalisedScenarioName + "_" + currentExampleRowNumberForScenario + File.separator;
        String screenshotDirectory = scenarioLogDirectory + FileLocations.SCREENSHOTS_DIRECTORY;
        String deviceLogsDirectory = scenarioLogDirectory + FileLocations.DEVICE_LOGS_DIRECTORY;

        scenarioLogDirectory = FileUtils.createDirectoryIn(Runner.USER_DIRECTORY, scenarioLogDirectory).getAbsolutePath();
        screenshotDirectory = FileUtils.createDirectoryIn(Runner.USER_DIRECTORY, screenshotDirectory).getAbsolutePath();
        deviceLogsDirectory = FileUtils.createDirectoryIn(Runner.USER_DIRECTORY, deviceLogsDirectory).getAbsolutePath();
        testExecutionContext.addTestState(TEST_CONTEXT.EXAMPLE_RUN_COUNT, currentExampleRowNumberForScenario);
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, runningScenarioNumber);
        testExecutionContext.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, normalisedScenarioName);
        testExecutionContext.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioLogDirectory);
        testExecutionContext.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory);
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_LOGS_DIRECTORY, deviceLogsDirectory);

        // todo - for appium - done
//        AppiumDriver createdAppiumDriver = allocateDeviceAndStartDriver(scenarioName);
//        String deviceLogFileName = AppiumDriverManager.startDataCapture();
//        testExecutionContext.addTestState("appiumDriver", createdAppiumDriver);
//        testExecutionContext.addTestState("deviceId", AppiumDeviceManager.getAppiumDevice().getUdid());
//        testExecutionContext.addTestState("deviceInfo", AppiumDeviceManager.getAppiumDevice());
//        testExecutionContext.addTestState("deviceLog", deviceLogFileName);
    }

//    private boolean isRunningOnpCloudy() {
//        boolean isPCloudy = getCloudName().equalsIgnoreCase("pCloudy");
//        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
//        return isPCloudy;
//    }
//
//    private static String getCloudName() {
//        return PluginClI.getInstance().getCloudName();
//    }
//
//    private boolean isRunningOnBrowserStack() {
//        boolean isBrowserStack = getCloudName().equalsIgnoreCase("browserstack");
//        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
//        return isBrowserStack;
//    }
//
//    private boolean isRunningOnHeadspin() {
//        boolean isHeadspin = getCloudName().equalsIgnoreCase("headspin");
//        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
//        return isHeadspin;
//    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_");
    }

    private Integer updateCurrentExampleRowNumberForScenario(String scenarioName) {
        if (numberOfExamplesForScenario.containsKey(scenarioName)) {
            numberOfExamplesForScenario.put(scenarioName, numberOfExamplesForScenario.get(scenarioName) + 1);
        } else {
            numberOfExamplesForScenario.put(scenarioName, 1);
        }
        return numberOfExamplesForScenario.get(scenarioName);
    }

    private Integer getCurrentExampleRowNumberForScenario(String scenarioName) {
        return numberOfExamplesForScenario.get(scenarioName);
    }

    private void scenarioFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        Integer currentExampleRowNumberForScenario = getCurrentExampleRowNumberForScenario(scenarioName);

        LOGGER.info("Finished Scenario #" + runningScenarioNumber + "'" + scenarioName + "' started");
        LOGGER.info("\tCurrent Example Row Number: " + currentExampleRowNumberForScenario);

        long threadId = Thread.currentThread().getId();
        TestExecutionContext testExecutionContext = SessionContext.getTestExecutionContext(threadId);

        // todo - for appium
//        AppiumDriver driver = (AppiumDriver) testExecutionContext.getTestState("appiumDriver");
//        attachCloudExecutionReportLinkToReportPortal(driver);
//        stopAppiumDriver();
//        String deviceLogFileName = testExecutionContext.getTestStateAsString("deviceLog");
//        if (null != deviceLogFileName) {
//            LOGGER.debug(String.format("Attaching device logs %s to ReportPortal: ", deviceLogFileName));
//            File file = new File(deviceLogFileName);
//            ReportPortal.emitLog("ADB Logs - " + file.getName(), "DEBUG", new Date(), file);
//        }
        SessionContext.remove(threadId);
    }

//    private void stopAppiumDriver() {
//        try {
//            appiumDriverManager.stopAppiumDriver();
//        } catch (Exception e) {
//            LOGGER.warn("Error stopping Appium Driver", e);
//        }
//    }

//    private void attachCloudExecutionReportLinkToReportPortal(AppiumDriver driver) {
//        if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnpCloudy()) {
//            String link = (String) driver.executeScript("pCloudy_getReportLink");
//            String message = "pCloudy Report link available here: " + link;
//            LOGGER.info(message);
//            ReportPortal.emitLog(message, "DEBUG", new Date());
//        } else if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnHeadspin()) {
//            String sessionId = driver.getSessionId().toString();
//            String link = "https://ui-dev.headspin.io/sessions/" + sessionId + "/waterfall";
//            String message = "Headspin Report link available here: " + link;
//            LOGGER.info(message);
//            ReportPortal.emitLog(message, "DEBUG", new Date());
//        } else if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnBrowserStack()) {
//            String sessionId = driver.getSessionId().toString();
//            String link = getReportLinkFromBrowserStack(sessionId);
//            String message = "BrowserStack Report link available here: " + link;
//            LOGGER.info(message);
//            ReportPortal.emitLog(message, "DEBUG", new Date());
//        }
//    }

//    private static String getReportLinkFromBrowserStack(String sessionId) {
//        String browserStackTestResultUrl = "";
//        String cloudUser = getOverriddenStringValue("CLOUD_USERNAME");
//        String cloudPassword = getOverriddenStringValue("CLOUD_KEY");
//        String resultStdOut = null;
//        try {
//            String[] curlCommand = new String[]{"curl --in" + "secure " + getCurlProxyCommand() + " -u \"" + cloudUser + ":" + cloudPassword + "\" -X GET \"https://api-cloud.browserstack.com/app-automate/sessions/" + sessionId + ".json\""};
//            CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(curlCommand);
//            LOGGER.info(String.format("Response from BrowserStack - '%s'", commandLineResponse.getStdOut()));
//            JSONObject pr = new JSONObject(commandLineResponse.getStdOut());
//            JSONObject automation_session = pr.getJSONObject("automation_session");
//            browserStackTestResultUrl = automation_session.getString("browser_url");
//            LOGGER.info("BrowserStack execution link: " + browserStackTestResultUrl);
//        } catch (Exception e) {
//            LOGGER.info("Unable to get test execution link from BrowserStack: " + e.getMessage());
//            ExceptionUtils.getStackTrace(e);
//        }
//        return browserStackTestResultUrl;
//    }

//    static String getCurlProxyCommand() {
//        String curlProxyCommand = "";
//        if (null != getOverriddenStringValue("PROXY_URL")) {
//            curlProxyCommand = " --proxy " + System.getProperty("PROXY_URL");
//        }
//        return curlProxyCommand;
//    }

    private void runFinishedHandler(TestRunFinished event) {
        LOGGER.info("runFinishedHandler: " + event.getResult().toString());
        LOGGER.info(String.format("ThreadID: %d: afterSuite: %n", Thread.currentThread().getId()));
        try {
            AppiumServerManager.destroyAppiumNode();
            SessionContext.setReportPortalLaunchURL();
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
    }
}
