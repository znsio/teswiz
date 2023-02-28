package com.znsio.teswiz.runner;

import com.appium.manager.AppiumDevice;
import com.appium.manager.DeviceAllocationManager;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.github.device.Device;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.DriverCreationException;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.znsio.teswiz.runner.Runner.DEBUG;
import static com.znsio.teswiz.runner.Runner.DEFAULT;
import static com.znsio.teswiz.runner.Setup.CAPS;

public class AppiumDriverManager {
    private static final int MAX_NUMBER_OF_APPIUM_DRIVERS = Runner.getMaxNumberOfAppiumDrivers();
    private static final List<AppiumDevice> additionalDevices = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(AppiumDriverManager.class.getName());
    private static final String CAPABILITIES = "CAPABILITIES: ";
    private static int numberOfAppiumDriversUsed = 0;

    private AppiumDriverManager() {
        LOGGER.debug("AppiumDriverManager - private constructor");
    }

    @NotNull
    static Driver createAndroidDriverForUser(String userPersona, Platform forPlatform,
                                             TestExecutionContext context) {
        LOGGER.info(String.format(
                "createAndroidDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of "
                + "appiumDrivers: '%d'%n",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        Driver currentDriver;
        if(numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }

        currentDriver = setupOrCreateAndroidDriver(userPersona, forPlatform, context);
        LOGGER.info(String.format(
                "createAndroidDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver,
                                              context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON),
                                              (String) Drivers.getCapabilitiesFor(userPersona)
                                                              .getCapability("udid"));
        return currentDriver;
    }

    @NotNull
    private static Driver setupOrCreateAndroidDriver(String userPersona, Platform forPlatform,
                                                     TestExecutionContext context) {
        String appName = Drivers.getAppNamefor(userPersona);
        File capabilityFileToUseForDriverCreation = getCapabilityFileToUseForDriverCreation(
                appName);
        Driver currentDriver;
        if(numberOfAppiumDriversUsed == 0) {
            currentDriver = setupFirstAppiumDriver(userPersona, forPlatform, context, appName);
        } else {
            currentDriver = createNewAppiumDriver(userPersona, forPlatform, context, appName,
                                                  capabilityFileToUseForDriverCreation);
        }
        numberOfAppiumDriversUsed++;
        return currentDriver;
    }

    @NotNull
    private static File getCapabilityFileToUseForDriverCreation(String appName) {
        String capabilityFileNameToUseForDriverCreation = System.getProperty(CAPS);

        if(!appName.equalsIgnoreCase(DEFAULT)) {
            String capabilityFileDirectory = new File(
                    capabilityFileNameToUseForDriverCreation).getParent();
            capabilityFileNameToUseForDriverCreation =
                    capabilityFileDirectory + File.separator + (appName + "_capabilities.json");
        }
        File capabilityFileToUseForDriverCreation = new File(
                capabilityFileNameToUseForDriverCreation);
        LOGGER.info(String.format("capabilityFileToUseForDriverCreation: %s",
                                  capabilityFileToUseForDriverCreation.getAbsolutePath()));
        return capabilityFileToUseForDriverCreation;
    }

    @NotNull
    private static Driver createNewAppiumDriver(String userPersona, Platform forPlatform,
                                                TestExecutionContext context, String appName,
                                                File capabilityFileToUseForDriverCreation) {
        Driver currentDriver;
        try {
            AppiumDriver appiumDriver = allocateNewDeviceAndStartAppiumDriver(userPersona, context,
                                                                              capabilityFileToUseForDriverCreation.getAbsolutePath());
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                       userPersona, appName, appiumDriver);
            Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
            LOGGER.info(CAPABILITIES + appiumDriverCapabilities);
            appiumDriverCapabilities.getCapabilityNames().forEach(key -> LOGGER.info(
                    String.format("\t%s:: %s", key, appiumDriverCapabilities.getCapability(key))));

            Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
        } catch(Exception e) {
            throw new EnvironmentSetupException(
                    String.format("Unable to create Android driver '#%d' for user persona: '%s'",
                                  numberOfAppiumDriversUsed, userPersona));
        }
        return currentDriver;
    }

    @NotNull
    private static Driver setupFirstAppiumDriver(String userPersona, Platform forPlatform,
                                                 TestExecutionContext context, String appName) {
        Driver currentDriver;
        AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) context.getTestState(
                TEST_CONTEXT.APPIUM_DRIVER);
        AppiumDevice deviceInfo = (AppiumDevice) context.getTestState(TEST_CONTEXT.DEVICE_INFO);
        // Do not add the device info to additionalDevices for the driver created by ATD
        // additionalDevices.add(deviceInfo);
        Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
        context.addTestState(TEST_CONTEXT.DEVICE_ON, deviceInfo.getDeviceOn());
        LOGGER.info(CAPABILITIES + appiumDriverCapabilities);
        Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
        currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                   userPersona, appName, appiumDriver);
        return currentDriver;
    }

    private static AppiumDriver allocateNewDeviceAndStartAppiumDriver(String userPersona,
                                                                      TestExecutionContext context,
                                                                      String capabilityFile) {
        String testName = context.getTestName();
        try {
            String normalisedScenarioName = context.getTestStateAsString(
                    TEST_CONTEXT.NORMALISED_SCENARIO_NAME);
            Integer scenarioRunCount = (Integer) context.getTestState(
                    TEST_CONTEXT.SCENARIO_RUN_COUNT);
            DeviceAllocationManager deviceAllocationManager = DeviceAllocationManager.getInstance();
            AppiumDevice availableDevice = deviceAllocationManager.getNextAvailableDevice();
            deviceAllocationManager.allocateDevice(availableDevice);
            AppiumDriver driver =
                    new com.appium.manager.AppiumDriverManager().startAppiumDriverInstance(
                    testName, capabilityFile);
            additionalDevices.add(availableDevice);
            updateAvailableDeviceInformation(availableDevice);
            startDataCaptureFromDevice(userPersona, normalisedScenarioName, scenarioRunCount,
                                       availableDevice);
            ReportPortal.emitLog(
                    String.format("allocateNewDeviceAndStartAppiumDriver: Device Info%n%s",
                                  availableDevice), DEBUG, new Date());
            return driver;
        } catch(Exception e) {
            LOGGER.info(ExceptionUtils.getStackTrace(e));
            throw new DriverCreationException(
                    String.format("Unable to create/allocate driver for test: '%s'", testName), e);
        }
    }

    private static void startDataCaptureFromDevice(String userPersona,
                                                   String normalisedScenarioName,
                                                   Integer scenarioRunCount,
                                                   AppiumDevice availableDevice) {
        try {
            String deviceLogFileName = availableDevice.startDataCapture(normalisedScenarioName,
                                                                        scenarioRunCount);
            Drivers.addDeviceLogFileNameFor(userPersona, Platform.android.name(),
                                            deviceLogFileName);
            LOGGER.info(String.format("Started device log capture in file: %s", deviceLogFileName));
        } catch(IOException | InterruptedException e) {
            LOGGER.info(String.format("Error in starting data capture: %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    private static void disableNotificationsAndToastsOnDevice(Driver currentDriver, String deviceOn,
                                                              String udid) {
        if(Runner.isRunningInCI()) {
            disableNotificationsForDeviceInDeviceFarm(currentDriver, deviceOn);
        } else {
            disableNotificationsForLocalDevice(udid);
        }
    }

    private static void disableNotificationsForLocalDevice(String udid) {
        String[] disableToastsCommand = new String[]{"adb", "-s", udid, "shell", "appops", "set",
                                                     Runner.getAppPackageName(), "TOAST_WINDOW",
                                                     "deny"};
        String[] disableNotificationsCommand = new String[]{"adb", "-s", udid, "shell", "settings",
                                                            "put", "global",
                                                            "heads_up_notifications_enabled", "0"};

        CommandLineResponse disableToastsCommandResponse = CommandLineExecutor.execCommand(
                disableToastsCommand);
        LOGGER.info(
                String.format("disableToastsCommandResponse: %s", disableToastsCommandResponse));
        CommandLineResponse disableNotificationsCommandResponse = CommandLineExecutor.execCommand(
                disableNotificationsCommand);
        LOGGER.info(String.format("disableNotificationsCommandResponse: %s",
                                  disableNotificationsCommandResponse));
    }

    private static void disableNotificationsForDeviceInDeviceFarm(Driver currentDriver,
                                                                  String deviceOn) {
        if(deviceOn.equalsIgnoreCase("pCloudy")) {
            Object disableToasts = ((AppiumDriver<?>) currentDriver.getInnerDriver()).executeScript(
                    "pCloudy_executeAdbCommand",
                    "adb shell appops set " + Runner.getAppPackageName() + " TOAST_WINDOW " +
                    "deny");
            LOGGER.info(String.format("@disableToastsCommandResponse: %s", disableToasts));
            Object disableNotifications =
                    ((AppiumDriver<?>) currentDriver.getInnerDriver()).executeScript(
                    "pCloudy_executeAdbCommand",
                    "adb shell settings put global heads_up_notifications_enabled 0");
            LOGGER.info("@disableNotificationsCommandResponse: " + disableNotifications);
        }
    }

    private static AppiumDevice updateAvailableDeviceInformation(AppiumDevice availableDevice) {
        Capabilities capabilities = com.appium.manager.AppiumDriverManager.getDriver()
                                                                          .getCapabilities();
        LOGGER.info(String.format("allocateDeviceAndStartDriver: %s", capabilities));

        String udid = capabilities.is("udid") ? Drivers.getCapabilityFor(capabilities, "udid")
                                              : Drivers.getCapabilityFor(capabilities,
                                                                         "deviceUDID");
        Device device = availableDevice.getDevice();
        device.setUdid(udid);
        device.setDeviceManufacturer(Drivers.getCapabilityFor(capabilities, "deviceManufacturer"));
        device.setDeviceModel(Drivers.getCapabilityFor(capabilities, "deviceModel"));
        device.setName(Drivers.getCapabilityFor(capabilities,
                                                "deviceName") + " " + Drivers.getCapabilityFor(
                capabilities, "deviceModel"));
        device.setApiLevel(Drivers.getCapabilityFor(capabilities, "deviceApiLevel"));
        device.setDeviceType(Drivers.getCapabilityFor(capabilities, "platformName"));
        device.setScreenSize(Drivers.getCapabilityFor(capabilities, "deviceScreenSize"));
        return availableDevice;
    }

    static void closeAppiumDriver(String userPersona, Driver driver) {
        if(Runner.getPlatform().equals(Platform.windows)) {
            closeWindowsAppOnMachine(userPersona, driver);
        } else {
            closeAndroidAppOnDevice(userPersona, driver);
        }
    }

    static void closeWindowsAppOnMachine(String userPersona,
                                         @NotNull
                                         Driver driver) {
        String logMessage;
        String appPackageName = Runner.getAppPackageName();
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        --numberOfAppiumDriversUsed;
        LOGGER.info(String.format("numberOfAppiumDriversUsed: %d", numberOfAppiumDriversUsed));
        if(null == appiumDriver) {
            logMessage = String.format("Strange. But WindowsDriver for user '%s' already closed",
                                       userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            logMessage = String.format("Closing WindowsDriver for App '%s' for user '%s'",
                                       appPackageName, userPersona);
            LOGGER.info(logMessage);
            appiumDriver.closeApp();
            TestExecutionContext context = SessionContext.getTestExecutionContext(
                    Thread.currentThread().getId());
            AppiumDriver<WebElement> atdAppiumDriver =
                    (AppiumDriver<WebElement>) context.getTestState(
                    TEST_CONTEXT.APPIUM_DRIVER);
            if(appiumDriver.equals(atdAppiumDriver)) {
                LOGGER.info(
                        String.format("ATD will quit the driver for persona: '%s'", userPersona));
            } else {
                LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
                appiumDriver.quit();
            }
            logMessage = String.format("App: '%s' terminated", appPackageName);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        }
    }

    static void closeAndroidAppOnDevice(String userPersona,
                                        @NotNull
                                        Driver driver) {
        String appPackageName = Runner.getAppPackageName();
        String logMessage;
        --numberOfAppiumDriversUsed;
        LOGGER.info(String.format("numberOfAppiumDriversUsed: %d", numberOfAppiumDriversUsed));
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if(null == appiumDriver) {
            logMessage = String.format("Strange. But AppiumDriver for user '%s' already closed",
                                       userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            TestExecutionContext context = SessionContext.getTestExecutionContext(
                    Thread.currentThread().getId());
            AppiumDriver<WebElement> atdAppiumDriver =
                    (AppiumDriver<WebElement>) context.getTestState(
                    TEST_CONTEXT.APPIUM_DRIVER);
            if(appiumDriver.equals(atdAppiumDriver)) {
                LOGGER.info(
                        String.format("ATD will quit the driver for persona: '%s'", userPersona));
            } else {
                LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
                attachDeviceLogsToReportPortal(userPersona);
                terminateAndroidAppOnDevice(appPackageName, appiumDriver);
            }
        }
    }

    private static void terminateAndroidAppOnDevice(String appPackageName,
                                                    AppiumDriver appiumDriver) {
        String logMessage;
        ApplicationState applicationState = null;
        try {
            LOGGER.info(String.format("Terminate app: %s", appPackageName));
            applicationState = appiumDriver.queryAppState(appPackageName);

            logMessage = String.format("App: '%s' Application state before closing app: '%s'%n",
                                       appPackageName, applicationState);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
            appiumDriver.closeApp();
            appiumDriver.terminateApp(appPackageName);
            applicationState = appiumDriver.queryAppState(appPackageName);
            logMessage = String.format("App: '%s' Application state after closing app: '%s'%n",
                                       appPackageName, applicationState);
            LOGGER.info(logMessage);
        } catch(NoSuchSessionException e) {
            logMessage = e.getMessage();
            LOGGER.info(logMessage);
        }
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
    }

    private static void attachDeviceLogsToReportPortal(String userPersona) {
        String deviceLogFileName = Drivers.getDeviceLogFileNameFor(userPersona,
                                                                   Platform.android.name());

        String adbLogMessage = String.format("ADB Logs for %s, file name: %s",
                                             Drivers.getDeviceNameForUser(userPersona),
                                             deviceLogFileName);
        ReportPortal.emitLog(adbLogMessage, "DEBUG", new Date(), new File(deviceLogFileName));
    }

    static void freeDevices() {
        for(AppiumDevice additionalDevice : additionalDevices) {
            LOGGER.info(
                    String.format("Freeing device: %s", additionalDevice.getDevice().getName()));
            additionalDevice.freeDevice();
            additionalDevice.setChromeDriverPort(0);
        }
    }

    @NotNull
    static Driver createWindowsDriverForUser(String userPersona, Platform forPlatform,
                                             TestExecutionContext context) {
        LOGGER.info(String.format(
                "createWindowsDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of "
                + "webdrivers: '%d'%n",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));

        if(numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }
        return createWindowsDriver(userPersona, forPlatform, context);
    }

    @NotNull
    private static Driver createWindowsDriver(String userPersona, Platform forPlatform,
                                              TestExecutionContext context) {
        Driver currentDriver;
        if(numberOfAppiumDriversUsed < MAX_NUMBER_OF_APPIUM_DRIVERS) {
            AppiumDriver<WebElement> windowsDriver =
                    (AppiumDriver<WebElement>) context.getTestState(
                    TEST_CONTEXT.APPIUM_DRIVER);
            String appName = Drivers.getAppNamefor(userPersona);

            String runningOn = Runner.isRunningInCI() ? "CI" : "local";
            context.addTestState(TEST_CONTEXT.WINDOWS_DEVICE_ON, runningOn);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                       userPersona, appName, windowsDriver);
            Capabilities windowsDriverCapabilities = windowsDriver.getCapabilities();
            LOGGER.info(CAPABILITIES + windowsDriverCapabilities);
            Drivers.addUserPersonaDriverCapabilities(userPersona, windowsDriverCapabilities);
        } else {
            throw new InvalidTestDataException(String.format(
                    "Current number of WindowsDriver instances used: '%d'. " + "Unable to create "
                    + "more than '%d' drivers for user persona: '%s' " + "on platform: '%s'",
                    numberOfAppiumDriversUsed, MAX_NUMBER_OF_APPIUM_DRIVERS, userPersona,
                    forPlatform.name()));
        }
        numberOfAppiumDriversUsed++;
        LOGGER.info(String.format(
                "createWindowsDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "windowsDrivers: '%d'",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        return currentDriver;
    }
}
