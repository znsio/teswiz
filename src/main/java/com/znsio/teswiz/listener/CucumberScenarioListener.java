package com.znsio.teswiz.listener;

import com.epam.reportportal.service.ReportPortal;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.runner.atd.*;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static com.znsio.teswiz.runner.atd.OverriddenVariable.getOverriddenStringValue;

public class CucumberScenarioListener implements ConcurrentEventListener {
    private static final Logger LOGGER = Logger
            .getLogger(CucumberScenarioListener.class.getName());
    private final ATD_AppiumDriverManager appiumDriverManager;
    private final ATD_AppiumServerManager appiumServerManager;
    private Map<String, Integer> scenarioRunCounts = new HashMap<String, Integer>();

    public CucumberScenarioListener() throws Exception {
        LOGGER.info(String.format("ThreadID: %d: CucumberScenarioListener%n",
                Thread.currentThread().getId()));
        new ATDRunner();
        appiumServerManager = new ATD_AppiumServerManager();
        appiumDriverManager = new ATD_AppiumDriverManager();
    }

    private AppiumDriver allocateDeviceAndStartDriver(String scenarioName) {
        AppiumDriver driver = ATD_AppiumDriverManager.getDriver();
        if (driver == null || driver.getSessionId() == null) {
            return appiumDriverManager.startAppiumDriverInstance(scenarioName);
        } else {
            return driver;
        }
    }

