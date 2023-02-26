package com.znsio.teswiz.runner;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.znsio.teswiz.runner.Runner.DEFAULT;
import static io.appium.java_client.remote.MobileCapabilityType.DEVICE_NAME;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

public class Drivers {
    private static final Map<String, Capabilities> userPersonaDriverCapabilities = new HashMap<>();
    private static final Map<String, String> userPersonaApps = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Drivers.class.getName());
    private static final Map<String, Driver> userPersonaDrivers = new HashMap<>();
    private static final Map<String, Platform> userPersonaPlatforms = new HashMap<>();
    private static final String NO_DRIVER_FOUND_FOR_USER_PERSONA = "No Driver found for user " +
                                                                   "persona: '%s'";
    private static final Map<String, String> deviceLogFileNameForUserPersonaAndPlaform =
            new HashMap<>();

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
                    String.format(NO_DRIVER_FOUND_FOR_USER_PERSONA, userPersona));
        }

        return userPersonaDrivers.get(userPersona);
    }

    public static String getDeviceNameForUser(String userPersona) {
        return getDeviceOrBrowserNameFromCapabilitiesForUser(userPersona, DEVICE_NAME);
    }

    public static String getBrowserNameForUser(String userPersona) {
        return getDeviceOrBrowserNameFromCapabilitiesForUser(userPersona, BROWSER_NAME);
    }

    @NotNull
    private static String getDeviceOrBrowserNameFromCapabilitiesForUser(String userPersona,
                                                                        String capabilityName) {
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String name = (String) userPersonaCapabilities.getCapability(capabilityName);
        if(null == name) {
            LOGGER.info(
                    "Capabilities available for userPersona: '" + userPersona + "': " + userPersonaCapabilities.asMap()
                                                                                                               .keySet());
            throw new InvalidTestDataException(String.format(
                    capabilityName + " capability NOT found for user persona: '%s'%n%s",
                    userPersona, userPersonaCapabilities.asMap().keySet()));
        }
        return name;
    }

    public static Platform getPlatformForUser(String userPersona) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getPlatformForUser: Platforms available for userPersonas: ");
            userPersonaPlatforms.keySet().forEach(key -> LOGGER.info(
                    "\tUser Persona: " + key + ": Platform: " + userPersonaPlatforms.get(key)
                                                                                    .name()));
            throw new InvalidTestDataException(
                    String.format(NO_DRIVER_FOUND_FOR_USER_PERSONA, userPersona));
        }

        return userPersonaPlatforms.get(userPersona);
    }

    public static void attachLogsAndCloseAllWebDrivers() {
        LOGGER.info("Close all drivers:");
        userPersonaDrivers.forEach((userPersona, driver) -> {
            LOGGER.info("\tUser Persona: " + userPersona);
            validateVisualTestResults(userPersona, driver);
            attachLogsAndCloseDriver(userPersona, driver);
        });
        AppiumDriverManager.freeDevices();
        userPersonaDrivers.clear();
        userPersonaApps.clear();
        userPersonaDriverCapabilities.clear();
        userPersonaPlatforms.clear();
        deviceLogFileNameForUserPersonaAndPlaform.clear();
    }

    private static void validateVisualTestResults(String userPersona, Driver driver) {
        driver.getVisual().handleTestResults(userPersona, driver.getType());
    }

    private static void attachLogsAndCloseDriver(String userPersona, Driver driver) {
        LOGGER.info(String.format("attachLogsAndCloseDriver: %s - %s - %s", userPersona,
                                  driver.getType(),
                                  driver.getInnerDriver().getClass().getSimpleName()));
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
                    String.format(NO_DRIVER_FOUND_FOR_USER_PERSONA, userPersona));
        }

        Driver currentDriver = userPersonaDrivers.get(userPersona);
        Platform currentPlatform = userPersonaPlatforms.get(userPersona);
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);

        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, newUserPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, currentPlatform);

        userPersonaDrivers.remove(userPersona);
        userPersonaPlatforms.remove(userPersona);
        userPersonaDriverCapabilities.remove(userPersona);

        userPersonaDrivers.put(newUserPersona, currentDriver);
        userPersonaPlatforms.put(newUserPersona, currentPlatform);
        userPersonaDriverCapabilities.put(newUserPersona, userPersonaCapabilities);

        LOGGER.info(
                String.format("assignNewPersonaToExistingDriver: Persona updated from '%s' to '%s'",
                              userPersona, newUserPersona));
    }

    public static void addUserPersonaDriverCapabilities(String userPersona,
                                                        Capabilities capabilities) {
        userPersonaDriverCapabilities.put(userPersona, capabilities);
    }

    public static Capabilities getCapabilitiesFor(String userPersona) {
        return userPersonaDriverCapabilities.get(userPersona);
    }

    public static String getAppNamefor(String userPersona) {
        return userPersonaApps.get(userPersona);
    }

    static String getDeviceLogFileNameFor(String userPersona, String forPlatform) {
        return deviceLogFileNameForUserPersonaAndPlaform.get(
                getKeyForDeviceLogFiles(userPersona, forPlatform));
    }

    static void addDeviceLogFileNameFor(String userPersona, String forPlatform,
                                        String deviceLogFileName) {
        deviceLogFileNameForUserPersonaAndPlaform.put(
                getKeyForDeviceLogFiles(userPersona, forPlatform), deviceLogFileName);
    }

    @NotNull
    private static String getKeyForDeviceLogFiles(String userPersona, String platform) {
        return userPersona + "-" + platform;
    }

    static void addBrowserLogFileNameFor(String userPersona, String forPlatform, String browserType,
                                         String logFileName) {
        deviceLogFileNameForUserPersonaAndPlaform.put(
                getKeyForDeviceLogFiles(userPersona, forPlatform + "-" + browserType), logFileName);
    }

    static String getBrowserLogFileNameFor(String userPersona, String forPlatform,
                                           String browserType) {
        return deviceLogFileNameForUserPersonaAndPlaform.get(
                getKeyForDeviceLogFiles(userPersona, forPlatform + "-" + browserType));
    }
}
