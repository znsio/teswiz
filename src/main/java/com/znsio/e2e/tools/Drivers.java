package com.znsio.e2e.tools;

import com.appium.manager.AppiumDevice;
import com.appium.manager.AppiumDriverManager;
import com.appium.manager.DeviceAllocationManager;
import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.github.device.Device;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static io.appium.java_client.remote.MobileCapabilityType.DEVICE_NAME;

public class Drivers {
    private static final String USER_DIR = "user.dir";
    private static final Logger LOGGER = Logger.getLogger(Drivers.class.getName());
    private final Map<String, Driver> userPersonaDrivers = new HashMap<>();
    private final Map<String, Capabilities> userPersonaDriverCapabilities = new HashMap<String, Capabilities>();
    private final Map<String, Platform> userPersonaPlatforms = new HashMap<>();
    private final Map<String, String> userPersonaBrowserLogs = new HashMap<>();
    private final int MAX_NUMBER_OF_APPIUM_DRIVERS = 5;
    private final int MAX_NUMBER_OF_WEB_DRIVERS = 5;
    private int numberOfAndroidDriversUsed = 0;
    private int numberOfWebDriversUsed = 0;
    private int numberOfWindowsDriversUsed = 0;

    public Driver setDriverFor (String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("getDriverFor: start: userPersona: '%s', Platform: '%s'", userPersona, forPlatform.name()));
        if (!userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format("ERROR: Driver for user persona: '%s' DOES NOT EXIST%nAvailable drivers: '%s'",
                    userPersona,
                    userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }
        Driver currentDriver = userPersonaDrivers.get(userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        return currentDriver;
    }

    public Driver createDriverFor (String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("allocateDriverFor: start: userPersona: '%s', Platform: '%s'", userPersona, forPlatform.name()));
        Driver currentDriver = null;
        if (userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format("ERROR: Driver for user persona: '%s' ALREADY EXISTS%nAvailable drivers: '%s'",
                    userPersona,
                    userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }

        switch (forPlatform) {
            case android:
                currentDriver = createAndroidDriverForUser(userPersona, forPlatform, context);
                break;
            case web:
                currentDriver = createWebDriverForUser(userPersona, forPlatform, context);
                break;
            case windows:
                currentDriver = createWindowsDriverForUser(userPersona, forPlatform, context);
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected platform value: '%s' provided to assign Driver for user: '%s': ",
                                forPlatform,
                                userPersona));
        }
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        userPersonaDrivers.put(userPersona, currentDriver);
        userPersonaPlatforms.put(userPersona, forPlatform);
        LOGGER.info(String.format("allocateDriverFor: done: userPersona: '%s', Platform: '%s'%n",
                userPersona,
                forPlatform.name()));