    private String getCapabilityFor(org.openqa.selenium.Capabilities capabilities, String name) {
        Object capability = capabilities.getCapability(name);
        return null == capability ? "" : capability.toString();
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::runStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::caseStartedHandler);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::caseFinishedHandler);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::runFinishedHandler);
    }

    private void runStartedHandler(TestRunStarted event) {
        LOGGER.info("runStartedHandler");
        LOGGER.info(String.format("ThreadID: %d: beforeSuite: %n",
                Thread.currentThread().getId()));
    }

    public static File createFile(String dirName, String fileName) {
        File logFile = new File(System.getProperty("user.dir")
                + dirName
                + fileName + ".txt");
        if (logFile.exists()) {
            return logFile;
        }
        try {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
        return logFile;
    }

    private void caseStartedHandler(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("$$$$$   TEST-CASE  -- " + scenarioName + "  STARTED   $$$$$");
        LOGGER.info("caseStartedHandler: " + scenarioName);
        Integer scenarioRunCount = getScenarioRunCount(scenarioName);
        String normalisedScenarioName = normaliseScenarioName(scenarioName);
        LOGGER.info(
                String.format("ThreadID: %d: beforeScenario: for scenario: %s%n",
                        Thread.currentThread().getId(), scenarioName));
        String scenarioReportDirectory = FileLocations.REPORTS_DIRECTORY
                                                 + normalisedScenarioName + File.separator;
        AppiumDriver createdAppiumDriver = allocateDeviceAndStartDriver(scenarioName);
        String deviceLogFileName = startDataCapture(scenarioRunCount,
                scenarioReportDirectory);

        TestExecutionContext testExecutionContext = new TestExecutionContext(scenarioName);
        testExecutionContext.addTestState("appiumDriver", createdAppiumDriver);
        testExecutionContext.addTestState("deviceId",
                ATD_AppiumDeviceManager.getAppiumDevice().getUdid());
        testExecutionContext.addTestState("deviceInfo", ATD_AppiumDeviceManager.getAppiumDevice());
        testExecutionContext.addTestState("deviceLog", deviceLogFileName);
        testExecutionContext.addTestState("scenarioRunCount", scenarioRunCount);
        testExecutionContext.addTestState("normalisedScenarioName", normalisedScenarioName);
        testExecutionContext.addTestState("scenarioDirectory", scenarioReportDirectory);
        testExecutionContext.addTestState("scenarioScreenshotsDirectory",
                scenarioReportDirectory
                        + "screenshot"
                        + File.separator);
    }

    private String startDataCapture(Integer scenarioRunCount,
                                    String deviceLogFileDirectory) {
        String fileName = String.format("/run-%s", scenarioRunCount);
        if (ATD_AppiumDeviceManager.getAppiumDevice().getPlatformName().equalsIgnoreCase("android")) {
            fileName = String.format("/%s-run-%s",
                            ATD_AppiumDeviceManager.getAppiumDevice().getUdid(), scenarioRunCount);
            File logFile = createFile(deviceLogFileDirectory
                                      + FileLocations.DEVICE_LOGS_DIRECTORY,
                    fileName);
            fileName = logFile.getAbsolutePath();
            LOGGER.debug("Capturing device logs here: " + fileName);
            PrintStream logFileStream = null;
            try {
                logFileStream = new PrintStream(logFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                LogEntries logcatOutput = ATD_AppiumDriverManager.getDriver()
                                                  .manage().logs().get("logcat");
                StreamSupport.stream(logcatOutput.spliterator(), false)
                        .forEach(logFileStream::println);
            } catch (Exception e) {
                LOGGER.warn("ERROR in getting logcat. Skipping logcat capture");
            }
        }
        return fileName;
    }

    private boolean isRunningOnpCloudy() {
        boolean isPCloudy = getCloudName().equalsIgnoreCase("pCloudy");
        LOGGER.info(ATD_AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: "
                + getCloudName());
        return isPCloudy;
    }

    private static String getCloudName() {
        return PluginClI.getInstance().getCloudName();
    }

    private boolean isRunningOnBrowserStack() {
        boolean isBrowserStack = getCloudName().equalsIgnoreCase("browserstack");
        LOGGER.info(ATD_AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: "
                + getCloudName());
        return isBrowserStack;
    }

    private boolean isRunningOnHeadspin() {
        boolean isHeadspin = getCloudName().equalsIgnoreCase("headspin");
        LOGGER.info(ATD_AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: "
                + getCloudName());
        return isHeadspin;
    }

    private String normaliseScenarioName(String scenarioName) {
        return scenarioName.replaceAll("[`~ !@#$%^&*()\\-=+\\[\\]{}\\\\|;:'\",<.>/?]", "_");
    }

    private Integer getScenarioRunCount(String scenarioName) {
        if (scenarioRunCounts.containsKey(scenarioName)) {
            scenarioRunCounts.put(scenarioName, scenarioRunCounts.get(scenarioName) + 1);
        } else {
            scenarioRunCounts.put(scenarioName, 1);
        }
        return scenarioRunCounts.get(scenarioName);
    }

    private void caseFinishedHandler(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        LOGGER.info("caseFinishedHandler Name: " + scenarioName);
        long threadId = Thread.currentThread().getId();
        LOGGER.info(
                String.format("ThreadID: %d: afterScenario: for scenario: %s%n",
                        threadId, event.getTestCase().toString()));

        TestExecutionContext testExecutionContext =
                SessionContext.getTestExecutionContext(threadId);

        AppiumDriver driver = (AppiumDriver) testExecutionContext.getTestState("appiumDriver");
        attachCloudExecutionReportLinkToReportPortal(driver);
        stopAppiumDriver();
        String deviceLogFileName = testExecutionContext.getTestStateAsString("deviceLog");
        if (null != deviceLogFileName) {
            LOGGER.debug(String.format("Attaching device logs %s to ReportPortal: ",
                    deviceLogFileName));
            File file = new File(deviceLogFileName);
            ReportPortal.emitLog("ADB Logs - " + file.getName(),
                    "DEBUG", new Date(), file);
        }
        SessionContext.remove(threadId);
        LOGGER.info("$$$$$   TEST-CASE  -- " + scenarioName + "  ENDED   $$$$$");
    }

    private void stopAppiumDriver() {
        try {
            appiumDriverManager.stopAppiumDriver();
        } catch (Exception e) {
            LOGGER.warn("Error stopping Appium Driver", e);
        }
    }

    private void attachCloudExecutionReportLinkToReportPortal(AppiumDriver driver) {
        if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnpCloudy()) {
            String link = (String) driver.executeScript("pCloudy_getReportLink");
            String message = "pCloudy Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        } else if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnHeadspin()) {
            String sessionId = driver.getSessionId().toString();
            String link = "https://ui-dev.headspin.io/sessions/" + sessionId + "/waterfall";
            String message = "Headspin Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        } else if ((PluginClI.getInstance().isCloudExecution()) && isRunningOnBrowserStack()) {
            String sessionId = driver.getSessionId().toString();
            String link = getReportLinkFromBrowserStack(sessionId);
            String message = "BrowserStack Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        }
    }

    private static String getReportLinkFromBrowserStack(String sessionId) {
        String browserStackTestResultUrl = "";
        String cloudUser = getOverriddenStringValue("CLOUD_USERNAME");
        String cloudPassword = getOverriddenStringValue("CLOUD_KEY");
        String resultStdOut = null;
        try {
            String[] curlCommand = new String[]{"curl --in" +
                                                 "secure " + getCurlProxyCommand() + " -u \"" + cloudUser + ":" + cloudPassword + "\" -X GET \"https://api-cloud.browserstack.com/app-automate/sessions/" + sessionId + ".json\""};
            CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(curlCommand);
            LOGGER.info(String.format("Response from BrowserStack - '%s'", commandLineResponse.getStdOut()));
            JSONObject pr = new JSONObject(commandLineResponse.getStdOut());
            JSONObject automation_session = pr.getJSONObject("automation_session");
            browserStackTestResultUrl = automation_session.getString("browser_url");
            LOGGER.info("BrowserStack execution link: " + browserStackTestResultUrl);
        } catch (Exception e) {
            LOGGER.info("Unable to get test execution link from BrowserStack: " + e.getMessage());
            ExceptionUtils.getStackTrace(e);
        }
        return browserStackTestResultUrl;
    }

    static String getCurlProxyCommand() {
        String curlProxyCommand = "";
        if (null != getOverriddenStringValue("PROXY_URL")) {
            curlProxyCommand = " --proxy " + System.getProperty("PROXY_URL");
        }
        return curlProxyCommand;
    }

    private void runFinishedHandler(TestRunFinished event) {
        LOGGER.info("runFinishedHandler: " + event.getResult().toString());
        LOGGER.info(String.format("ThreadID: %d: afterSuite: %n",
                Thread.currentThread().getId()));
        try {
            appiumServerManager.destroyAppiumNode();
            SessionContext.setReportPortalLaunchURL();
        } catch (Exception e) {
            ExceptionUtils.getStackTrace(e);
        }
    }
}
