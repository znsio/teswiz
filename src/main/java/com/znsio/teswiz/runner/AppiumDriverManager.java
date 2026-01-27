package com.znsio.teswiz.runner;

import com.epam.reportportal.service.ReportPortal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.znsio.teswiz.runner.FileLocations.SERVER_CONFIG_JSON;
import static com.znsio.teswiz.runner.Runner.DEFAULT;
import static com.znsio.teswiz.runner.Runner.NOT_SET;
import static com.znsio.teswiz.runner.Setup.CAPS;
import static com.znsio.teswiz.runner.Setup.RUN_IN_CI;
import static com.znsio.teswiz.tools.OverriddenVariable.getOverriddenStringValue;

public class AppiumDriverManager {
    private static final int MAX_NUMBER_OF_APPIUM_DRIVERS = Runner.getMaxNumberOfAppiumDrivers();
    private static final Logger LOGGER = LogManager.getLogger(AppiumDriverManager.class.getName());
    private static final String CAPABILITIES = "CAPABILITIES: ";
    private static int numberOfAppiumDriversUsed = 0;
    private static final ThreadLocal<AppiumDriver> appiumDriver = new ThreadLocal<>();
    private static AppiumServerManager appiumServerManager = null;
    private static AppiumDriverManager appiumDriverManager = null;