        return currentDriver;
    }

    @NotNull
    private Driver createAndroidDriverForUser (String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createAndroidDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of appiumDrivers: '%d'%n",
                userPersona,
                forPlatform.name(),
                numberOfAndroidDriversUsed));
        Driver currentDriver;
        if (Platform.android.equals(forPlatform) && numberOfAndroidDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'",
                            numberOfAndroidDriversUsed,
                            userPersona,
                            forPlatform.name())
            );
        }

        if (numberOfAndroidDriversUsed == 0) {
            AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
            LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
            userPersonaDriverCapabilities.put(userPersona, appiumDriverCapabilities);
            currentDriver = new Driver(
                    context.getTestName() + "-" + userPersona,
                    appiumDriver);
        } else {
            try {
                AppiumDriver appiumDriver = allocateNewDeviceAndStartAppiumDriver();
                currentDriver = new Driver(context.getTestName() + "-" + userPersona, appiumDriver);
                Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
                LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
                userPersonaDriverCapabilities.put(userPersona, appiumDriverCapabilities);
            } catch (Exception e) {
                throw new EnvironmentSetupException(
                        String.format("Unable to create Android driver '#%d' for user persona: '%s'",
                                numberOfAndroidDriversUsed,
                                userPersona)
                );
            }
        }
        numberOfAndroidDriversUsed++;
        LOGGER.info(String.format("createAndroidDriverForUser: done: userPersona: '%s', Platform: '%s', Number of appiumDrivers: '%d'%n",
                userPersona,
                forPlatform.name(),
                numberOfAndroidDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver);
        return currentDriver;
    }

    private AppiumDriver allocateNewDeviceAndStartAppiumDriver () {
        try {
            DeviceAllocationManager deviceAllocationManager = DeviceAllocationManager.getInstance();
            AppiumDevice availableDevice = deviceAllocationManager.getNextAvailableDevice();
            deviceAllocationManager.allocateDevice(availableDevice);
            AppiumDriver driver = new AppiumDriverManager().startAppiumDriverInstance();
            updateAvailableDeviceInformation(availableDevice);
            return driver;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private AppiumDevice updateAvailableDeviceInformation (AppiumDevice availableDevice) {
        org.openqa.selenium.Capabilities capabilities = AppiumDriverManager.getDriver()
                .getCapabilities();
        LOGGER.info("allocateDeviceAndStartDriver: "
                + capabilities);

        String udid = capabilities.is("udid")
                ? getCapabilityFor(capabilities, "udid")
                : getCapabilityFor(capabilities, "deviceUDID");
        Device device = availableDevice.getDevice();
        device.setUdid(udid);
        device.setDeviceManufacturer(
                getCapabilityFor(capabilities, "deviceManufacturer"));
        device.setDeviceModel(
                getCapabilityFor(capabilities, "deviceModel"));
        device.setName(
                getCapabilityFor(capabilities, "deviceName")
                        + " "
                        + getCapabilityFor(capabilities, "deviceModel"));
        device.setApiLevel(
                getCapabilityFor(capabilities, "deviceApiLevel"));
        device.setDeviceType(
                getCapabilityFor(capabilities, "platformName"));
        device.setScreenSize(
                getCapabilityFor(capabilities, "deviceScreenSize"));
        return availableDevice;
    }

    private String getCapabilityFor (org.openqa.selenium.Capabilities capabilities, String name) {
        Object capability = capabilities.getCapability(name);
        return null == capability ? "" : capability.toString();
    }

    @NotNull
    private Driver createWebDriverForUser (String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createWebDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of webdrivers: '%d'%n",
                userPersona,
                forPlatform.name(),
                numberOfWebDriversUsed));

        Driver currentDriver;
        if (Platform.web.equals(forPlatform) && numberOfWebDriversUsed == MAX_NUMBER_OF_WEB_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'",
                            numberOfWebDriversUsed,
                            userPersona,
                            forPlatform.name())
            );
        }
        String updatedTestName = context.getTestName() + "-" + userPersona;
        if (numberOfWebDriversUsed < MAX_NUMBER_OF_WEB_DRIVERS) {
            currentDriver = new Driver(updatedTestName, createNewWebDriver(userPersona, context));
        } else {
            throw new InvalidTestDataException(
                    String.format("Current number of WebDriver instances used: '%d'. " +
                                    "Unable to create more than '%d' drivers for user persona: '%s' " +
                                    "on platform: '%s'",
                            numberOfWebDriversUsed,
                            MAX_NUMBER_OF_WEB_DRIVERS,
                            userPersona,
                            forPlatform.name())
            );
        }
        numberOfWebDriversUsed++;
        LOGGER.info(String.format("createWebDriverForUser: done: userPersona: '%s', Platform: '%s', Number of webdrivers: '%d'", userPersona, forPlatform.name(), numberOfWebDriversUsed));
        return currentDriver;
    }

    @NotNull
    private Driver createWindowsDriverForUser (String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createWindowsDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of webdrivers: '%d'%n",
                userPersona,
                forPlatform.name(),
                numberOfWindowsDriversUsed));

        Driver currentDriver;
        if (Platform.windows.equals(forPlatform) && numberOfWindowsDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'",
                    numberOfWindowsDriversUsed,
                            userPersona,
                            forPlatform.name())
            );
        }
        if (numberOfWindowsDriversUsed < MAX_NUMBER_OF_APPIUM_DRIVERS) {
            AppiumDriver<WebElement> windowsDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            currentDriver = new Driver(
                    context.getTestName() + "-" + userPersona,
                    windowsDriver);
            Capabilities windowsDriverCapabilities = windowsDriver.getCapabilities();
            LOGGER.info("CAPABILITIES: " + windowsDriverCapabilities);
            userPersonaDriverCapabilities.put(userPersona, windowsDriverCapabilities);
        } else {
            throw new InvalidTestDataException(
                    String.format("Current number of WindowsDriver instances used: '%d'. " +
                                    "Unable to create more than '%d' drivers for user persona: '%s' " +
                                    "on platform: '%s'",
                                    numberOfWindowsDriversUsed,
                                    MAX_NUMBER_OF_APPIUM_DRIVERS,
                            userPersona,
                            forPlatform.name())
            );
        }
        numberOfWindowsDriversUsed++;
        LOGGER.info(String.format("createWindowsDriverForUser: done: userPersona: '%s', Platform: '%s', Number of windowsDrivers: '%d'", userPersona, forPlatform.name(), numberOfWindowsDriversUsed));
        return currentDriver;
    }

    private void disableNotificationsAndToastsOnDevice (Driver currentDriver) {
        if (Runner.isRunningInCI()) {
            Object disableToasts = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand", "adb shell appops set " + Runner.getAppPackageName() + " TOAST_WINDOW deny");
            LOGGER.info("@disableToastsCommandResponse: " + disableToasts);
            Object disableNotifications = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand", "adb shell settings put global heads_up_notifications_enabled 0");
            LOGGER.info("@disableNotificationsCommandResponse: " + disableNotifications);
        } else {
            String[] disableToastsCommand = new String[]{"adb", "-s", "${device.SERIAL}", "shell", "appops", "set", Runner.getAppPackageName(), "TOAST_WINDOW", "deny"};
            String[] disableNotificationsCommand = new String[]{"adb", "-s", "${device.SERIAL}", "shell", "settings", "put", "global", "heads_up_notifications_enabled", "0"};

            CommandLineResponse disableToastsCommandResponse = CommandLineExecutor.execCommand(disableToastsCommand);
            LOGGER.info("disableToastsCommandResponse: " + disableToastsCommandResponse);
            CommandLineResponse disableNotificationsCommandResponse = CommandLineExecutor.execCommand(disableNotificationsCommand);
            LOGGER.info("disableNotificationsCommandResponse: " + disableNotificationsCommandResponse);
        }
    }

    @NotNull
    private WebDriver createNewWebDriver (String forUserPersona,
                                          TestExecutionContext testExecutionContext) {
        String browserType = Runner.getBrowser();
        boolean shouldMaximizeBrowser = Runner.shouldMaximizeBrowser();

        String providedBaseUrl = Runner.getBaseURLForWeb();
        if (null == providedBaseUrl) {
            throw new InvalidTestDataException("baseUrl not provided as an environment variable");
        }
        String baseUrl = String.valueOf(Runner.getFromEnvironmentConfiguration(providedBaseUrl));
        LOGGER.info("baseUrl: " + baseUrl);

        DriverManagerType driverManagerType = DriverManagerType.valueOf(browserType.toUpperCase());
        WebDriverManager.getInstance(driverManagerType).setup();

        WebDriver driver = null;
        switch (driverManagerType) {
            case CHROME:
                driver = createChromeDriver(forUserPersona, testExecutionContext);
                break;
            case FIREFOX:
                driver = createFirefoxDriver(forUserPersona, testExecutionContext);
                break;
            case OPERA:
            case EDGE:
            case PHANTOMJS:
            case IEXPLORER:
            case SELENIUM_SERVER_STANDALONE:
            case CHROMIUM:
            case SAFARI:
                throw new InvalidTestDataException(String.format("Browser: '%s' is NOT supported", browserType));
        }
        driver.get(baseUrl);
        if (shouldMaximizeBrowser) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    private WebDriver createFirefoxDriver (String forUserPersona,
                                           TestExecutionContext testExecutionContext) {

        boolean isBrowserHeadless = Runner.isRunInHeadlessMode();
        boolean enableVerboseLogging = Runner.enableVerboseLoggingInBrowser();
        String proxyUrl = Runner.getProxyURL();

        String logFileName = setLogDirectory(forUserPersona, testExecutionContext, "Firefox");
        userPersonaBrowserLogs.put(forUserPersona, logFileName);
        LOGGER.info("Creating Firefox logs in file: " + logFileName);
        System.setProperty("webdriver.firefox.logfile", logFileName);

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("dom.push.enabled", false);
        firefoxOptions.setProfile(firefoxProfile);
        firefoxOptions.addPreference("dom.webnotifications.enabled", false);
        firefoxOptions.addArguments("disable-infobars");
        firefoxOptions.addArguments("--disable-extensions");
        firefoxOptions.addArguments("--disable-notifications");

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);

        if (enableVerboseLogging) {
            firefoxOptions.setLogLevel(FirefoxDriverLogLevel.DEBUG);
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            firefoxOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        } else {
            firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
        }

        if (null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            firefoxOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }
        firefoxOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        firefoxOptions.setHeadless(isBrowserHeadless);

        LOGGER.info("FirefoxOptions: " + firefoxOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(firefoxOptions) : new FirefoxDriver(firefoxOptions);
        Capabilities capabilities = Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities() : ((FirefoxDriver) driver).getCapabilities();
        userPersonaDriverCapabilities.put(forUserPersona, capabilities);
        return driver;
    }

    @NotNull
    private WebDriver createChromeDriver (String forUserPersona,
                                          TestExecutionContext testExecutionContext) {
        boolean isBrowserHeadless = Runner.isRunInHeadlessMode();
        boolean enableVerboseLogging = Runner.enableVerboseLoggingInBrowser();
        String proxyUrl = Runner.getProxyURL();

        String logFileName = setLogDirectory(forUserPersona, testExecutionContext, "Chrome");
        userPersonaBrowserLogs.put(forUserPersona, logFileName);
        LOGGER.info("Creating Chrome logs in file: " + logFileName);
        System.setProperty("webdriver.chrome.logfile", logFileName);

        ChromeOptions chromeOptions = new ChromeOptions();
        List<String> excludeSwitches = Arrays.asList(
                "enable-automation",
                "disable-notifications",
                "disable-default-apps",
                "disable-extensions",
                "enable-user-metrics",
                "incognito",
                "show-taps",
                "disable-infobars"
        );
        chromeOptions.setExperimentalOption("excludeSwitches", excludeSwitches);

        Map<String, Boolean> excludedSchemes = new HashMap<>();
        excludedSchemes.put("jhb", true);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 1);
        prefs.put("profile.default_content_setting_values.media_stream_mic", 1);
        prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
        prefs.put("protocol_handler.excluded_schemes", excludedSchemes);
        chromeOptions.setExperimentalOption("prefs", prefs);

        LOGGER.info("Set Logging preferences");
        LoggingPreferences logPrefs = new LoggingPreferences();
        if (enableVerboseLogging) {
            System.setProperty("webdriver.chrome.verboseLogging", "true");
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        } else {
            logPrefs.enable(LogType.BROWSER, Level.ALL);
        }

        LOGGER.info("Set Proxy:");
        LOGGER.info(proxyUrl);
        if (null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            chromeOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }

        chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        chromeOptions.setHeadless(isBrowserHeadless);

        LOGGER.info("ChromeOptions: " + chromeOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(chromeOptions) : new ChromeDriver(chromeOptions);
        Capabilities capabilities = Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities() : ((ChromeDriver) driver).getCapabilities();
        userPersonaDriverCapabilities.put(forUserPersona, capabilities);
        return driver;
    }

    private String setLogDirectory (String forUserPersona, TestExecutionContext testExecutionContext, String browserType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + testExecutionContext.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        String logFile = scenarioLogDir + File.separator + "deviceLogs" + File.separator + browserType + "-" + forUserPersona + ".log";

        File file = new File(logFile);
        file.getParentFile().mkdirs();

        LOGGER.info("Creating " + browserType + " logs in file: " + logFile);
        return logFile;
    }

    @NotNull
    private RemoteWebDriver createRemoteWebDriver (MutableCapabilities chromeOptions) {
        try {
            return new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), chromeOptions);
        } catch (MalformedURLException e) {
            throw new EnvironmentSetupException("Unable to create a new RemoteWebDriver", e);
        }
    }

    public Driver getDriverForUser (String userPersona) {
        if (!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getDriverForUser: Drivers available for userPersonas: " + userPersonaDrivers.keySet());
            throw new InvalidTestDataException(String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaDrivers.get(userPersona);
    }

    public String getDeviceNameForUser (String userPersona) {
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String deviceName = (String) userPersonaCapabilities.getCapability(DEVICE_NAME);
        if (null == deviceName) {
            LOGGER.info("getDeviceNameForUser: Capabilities available for userPersona: '" + userPersona + "': " + userPersonaCapabilities.asMap().keySet());
            throw new InvalidTestDataException(String.format(DEVICE_NAME + " capability NOT found for user persona: '%s'\n%s", userPersona, userPersonaCapabilities.asMap().keySet()));
        }
        return deviceName;
    }

    public Platform getPlatformForUser (String userPersona) {
        if (!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getPlatformForUser: Platforms available for userPersonas: ");
            userPersonaPlatforms.keySet().forEach(key -> {
                LOGGER.info("\tUser Persona: " + key + ": Platform: " + userPersonaPlatforms.get(key).name());
            });
            throw new InvalidTestDataException(String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaPlatforms.get(userPersona);
    }

    public void attachLogsAndCloseAllWebDrivers (TestExecutionContext context) {
        LOGGER.info("Close all drivers:");
        userPersonaDrivers.keySet().forEach(key -> {
            LOGGER.info("\tUser Persona: " + key);
            validateVisualTestResults(key);
            attachLogsAndCloseDriver(key);
        });
    }

    private void validateVisualTestResults (String key) {
        Driver driver = userPersonaDrivers.get(key);
        driver.getVisual().handleTestResults(key);
    }

    private void attachLogsAndCloseDriver (String key) {
        Driver driver = userPersonaDrivers.get(key);

        switch (driver.getType()) {
            case Driver.WEB_DRIVER:
                closeWebDriver(key, driver);
                break;
            case Driver.APPIUM_DRIVER:
                if (Runner.platform.equals(Platform.windows)) {
                    closeAppOnMachine(driver);
                } else {
                    closeAppOnDevice(driver);
                }
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected driver type: '%s'", driver.getType()));
        }
    }

    private void closeWebDriver (String key, Driver driver) {
        String message = "Browser logs for user: " + key;
        String logFileName = userPersonaBrowserLogs.get(key);
        LOGGER.info(message + ": logFileName: " + logFileName);
        ReportPortal.emitLog(
                message,
                "DEBUG",
                new Date(), new File(logFileName));
        WebDriver webDriver = driver.getInnerDriver();
        if (null == webDriver) {
            LOGGER.info(String.format("Strange. But WebDriver for user: '%s' already closed", key));
        } else {
            LOGGER.info(String.format("Closing WebDriver for user: '%s'", key));
            webDriver.quit();
        }
    }

    private void closeAppOnDevice (Driver driver) {
        String appPackageName = Runner.getAppPackageName();
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if (Runner.isRunningInCI()) {
            String message = "Skip terminating & closing app on Cloud device";
            LOGGER.info(message);
            ReportPortal.emitLog(message, "DEBUG", new Date());
        } else {
            LOGGER.info("Terminate app: " + appPackageName);
            ApplicationState applicationState = appiumDriver.queryAppState(appPackageName);
            LOGGER.info("Application State: " + applicationState);
            appiumDriver.closeApp();
            appiumDriver.quit();
            ReportPortal.emitLog(
                    String.format("App: '%s' Current application state: '%s'%n",

                            appPackageName,
                            applicationState),
                    "DEBUG",
                    new Date());
        }
    }

    private void closeAppOnMachine (Driver driver) {
        String appPackageName = Runner.getAppPackageName();
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        LOGGER.info(String.format("Closing WindowsDriver for App '%s'", appPackageName));
        appiumDriver.closeApp();
        appiumDriver.quit();
        ReportPortal.emitLog(
                String.format("App: '%s' terminated",
                        appPackageName),
                "DEBUG",
                new Date());
    }
}
