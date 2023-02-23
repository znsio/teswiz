package com.znsio.e2e.runner;

import com.appium.manager.AppiumDevice;
import com.appium.manager.DeviceAllocationManager;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.github.device.Device;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.znsio.e2e.runner.Runner.DEBUG;
import static com.znsio.e2e.runner.Runner.DEFAULT;
import static com.znsio.e2e.runner.Setup.CAPS;

public class AppiumDriverManager {
    private static final int MAX_NUMBER_OF_APPIUM_DRIVERS = Runner.getMaxNumberOfAppiumDrivers();
    private static final List<AppiumDevice> additionalDevices = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(AppiumDriverManager.class.getName());
    private static int numberOfAppiumDriversUsed = 0;

    @NotNull
    static Driver createAndroidDriverForUser(String userPersona, Platform forPlatform,
                                             TestExecutionContext context) {
        LOGGER.info(String.format(
                "createAndroidDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of "
                + "appiumDrivers: '%d'%n",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        Driver currentDriver;
        if(Platform.android.equals(
                forPlatform) && numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }

        String capabilityFileNameToUseForDriverCreation = System.getProperty(CAPS);

        String appName = Drivers.getAppNamefor(userPersona);
        if(!appName.equalsIgnoreCase(DEFAULT)) {
            String capabilityFileDirectory = new File(
                    capabilityFileNameToUseForDriverCreation).getParent();
            capabilityFileNameToUseForDriverCreation =
                    capabilityFileDirectory + File.separator + (appName + "_capabilities.json");
        }
        File capabilityFileToUseForDriverCreation = new File(
                capabilityFileNameToUseForDriverCreation);
        LOGGER.info(
                "capabilityFileToUseForDriverCreation: " + capabilityFileToUseForDriverCreation.getAbsolutePath());
        LOGGER.info(
                "capabilityFileToUseForDriverCreation.exists(): " + capabilityFileToUseForDriverCreation.exists());

        if(numberOfAppiumDriversUsed == 0) {
            AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) context.getTestState(
                    TEST_CONTEXT.APPIUM_DRIVER);
            AppiumDevice deviceInfo = (AppiumDevice) context.getTestState(TEST_CONTEXT.DEVICE_INFO);
            // Do not add the device info to additionalDevices for the driver created by ATD
            // additionalDevices.add(deviceInfo);
            Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
            context.addTestState(TEST_CONTEXT.DEVICE_ON, deviceInfo.getDeviceOn());
            LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
            Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                       deviceInfo.getDeviceOn(), userPersona, appName,
                                       appiumDriver);
        } else {
            try {
                AppiumDriver appiumDriver = allocateNewDeviceAndStartAppiumDriver(context,
                                                                                  capabilityFileToUseForDriverCreation.getAbsolutePath());
                currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                           context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON),
                                           userPersona, appName, appiumDriver);
                Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
                LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
                appiumDriverCapabilities.getCapabilityNames().forEach(key -> LOGGER.info(
                        "\t" + key + ":: " + appiumDriverCapabilities.getCapability(key)));

                Drivers.addUserPersonaDriverCapabilities(userPersona, appiumDriverCapabilities);
            } catch(Exception e) {
                throw new EnvironmentSetupException(String.format(
                        "Unable to create Android driver '#%d' for user persona: '%s'",
                        numberOfAppiumDriversUsed, userPersona));
            }
        }
        numberOfAppiumDriversUsed++;
        LOGGER.info(String.format(
                "createAndroidDriverForUser: done: userPersona: '%s', Platform: '%s', Number of " + "appiumDrivers: '%d'%n",
                userPersona, forPlatform.name(), numberOfAppiumDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver,
                                              context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON),
                                              (String) Drivers.getCapabilitiesFor(userPersona)
                                                              .getCapability("udid"));
        return currentDriver;
    }

    private static AppiumDriver allocateNewDeviceAndStartAppiumDriver(TestExecutionContext context,
                                                                      String capabilityFile) {
        try {
            String testName = context.getTestName();
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
            try {
                String deviceLogFileName = availableDevice.startDataCapture(normalisedScenarioName,
                                                                            scenarioRunCount);
                LOGGER.info("Started device log capture in file: " + deviceLogFileName);
            } catch(IOException | InterruptedException e) {
                LOGGER.info("Error in starting data capture: " + e.getMessage());
                e.printStackTrace();
            }
            ReportPortal.emitLog(
                    "allocateNewDeviceAndStartAppiumDriver: Device Info\n" + availableDevice,
                    DEBUG, new Date());
            return driver;
        } catch(Exception e) {
            LOGGER.info(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    private static void disableNotificationsAndToastsOnDevice(Driver currentDriver, String deviceOn,
                                                              String udid) {
        if(Runner.isRunningInCI()) {
            if(deviceOn.equalsIgnoreCase("pCloudy")) {
                Object disableToasts =
                        ((AppiumDriver<?>) currentDriver.getInnerDriver()).executeScript(
                        "pCloudy_executeAdbCommand",
                        "adb shell appops set " + Runner.getAppPackageName() + " TOAST_WINDOW " + "deny");
                LOGGER.info("@disableToastsCommandResponse: " + disableToasts);
                Object disableNotifications =
                        ((AppiumDriver<?>) currentDriver.getInnerDriver()).executeScript(
                        "pCloudy_executeAdbCommand",
                        "adb shell settings put global heads_up_notifications_enabled 0");
                LOGGER.info("@disableNotificationsCommandResponse: " + disableNotifications);
            }
        } else {
            String[] disableToastsCommand = new String[]{"adb", "-s", udid, "shell", "appops",
                                                         "set", Runner.getAppPackageName(),
                                                         "TOAST_WINDOW", "deny"};
            String[] disableNotificationsCommand = new String[]{"adb", "-s", udid, "shell",
                                                                "settings", "put", "global",
                                                                "heads_up_notifications_enabled",
                                                                "0"};

            CommandLineResponse disableToastsCommandResponse = CommandLineExecutor.execCommand(
                    disableToastsCommand);
            LOGGER.info("disableToastsCommandResponse: " + disableToastsCommandResponse);
            CommandLineResponse disableNotificationsCommandResponse =
                    CommandLineExecutor.execCommand(
                    disableNotificationsCommand);
            LOGGER.info(
                    "disableNotificationsCommandResponse: " + disableNotificationsCommandResponse);
        }
    }

    private static AppiumDevice updateAvailableDeviceInformation(AppiumDevice availableDevice) {
        Capabilities capabilities = com.appium.manager.AppiumDriverManager.getDriver()
                                                                          .getCapabilities();
        LOGGER.info("allocateDeviceAndStartDriver: " + capabilities);

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
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if(null == appiumDriver) {
            logMessage = String.format("Strange. But AppiumDriver for user '%s' already closed",
                                       userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            LOGGER.info("Terminate app: " + appPackageName);
            ApplicationState applicationState = appiumDriver.queryAppState(appPackageName);

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
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        }
    }

    static void freeDevices() {
        for(AppiumDevice additionalDevice : additionalDevices) {
            LOGGER.info("Freeing device: " + additionalDevice.getDevice().getName());
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

        Driver currentDriver;
        if(Platform.windows.equals(
                forPlatform) && numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }
        if(numberOfAppiumDriversUsed < MAX_NUMBER_OF_APPIUM_DRIVERS) {
            AppiumDriver<WebElement> windowsDriver =
                    (AppiumDriver<WebElement>) context.getTestState(
                    TEST_CONTEXT.APPIUM_DRIVER);
            String appName = Drivers.getAppNamefor(userPersona);

            String runningOn = Runner.isRunningInCI() ? "CI" : "local";
            context.addTestState(TEST_CONTEXT.WINDOWS_DEVICE_ON, runningOn);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, forPlatform,
                                       userPersona, appName, runningOn, windowsDriver);
            Capabilities windowsDriverCapabilities = windowsDriver.getCapabilities();
            LOGGER.info("CAPABILITIES: " + windowsDriverCapabilities);
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