    @NotNull
    static Driver createAndroidDriverForUser(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createAndroidDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        Driver currentDriver;
        if (numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: " + "'%s'", numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }

        currentDriver = setupOrCreateAndroidDriver(userPersona, forPlatform, context);
        LOGGER.info(String.format("createAndroidDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver, context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON), (String) Drivers.getCapabilitiesFor(userPersona).getCapability("udid"));
        return currentDriver;
    }

    @NotNull
    private static Driver setupOrCreateAndroidDriver(String userPersona, Platform forPlatform, TestExecutionContext context) {
        String appName = Drivers.getAppNamefor(userPersona);
        File capabilityFileToUseForDriverCreation = getCapabilityFileToUseForDriverCreation(appName);
        Driver currentDriver;
        if (numberOfAppiumDriversUsed == 0) {
            currentDriver = setupFirstAppiumDriver(userPersona, forPlatform, context, appName);
        } else {
            currentDriver = createNewAppiumDriver(userPersona, forPlatform, context, appName, capabilityFileToUseForDriverCreation);
        }
        numberOfAppiumDriversUsed++;
        return currentDriver;
    }

    @NotNull
    private static File getCapabilityFileToUseForDriverCreation(String appName) {
        String capabilityFileNameToUseForDriverCreation = System.getProperty(CAPS);

        if (!appName.equalsIgnoreCase(DEFAULT)) {
            String capabilityFileDirectory = new File(capabilityFileNameToUseForDriverCreation).getParent();
            capabilityFileNameToUseForDriverCreation = capabilityFileDirectory + File.separator + (appName + "_capabilities.json");
        }
        File capabilityFileToUseForDriverCreation = new File(capabilityFileNameToUseForDriverCreation);
        LOGGER.info(String.format("capabilityFileToUseForDriverCreation: %s", capabilityFileToUseForDriverCreation.getAbsolutePath()));
        return capabilityFileToUseForDriverCreation;
    }

    @NotNull
    private static Driver createNewAppiumDriver(String userPersona, Platform forPlatform, TestExecutionContext context, String appName, File capabilityFileToUseForDriverCreation) {
        Driver currentDriver;
        try {
            AppiumDriver appiumDriver = startAppiumDriverInstance(userPersona, capabilityFileToUseForDriverCreation.getAbsolutePath());

            String scenarioDirectory = context.getTestStateAsString("scenarioDirectory");
            Integer scenarioRunCount = (Integer) context.getTestState("scenarioRunCount");
            String deviceLogFileName = startDataCapture();
            addDeviceLogFileNameFor(userPersona, forPlatform.name(), deviceLogFileName);

            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform, userPersona, appName, appiumDriver);
            Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
            LOGGER.info("Capabilities for the new appium driver{}", appiumDriverCapabilities);
            appiumDriverCapabilities.getCapabilityNames().forEach(key -> LOGGER.info("\t{}:: {}", key, appiumDriverCapabilities.getCapability(key)));

            Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
        } catch (Exception e) {
            throw new EnvironmentSetupException(String.format("Unable to create Android driver '#%d' for user persona: '%s'", numberOfAppiumDriversUsed, userPersona));
        }
        return currentDriver;
    }

    public static String startDataCapture() {
        TestExecutionContext testExecutionContext = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        Integer scenarioCount = (Integer) testExecutionContext.getTestState(TEST_CONTEXT.EXAMPLE_RUN_COUNT);
        String deviceLogDirectory = testExecutionContext.getTestStateAsString(TEST_CONTEXT.DEVICE_LOGS_DIRECTORY);
        String fileName = String.format("%s-Device-%s-run-%s.log", scenarioCount, numberOfAppiumDriversUsed + 1, AppiumDeviceManager.getAppiumDevice().getUdid());
        if (!Runner.getCloudName().equalsIgnoreCase(NOT_SET)) {
            LOGGER.warn("Skipping logcat capture for cloud devices");
        } else {
            if ("android".equalsIgnoreCase(AppiumDeviceManager.getAppiumDevice().getPlatformName())) {
                try {
                    File logFile = new File(deviceLogDirectory, fileName);
                    fileName = logFile.getAbsolutePath();
                    LOGGER.debug("Capturing device logs here: {}", logFile.getAbsolutePath());

                    // Use try-with-resources for proper closing of PrintStream
                    try (PrintStream logFileStream = new PrintStream(logFile)) {
                        LogEntries logcatOutput = AppiumDriverManager.getDriver().manage().logs().get("logcat");
                        StreamSupport.stream(logcatOutput.spliterator(), false).forEach(logFileStream::println);
                    }

                } catch (FileNotFoundException e) {
                    LOGGER.warn("ERROR in getting logcat. Skipping logcat capture", e);
                }
            }
        }
        return fileName;
    }

    private static void writeServiceConfig() {
        JSONObject serverConfig = CustomCapabilities.getInstance().getCapabilityObjectFromKey("serverConfig");
        try (FileWriter writer = new FileWriter(new File(OsUtils.getUserDirectory() + SERVER_CONFIG_JSON))) {
            writer.write(serverConfig.toString());
            writer.flush();
        } catch (IOException e) {
            ExceptionUtils.getStackTrace(e);
        }
    }

    private static AppiumDriver allocateDeviceAndStartDriver(String scenarioName) {
        AppiumDriver driver = AppiumDriverManager.getDriver();
        if (driver == null || driver.getSessionId() == null) {
            return appiumDriverManager.startAppiumDriverInstance(scenarioName);
        } else {
            return driver;
        }
    }

    @NotNull
    private static Driver setupFirstAppiumDriver(String userPersona, Platform forPlatform, TestExecutionContext testExecutionContext, String appName) {
        Driver currentDriver;

        if (null == appiumServerManager) {
            CustomCapabilities.getInstance();
            writeServiceConfig();
            appiumServerManager = new AppiumServerManager();
            appiumDriverManager = new AppiumDriverManager();
            if (Runner.getCloudName()==NOT_SET) {
                appiumServerManager.startAppiumServer("127.0.0.1"); //Needs to be removed
            } else {
                LOGGER.info("Cloud execution on: {}. No need to start Appium Server", Runner.getCloudName());
            }
        }

        AppiumDriver appiumDriver = allocateDeviceAndStartDriver(testExecutionContext.getTestName());
        String deviceLogFileName = startDataCapture();
        testExecutionContext.addTestState(TEST_CONTEXT.APPIUM_DRIVER, appiumDriver);
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_ID, AppiumDeviceManager.getAppiumDevice().getUdid());
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_INFO, AppiumDeviceManager.getAppiumDevice());
        testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_LOG, deviceLogFileName);

        Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
        if (isCloudExecution()) {
            testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_ON, getCloudName());
        } else {
            testExecutionContext.addTestState(TEST_CONTEXT.DEVICE_ON, "localDevice");
        }
        LOGGER.info("Capabilities for the first appium driver{}", appiumDriverCapabilities);
        Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
        Drivers.addUserPersonaDeviceLogFileName(userPersona, testExecutionContext.getTestStateAsString(TEST_CONTEXT.DEVICE_LOG), forPlatform);
        currentDriver = new Driver(testExecutionContext.getTestName() + "-" + userPersona, forPlatform, userPersona, appName, appiumDriver);
        return currentDriver;
    }

    private static boolean isCloudExecution() {
        return Runner.getCloudName() != null;
    }

    private static void disableNotificationsAndToastsOnDevice(Driver currentDriver, String deviceOn, String udid) {
        if (Runner.isRunningInCI()) {
            LOGGER.debug("Running in CI. No need to disable notifications.");
            //            disableNotificationsForDeviceInDeviceFarm(currentDriver, deviceOn);
        } else {
            disableNotificationsForLocalDevice(udid);
        }
    }

    private static void disableNotificationsForLocalDevice(String udid) {
        String[] disableToastsCommand = new String[]{"adb", "-s", udid, "shell", "appops", "set", Runner.getAppPackageName(), "TOAST_WINDOW", "deny"};
        String[] disableNotificationsCommand = new String[]{"adb", "-s", udid, "shell", "settings", "put", "global", "heads_up_notifications_enabled", "0"};

        CommandLineResponse disableToastsCommandResponse = CommandLineExecutor.execCommand(disableToastsCommand);
        LOGGER.info(String.format("disableToastsCommandResponse: %s", disableToastsCommandResponse));
        CommandLineResponse disableNotificationsCommandResponse = CommandLineExecutor.execCommand(disableNotificationsCommand);
        LOGGER.info(String.format("disableNotificationsCommandResponse: %s", disableNotificationsCommandResponse));
    }

    private static void disableNotificationsForDeviceInDeviceFarm(Driver currentDriver, String deviceOn) {
        if (deviceOn.equalsIgnoreCase("pCloudy")) {
            Object disableToasts = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand", "adb shell appops set " + Runner.getAppPackageName() + " TOAST_WINDOW " + "deny");
            LOGGER.info(String.format("@disableToastsCommandResponse: %s", disableToasts));
            Object disableNotifications = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand", "adb shell settings put global heads_up_notifications_enabled 0");
            LOGGER.info("@disableNotificationsCommandResponse: " + disableNotifications);
        }
    }

    private static boolean isRunningOnpCloudy() {
        boolean isPCloudy = getCloudName().equalsIgnoreCase("pCloudy");
        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
        return isPCloudy;
    }

    private static String getCloudName() {
        return Runner.getCloudName();
    }

    private static boolean isRunningOnBrowserStack() {
        boolean isBrowserStack = getCloudName().equalsIgnoreCase("browserstack");
        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
        return isBrowserStack;
    }

    private static boolean isRunningOnHeadspin() {
        boolean isHeadspin = getCloudName().equalsIgnoreCase("headspin");
        LOGGER.info(AppiumDeviceManager.getAppiumDevice().getUdid() + ": running on: " + getCloudName());
        return isHeadspin;
    }

    static String getCurlProxyCommand() {
        String curlProxyCommand = "";
        if (null != getOverriddenStringValue("PROXY_URL")) {
            curlProxyCommand = " --proxy " + System.getProperty("PROXY_URL");
        }
        return curlProxyCommand;
    }

    private static String getReportLinkFromBrowserStack(String sessionId) {
        String browserStackTestResultUrl = "";
        String cloudUser = getOverriddenStringValue("CLOUD_USERNAME");
        String cloudPassword = getOverriddenStringValue("CLOUD_KEY");
        try {
            String[] curlCommand = new String[]{"curl --in" + "secure " + getCurlProxyCommand() + " -u \"" + cloudUser + ":" + cloudPassword + "\" -X GET \"https://api-cloud.browserstack.com/app-automate/sessions/" + sessionId + ".json\""};
            CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(curlCommand);
            LOGGER.info("Response from BrowserStack - '{}'", commandLineResponse.getStdOut());
            JSONObject pr = new JSONObject(commandLineResponse.getStdOut());
            JSONObject automation_session = pr.getJSONObject("automation_session");
            browserStackTestResultUrl = automation_session.getString("browser_url");
            LOGGER.info("BrowserStack execution link: {}", browserStackTestResultUrl);
        } catch (Exception e) {
            LOGGER.info("Unable to get test execution link from BrowserStack: {}", e.getMessage());
            ExceptionUtils.getStackTrace(e);
        }
        return browserStackTestResultUrl;
    }

    private static void attachCloudExecutionReportLinkToReportPortal(AppiumDriver driver) {
        if (isCloudExecution() && isRunningOnpCloudy()) {
            String link = (String) driver.executeScript("pCloudy_getReportLink");
            String message = "pCloudy Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        } else if (isCloudExecution() && isRunningOnHeadspin()) {
            String sessionId = driver.getSessionId().toString();
            String link = "https://ui-dev.headspin.io/sessions/" + sessionId + "/waterfall";
            String message = "Headspin Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        } else if (isCloudExecution() && isRunningOnBrowserStack()) {
            String sessionId = driver.getSessionId().toString();
            String link = getReportLinkFromBrowserStack(sessionId);
            String message = "BrowserStack Report link available here: " + link;
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        }
    }

    static void closeAppiumDriver(String userPersona, Driver driver) {
        TestExecutionContext testExecutionContext = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        AppiumDriver appiumDriver = (AppiumDriver) testExecutionContext.getTestState("appiumDriver");
        attachCloudExecutionReportLinkToReportPortal(appiumDriver);
        if (Runner.getPlatform().equals(Platform.windows)) {
            closeWindowsAppOnMachine(userPersona, driver);
        } else if (Runner.getPlatform().equals(Platform.android)) {
            closeAndroidAppOnDevice(userPersona, driver);
        } else if (Runner.getPlatform().equals(Platform.iOS)) {
            closeIOSAppOnDevice(userPersona, driver);
        } else {
            throw new InvalidTestDataException(String.format("No implementation for platform: %s", Runner.getPlatform()));
        }
    }

    static void closeWindowsAppOnMachine(String userPersona, @NotNull Driver driver) {
        String logMessage;
        String appPackageName = Runner.getAppPackageName();
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        --numberOfAppiumDriversUsed;
        LOGGER.info(String.format("numberOfAppiumDriversUsed: %d", numberOfAppiumDriversUsed));
        if (null == appiumDriver) {
            logMessage = String.format("Strange. But WindowsDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
        } else {
            logMessage = String.format("Closing WindowsDriver for App '%s' for user '%s'", appPackageName, userPersona);
            LOGGER.info(logMessage);
            // todo - fix for appium 2.0, test terminateApp() on windows app in windows OS
            AndroidDriver androidDriver = (AndroidDriver) appiumDriver;
            androidDriver.terminateApp(appPackageName);
            TestExecutionContext context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
            AppiumDriver atdAppiumDriver = (AppiumDriver) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            if (appiumDriver.equals(atdAppiumDriver)) {
                LOGGER.info(String.format("ATD will quit the driver for persona: '%s'", userPersona));
            } else {
                LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
                appiumDriver.quit();
            }
            logMessage = String.format("App: '%s' terminated", appPackageName);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
        }
    }

    static void closeAndroidAppOnDevice(String userPersona, @NotNull Driver driver) {
        String appPackageName = Runner.getAppPackageName();
        String logMessage;
        --numberOfAppiumDriversUsed;
        LOGGER.info("numberOfAppiumDriversUsed: {}", numberOfAppiumDriversUsed);
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if (null == appiumDriver) {
            logMessage = String.format("Strange. But AppiumDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
        } else {
            LOGGER.info("Quit driver for persona: '{}'", userPersona);
            attachDeviceLogsToReportPortal(userPersona);
            terminateAndroidAppOnDevice(appPackageName, appiumDriver);
        }
    }

    private static void terminateAndroidAppOnDevice(String appPackageName, AppiumDriver appiumDriver) {
        String logMessage;
        ApplicationState applicationState = null;
        try {
            LOGGER.info(String.format("Terminate app: %s", appPackageName));
            AndroidDriver androidDriver = (AndroidDriver) appiumDriver;
            applicationState = androidDriver.queryAppState(appPackageName);

            logMessage = String.format("App: '%s' Application state before closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);

            androidDriver.terminateApp(appPackageName);
            stopAppiumDriver();
            applicationState = androidDriver.queryAppState(appPackageName);
            logMessage = String.format("App: '%s' Application state after closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
        } catch (NoSuchSessionException e) {
            logMessage = e.getMessage();
            LOGGER.info(logMessage);
        }
        ReportPortalLogger.logDebugMessage(logMessage);
    }

    private static void attachDeviceLogsToReportPortal(String userPersona) {
        String deviceLogFileName = getDeviceLogFileNameFor(userPersona, Runner.getPlatform().toString());
        File destinationFile = new File(deviceLogFileName);

        String adbLogMessage = String.format("ADB Logs for %s, file name: %s", Drivers.getNameOfDeviceUsedByUser(userPersona), destinationFile.getName());
        LOGGER.info(adbLogMessage);
        ReportPortalLogger.attachFileInReportPortal(adbLogMessage, destinationFile);
    }

    private static String getDeviceLogFileNameFor(String userPersona, String forPlatform) {
        UserPersonaDetails userPersonaDetails = Drivers.getUserPersonaDetails(Runner.getTestExecutionContext(Thread.currentThread().getId()));
        return userPersonaDetails.getDeviceLogFileNameFor(userPersona, forPlatform);
    }

    private static void addDeviceLogFileNameFor(String userPersona, String forPlatform, String deviceLogFileName) {
        UserPersonaDetails userPersonaDetails = Drivers.getUserPersonaDetails(Runner.getTestExecutionContext(Thread.currentThread().getId()));
        userPersonaDetails.addDeviceLogFileNameFor(userPersona, forPlatform, deviceLogFileName);
    }

    @NotNull
    static Driver createWindowsDriverForUser(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createWindowsDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of " + "webdrivers: '%d'%n", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));

        if (numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: " + "'%s'", numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }
        return createWindowsDriver(userPersona, forPlatform, context);
    }

    @NotNull
    private static Driver createWindowsDriver(String userPersona, Platform forPlatform, TestExecutionContext context) {
        Driver currentDriver;
        if (numberOfAppiumDriversUsed < MAX_NUMBER_OF_APPIUM_DRIVERS) {
            AppiumDriver windowsDriver = (AppiumDriver) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            String appName = Drivers.getAppNamefor(userPersona);

            String runningOn = Runner.isRunningInCI() ? "CI" : "local";
            context.addTestState(TEST_CONTEXT.WINDOWS_DEVICE_ON, runningOn);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform, userPersona, appName, windowsDriver);
            Capabilities windowsDriverCapabilities = windowsDriver.getCapabilities();
            LOGGER.info(CAPABILITIES + windowsDriverCapabilities);
            Drivers.addUserPersonaDriverCapabilities(userPersona, windowsDriverCapabilities);
            LOGGER.info("deviceLog for windows driver: {}", context.getTestStateAsString(TEST_CONTEXT.DEVICE_LOG));
            Drivers.addUserPersonaDeviceLogFileName(userPersona, context.getTestStateAsString(TEST_CONTEXT.DEVICE_LOG), forPlatform);
        } else {
            throw new InvalidTestDataException(String.format("Current number of WindowsDriver instances used: '%d'. " + "Unable to create " + "more than '%d' drivers for user persona: '%s' " + "on platform: '%s'", numberOfAppiumDriversUsed, MAX_NUMBER_OF_APPIUM_DRIVERS, userPersona, forPlatform.name()));
        }
        numberOfAppiumDriversUsed++;
        LOGGER.info(String.format("createWindowsDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "windowsDrivers: '%d'", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        return currentDriver;
    }

    public static Driver createIOSDriverForUser(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createIOSDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        Driver currentDriver;
        if (numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: " + "'%s'", numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }

        currentDriver = setupOrCreateIOSDriver(userPersona, forPlatform, context);
        LOGGER.info(String.format("createIOSDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n", userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver, context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON), (String) Drivers.getCapabilitiesFor(userPersona).getCapability("udid"));
        return currentDriver;
    }

    private static Driver setupOrCreateIOSDriver(String userPersona, Platform forPlatform, TestExecutionContext context) {
        String appName = Drivers.getAppNamefor(userPersona);
        File capabilityFileToUseForDriverCreation = getCapabilityFileToUseForDriverCreation(appName);
        Driver currentDriver;
        if (numberOfAppiumDriversUsed == 0) {
            currentDriver = setupFirstAppiumDriver(userPersona, forPlatform, context, appName);
        } else {
            currentDriver = createNewAppiumDriver(userPersona, forPlatform, context, appName, capabilityFileToUseForDriverCreation);
        }
        numberOfAppiumDriversUsed++;
        return currentDriver;
    }

    static void closeIOSAppOnDevice(String userPersona, @NotNull Driver driver) {
        String appBundleId = Runner.getAppPackageName();
        String logMessage;
        --numberOfAppiumDriversUsed;
        LOGGER.info(String.format("numberOfAppiumDriversUsed: %d", numberOfAppiumDriversUsed));
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();

        if (null == appiumDriver) {
            logMessage = String.format("Strange. But AppiumDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
        } else {
            TestExecutionContext context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
            AppiumDriver iOSAppiumDriver = (AppiumDriver) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);

            if (appiumDriver.equals(iOSAppiumDriver)) {
                LOGGER.info(String.format("Appium will quit the driver for persona: '%s'", userPersona));
                LOGGER.info("Close the app");
                IOSDriver iosDriver = (IOSDriver) appiumDriver;
                iosDriver.terminateApp(appBundleId);
            } else {
                quitDriver(appiumDriver, userPersona);
                attachDeviceLogsToReportPortal(userPersona);
                terminateIOSAppOnDevice(appBundleId, appiumDriver);
            }
        }
    }

    private static void quitDriver(AppiumDriver appiumDriver, String userPersona) {
        LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
    }

    private static void terminateIOSAppOnDevice(String appPackageName, AppiumDriver appiumDriver) {
        String logMessage;
        ApplicationState applicationState = null;
        try {
            LOGGER.info(String.format("Terminate app: %s", appPackageName));
            IOSDriver iOSDriver = (IOSDriver) appiumDriver;
            applicationState = iOSDriver.queryAppState(appPackageName);

            logMessage = String.format("App: '%s' Application state before closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);

            iOSDriver.terminateApp(appPackageName);
            applicationState = iOSDriver.queryAppState(appPackageName);
            logMessage = String.format("App: '%s' Application state after closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
        } catch (NoSuchSessionException e) {
            logMessage = e.getMessage();
            LOGGER.info(logMessage);
        }
        ReportPortalLogger.logDebugMessage(logMessage);
    }

    public static AppiumDriver getDriver() {
        return appiumDriver.get();
    }

    private static void setDriver(AppiumDriver driver) {
        String allCapabilities = driver.getCapabilities().getCapabilityNames().stream().map(key -> String.format("%n\t%s:: %s", key, driver.getCapabilities().getCapability(key))).collect(Collectors.joining(""));
        LOGGER.info(String.format("AppiumDriverManager: Created AppiumDriver with capabilities: %s", allCapabilities));
        appiumDriver.set(driver);
    }

    private static AppiumDriver initialiseDriver(DesiredCapabilities desiredCapabilities) {
        String allCapabilities = desiredCapabilities.getCapabilityNames().stream().map(key -> String.format("%n\t%s:: %s", key, desiredCapabilities.getCapability(key))).collect(Collectors.joining(""));

        LOGGER.info("Initialise Driver with Capabilities: {}", allCapabilities);
        AppiumServerManager appiumServerManager = new AppiumServerManager();
        String remoteWDHubIP = appiumServerManager.getRemoteWDHubIP();
        return createAppiumDriver(desiredCapabilities, remoteWDHubIP);
    }

    private static AppiumDriver createAppiumDriver(DesiredCapabilities desiredCapabilities, String remoteWDHubIP) {
        AppiumDriver currentDriverSession;
        Platform platform = Runner.getPlatform();
        URL remoteUrl = null;
        try {
            remoteUrl = new URL(remoteWDHubIP);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        currentDriverSession = switch (platform) {
            case android -> new AndroidDriver(remoteUrl, desiredCapabilities);
            case iOS -> new IOSDriver(remoteUrl, desiredCapabilities);
            case windows -> new WindowsDriver(remoteUrl, desiredCapabilities);
            default -> throw new IllegalStateException("Unexpected value: " + platform.name());
        };
        Capabilities currentDriverSessionCapabilities = currentDriverSession.getCapabilities();
        LOGGER.info("Session Created for " + platform.name() + "\n\tSession Id: " + currentDriverSession.getSessionId() + "\n\tUDID: " + currentDriverSessionCapabilities.getCapability("udid"));
        String json = new Gson().toJson(currentDriverSessionCapabilities.asMap());
        DriverSession driverSessions = null;
        try {
            driverSessions = (new ObjectMapper().readValue(json, DriverSession.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        AppiumDeviceManager.setDevice(driverSessions);
        return currentDriverSession;
    }

    public AppiumDriver startAppiumDriverInstance(String testMethodName) {
        return startAppiumDriverInstance(testMethodName, buildDesiredCapabilities(ConfigFileManager.CAPS.get()));
    }

    private static AppiumDriver startAppiumDriverInstance(String testMethodName, String capabilityFilePath) {
        return startAppiumDriverInstance(testMethodName, buildDesiredCapabilities(capabilityFilePath));
    }

    private static AppiumDriver startAppiumDriverInstance(String testMethodName, DesiredCapabilities desiredCapabilities) {
        LOGGER.info("startAppiumDriverInstance for {} using capability file: {}", testMethodName, ConfigFileManager.CAPS.get());
        AppiumDriver currentDriverSession = initialiseDriver(desiredCapabilities);
        setDriver(currentDriverSession);
        return currentDriverSession;
    }

    private static DesiredCapabilities buildDesiredCapabilities(String capabilityFilePath) {
        if (new File(capabilityFilePath).exists()) {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilityBuilder().buildDesiredCapability(capabilityFilePath, numberOfAppiumDriversUsed);
//            desiredCapabilities = BrowserStackSetup.updateBrowserStackCapabilities()
//            bstackOptions.put("sessionName", Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName());
            return desiredCapabilities;
        } else {
            throw new RuntimeException("Capability file not found");
        }
    }

    private static void stopAppiumDriver() {
        if (getDriver() != null && getDriver().getSessionId() != null) {
            LOGGER.info("Session Deleting ---- " + getDriver().getSessionId() + "---" + getDriver().getCapabilities().getCapability("udid"));
            getDriver().quit();
        }
    }
}
