package com.znsio.teswiz.runner;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.znsio.teswiz.runner.Runner.DEFAULT;
import static io.appium.java_client.remote.MobileCapabilityType.DEVICE_NAME;

public class Drivers {
    private static final Map<String, Capabilities> userPersonaDriverCapabilities = new HashMap<>();
    private static final Map<String, String> userPersonaApps = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Drivers.class.getName());
    private static final Map<String, Driver> userPersonaDrivers = new HashMap<>();
    private static final Map<String, Platform> userPersonaPlatforms = new HashMap<>();

    private Drivers() {
        LOGGER.debug("Drivers - private constructor");
    }

    public static Driver setDriverFor(String userPersona, Platform forPlatform,
                                      TestExecutionContext context) {
        LOGGER.info(
                String.format("setDriverFor: start: userPersona: '%s', Platform: '%s'", userPersona,
                              forPlatform.name()));
        if(!userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format(
                    "ERROR: Driver for user persona: '%s' DOES NOT EXIST%nAvailable drivers: '%s'",
                    userPersona, userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }
        Driver currentDriver = userPersonaDrivers.get(userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, forPlatform);
        return currentDriver;
    }

    public static Driver createDriverFor(String userPersona, Platform forPlatform,
                                         TestExecutionContext context) {
        return createDriverFor(userPersona, DEFAULT, Runner.getBrowser(), forPlatform, context);
    }

    public static Driver createDriverFor(String userPersona, String appName, String browserName,
                                         Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createDriverFor: start: userPersona: '%s', Platform: '%s'",
                                  userPersona, forPlatform.name()));
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, forPlatform);
        userPersonaApps.put(userPersona, appName);
        userPersonaPlatforms.put(userPersona, forPlatform);

        Driver currentDriver;
        if(userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format(
                    "ERROR: Driver for user persona: '%s' ALREADY EXISTS%nAvailable drivers: '%s'",
                    userPersona, userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }

        switch(forPlatform) {
            case android:
                currentDriver = AppiumDriverManager.createAndroidDriverForUser(userPersona,
                                                                               forPlatform,
                                                                               context);
                break;
            case web:
                currentDriver = BrowserDriverManager.createWebDriverForUser(userPersona,
                                                                            browserName,
                                                                            forPlatform, context);
                break;
            case windows:
                currentDriver = AppiumDriverManager.createWindowsDriverForUser(userPersona,
                                                                               forPlatform,
                                                                               context);
                break;
            default:
                throw new InvalidTestDataException(String.format(
                        "Unexpected platform value: '%s' provided to assign Driver for user: " +
                        "'%s': ",
                        forPlatform, userPersona));
        }
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        userPersonaDrivers.put(userPersona, currentDriver);
        LOGGER.info(String.format("createDriverFor: done: userPersona: '%s', Platform: '%s'%n",
                                  userPersona, forPlatform.name()));

        return currentDriver;
    }

    static String getCapabilityFor(org.openqa.selenium.Capabilities capabilities, String name) {
        Object capability = capabilities.getCapability(name);
        return null == capability ? "" : capability.toString();
    }

    public static Driver createDriverFor(String userPersona, String appName, Platform forPlatform,
                                         TestExecutionContext context) {
        return createDriverFor(userPersona, appName, Runner.getBrowser(), forPlatform, context);
    }

    public static Driver getDriverForUser(String userPersona) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info(
                    "getDriverForUser: Drivers available for userPersonas: " + userPersonaDrivers.keySet());
            throw new InvalidTestDataException(
                    String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaDrivers.get(userPersona);
    }

    public static String getDeviceNameForUser(String userPersona) {
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String deviceName = (String) userPersonaCapabilities.getCapability(DEVICE_NAME);
        if(null == deviceName) {
            LOGGER.info(
                    "getDeviceNameForUser: Capabilities available for userPersona: '" + userPersona + "': " + userPersonaCapabilities.asMap()
                                                                                                                                     .keySet());
            throw new InvalidTestDataException(
                    String.format(DEVICE_NAME + " capability NOT found for user persona: '%s'\n%s",
                                  userPersona, userPersonaCapabilities.asMap().keySet()));
        }
        return deviceName;
    }

    public static Platform getPlatformForUser(String userPersona) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getPlatformForUser: Platforms available for userPersonas: ");
            userPersonaPlatforms.keySet().forEach(key -> LOGGER.info(
                    "\tUser Persona: " + key + ": Platform: " + userPersonaPlatforms.get(key)
                                                                                    .name()));
            throw new InvalidTestDataException(
                    String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaPlatforms.get(userPersona);
    }

    public static void attachLogsAndCloseAllWebDrivers() {
        LOGGER.info("Close all drivers:");
        userPersonaDrivers.keySet().forEach(userPersona -> {
            LOGGER.info("\tUser Persona: " + userPersona);
            validateVisualTestResults(userPersona);
            attachLogsAndCloseDriver(userPersona);
        });
        AppiumDriverManager.freeDevices();
    }

    private static void validateVisualTestResults(String userPersona) {
        Driver driver = userPersonaDrivers.get(userPersona);
        driver.getVisual().handleTestResults(userPersona, driver.getType());
    }

    private static void attachLogsAndCloseDriver(String userPersona) {
        Driver driver = userPersonaDrivers.get(userPersona);

        switch(driver.getType()) {
            case Driver.WEB_DRIVER:
                BrowserDriverManager.closeWebDriver(userPersona, driver);
                break;
            case Driver.APPIUM_DRIVER:
                AppiumDriverManager.closeAppiumDriver(userPersona, driver);
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected driver type: '%s'", driver.getType()));
        }
    }

    public static Set<String> getAvailableUserPersonas() {
        return userPersonaDrivers.keySet();
    }

    public static void assignNewPersonaToExistingDriver(String userPersona, String newUserPersona,
                                                        TestExecutionContext context) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info(
                    "assignNewPersonaToExistingDriver: Drivers available for userPersonas: " + userPersonaDrivers.keySet());
            throw new InvalidTestDataException(
                    String.format("No Driver found for user persona: '%s'", userPersona));
        }

        Driver currentDriver = userPersonaDrivers.get(userPersona);
        Platform currentPlatform = userPersonaPlatforms.get(userPersona);
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String logFileName = BrowserDriverManager.getBrowserLogFileNameFor(userPersona);

        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, newUserPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, currentPlatform);

        userPersonaDrivers.remove(userPersona);
        userPersonaPlatforms.remove(userPersona);
        userPersonaDriverCapabilities.remove(userPersona);
        BrowserDriverManager.removeBrowserLogsFor(userPersona);

        userPersonaDrivers.put(newUserPersona, currentDriver);
        userPersonaPlatforms.put(newUserPersona, currentPlatform);
        userPersonaDriverCapabilities.put(newUserPersona, userPersonaCapabilities);
        BrowserDriverManager.addBrowserLogFileNamefor(newUserPersona, logFileName);

        LOGGER.info(
                String.format("assignNewPersonaToExistingDriver: Persona updated from '%s' to '%s'",
                              userPersona, newUserPersona));
    }

    public static void addUserPersonaDriverCapabilities(String userPersona,
                                                        Capabilities windowsDriverCapabilities) {
        userPersonaDriverCapabilities.put(userPersona, windowsDriverCapabilities);
    }

    public static Capabilities getCapabilitiesFor(String userPersona) {
        return userPersonaDriverCapabilities.get(userPersona);
    }

    public static String getAppNamefor(String userPersona) {
        return userPersonaApps.get(userPersona);
    }
}
