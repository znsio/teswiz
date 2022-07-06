package com.znsio.e2e.tools;

import com.appium.manager.AppiumDevice;
import com.appium.manager.AppiumDriverManager;
import com.appium.manager.DeviceAllocationManager;
import com.context.SessionContext;
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
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.znsio.e2e.runner.Runner.DEFAULT;
import static com.znsio.e2e.runner.Runner.NOT_SET;
import static com.znsio.e2e.runner.Setup.CAPS;
import static io.appium.java_client.remote.MobileCapabilityType.DEVICE_NAME;

public class Drivers {
    private static final Logger LOGGER = Logger.getLogger(Drivers.class.getName());
    private static final String DEBUG = "DEBUG";
    private static final String BROWSER_CONFIG_SCHEMA_FILE = "BrowserConfigSchema.json";
    private final Map<String, Driver> userPersonaDrivers = new HashMap<>();
    private final Map<String, Capabilities> userPersonaDriverCapabilities = new HashMap<>();
    private final Map<String, Platform> userPersonaPlatforms = new HashMap<>();
    private final Map<String, String> userPersonaBrowserLogs = new HashMap<>();
    private final Map<String, String> userPersonaApps = new HashMap<>();
    private final List<AppiumDevice> additionalDevices = new ArrayList<>();
    private final int MAX_NUMBER_OF_APPIUM_DRIVERS;
    private final int MAX_NUMBER_OF_WEB_DRIVERS;
    private int numberOfWebDriversUsed = 0;
    private int numberOfAppiumDriversUsed = 0;
    private boolean shouldBrowserBeMaximized = false;
    private boolean isRunInHeadlessMode = false;

    public Drivers() {
        MAX_NUMBER_OF_APPIUM_DRIVERS = Runner.getMaxNumberOfAppiumDrivers();
        MAX_NUMBER_OF_WEB_DRIVERS = Runner.getMaxNumberOfWebDrivers();
    }

    public Driver setDriverFor(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("setDriverFor: start: userPersona: '%s', Platform: '%s'", userPersona, forPlatform.name()));
        if(!userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format("ERROR: Driver for user persona: '%s' DOES NOT EXIST%nAvailable drivers: '%s'", userPersona, userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }
        Driver currentDriver = userPersonaDrivers.get(userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, forPlatform);
        return currentDriver;
    }

    public Driver createDriverFor(String userPersona, Platform forPlatform, TestExecutionContext context) {
        return createDriverFor(userPersona, DEFAULT, Runner.getBrowser(), forPlatform, context);
    }

    public Driver createDriverFor(String userPersona, String appName, String browserName, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createDriverFor: start: userPersona: '%s', Platform: '%s'", userPersona, forPlatform.name()));
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, userPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, forPlatform);
        userPersonaApps.put(userPersona, appName);
        userPersonaPlatforms.put(userPersona, forPlatform);

        Driver currentDriver;
        if(userPersonaDrivers.containsKey(userPersona)) {
            String message = String.format("ERROR: Driver for user persona: '%s' ALREADY EXISTS%nAvailable drivers: '%s'", userPersona, userPersonaDrivers.keySet());
            throw new InvalidTestDataException(message);
        }

        switch(forPlatform) {
            case android:
                currentDriver = createAndroidDriverForUser(userPersona, forPlatform, context);
                break;
            case web:
                currentDriver = createWebDriverForUser(userPersona, browserName, forPlatform, context);
                break;
            case windows:
                currentDriver = createWindowsDriverForUser(userPersona, forPlatform, context);
                break;
            default:
                throw new InvalidTestDataException(String.format("Unexpected platform value: '%s' provided to assign Driver for user: '%s': ", forPlatform, userPersona));
        }
        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        userPersonaDrivers.put(userPersona, currentDriver);
        LOGGER.info(String.format("createDriverFor: done: userPersona: '%s', Platform: '%s'%n", userPersona, forPlatform.name()));

        return currentDriver;
    }

    @NotNull
    private Driver createAndroidDriverForUser(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createAndroidDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of appiumDrivers: '%d'%n", userPersona, forPlatform.name(),
                                  numberOfAppiumDriversUsed));
        Driver currentDriver;
        if(Platform.android.equals(forPlatform) && numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'", numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }

        String capabilityFileNameToUseForDriverCreation = System.getProperty(CAPS);

        String appName = userPersonaApps.get(userPersona);
        if(!appName.equalsIgnoreCase(DEFAULT)) {
            String capabilityFileDirectory = new File(capabilityFileNameToUseForDriverCreation).getParent();
            capabilityFileNameToUseForDriverCreation = capabilityFileDirectory + File.separator + (appName + "_capabilities.json");
        }
        File capabilityFileToUseForDriverCreation = new File(capabilityFileNameToUseForDriverCreation);
        System.out.println("capabilityFileToUseForDriverCreation: " + capabilityFileToUseForDriverCreation.getAbsolutePath());
        System.out.println("capabilityFileToUseForDriverCreation.exists(): " + capabilityFileToUseForDriverCreation.exists());

        if(numberOfAppiumDriversUsed == 0) {
            AppiumDriver<WebElement> appiumDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            AppiumDevice deviceInfo = (AppiumDevice) context.getTestState(TEST_CONTEXT.DEVICE_INFO);
            // Do not add the device info to additionalDevices for the driver created by ATD
            // additionalDevices.add(deviceInfo);
            Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
            context.addTestState(TEST_CONTEXT.DEVICE_ON, deviceInfo.getDeviceOn());
            LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
            userPersonaDriverCapabilities.put(userPersona, appiumDriverCapabilities);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, deviceInfo.getDeviceOn(), userPersona, appName, appiumDriver);
        } else {
            try {
                AppiumDriver appiumDriver = allocateNewDeviceAndStartAppiumDriver(context, capabilityFileToUseForDriverCreation.getAbsolutePath());
                currentDriver = new Driver(context.getTestName() + "-" + userPersona, context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON), userPersona, appName, appiumDriver);
                Capabilities appiumDriverCapabilities = appiumDriver.getCapabilities();
                LOGGER.info("CAPABILITIES: " + appiumDriverCapabilities);
                appiumDriverCapabilities.getCapabilityNames()
                                        .forEach(key -> LOGGER.info("\t" + key + ":: " + appiumDriverCapabilities.getCapability(key)));

                userPersonaDriverCapabilities.put(userPersona, appiumDriverCapabilities);
            } catch(Exception e) {
                throw new EnvironmentSetupException(String.format("Unable to create Android driver '#%d' for user persona: '%s'", numberOfAppiumDriversUsed, userPersona));
            }
        }
        numberOfAppiumDriversUsed++;
        LOGGER.info(String.format("createAndroidDriverForUser: done: userPersona: '%s', Platform: '%s', Number of appiumDrivers: '%d'%n", userPersona, forPlatform.name(),
                                  numberOfAppiumDriversUsed));
        disableNotificationsAndToastsOnDevice(currentDriver, context.getTestStateAsString(TEST_CONTEXT.DEVICE_ON), (String) userPersonaDriverCapabilities.get(userPersona)
                                                                                                                                                         .getCapability("udid"));
        return currentDriver;
    }

    @NotNull
    private Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform, TestExecutionContext context) {
        JSONObject browserConfig = null;
        LOGGER.info(String.format("createWebDriverForUser: begin: userPersona: '%s', browserName: '%s', Platform: '%s', Number of webdrivers: '%d'%n", userPersona, browserName,
                                  forPlatform.name(), numberOfWebDriversUsed));

        String baseUrl = getBaseUrl(userPersona);
        String appName = userPersonaApps.get(userPersona);

        if(numberOfWebDriversUsed == 0) {
            browserConfig = getBrowserConfig();
            context.addTestState(TEST_CONTEXT.BROWSER_CONFIG, browserConfig);
            checkConnectivityToBaseUrl(baseUrl);
        } else {
            browserConfig = (JSONObject) context.getTestState(TEST_CONTEXT.BROWSER_CONFIG);
        }

        Driver currentDriver;
        if(Platform.web.equals(forPlatform) && numberOfWebDriversUsed == MAX_NUMBER_OF_WEB_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'", numberOfWebDriversUsed, userPersona, forPlatform.name()));
        }
        String updatedTestName = context.getTestName() + "-" + userPersona;
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        if(numberOfWebDriversUsed < MAX_NUMBER_OF_WEB_DRIVERS) {
            LOGGER.info("Create new webdriver instance");
            WebDriver newWebDriver = createNewWebDriver(userPersona, browserName, context, browserConfig);
            LOGGER.info("Webdriver instance created");
            newWebDriver.get(baseUrl);
            LOGGER.info("Navigated to baseUrl: " + baseUrl);
            currentDriver = new Driver(updatedTestName, runningOn, userPersona, appName, newWebDriver, isRunInHeadlessMode, shouldBrowserBeMaximized);
            LOGGER.info("New Driver with Visual instance created");
        } else {
            throw new InvalidTestDataException(
                    String.format("Current number of WebDriver instances used: '%d'. " + "Unable to create more than '%d' drivers for user persona: '%s' " + "on platform: '%s'",
                                  numberOfWebDriversUsed, MAX_NUMBER_OF_WEB_DRIVERS, userPersona, forPlatform.name()));
        }
        numberOfWebDriversUsed++;

        LOGGER.info(String.format("createWebDriverForUser: done: userPersona: '%s', Platform: '%s', appName: '%s', Number of webdrivers: '%d'", userPersona, forPlatform.name(),
                                  appName, numberOfWebDriversUsed));
        return currentDriver;
    }

    @NotNull
    private Driver createWindowsDriverForUser(String userPersona, Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format("createWindowsDriverForUser: begin: userPersona: '%s', Platform: '%s', Number of webdrivers: '%d'%n", userPersona, forPlatform.name(),
                                  numberOfAppiumDriversUsed));

        Driver currentDriver;
        if(Platform.windows.equals(forPlatform) && numberOfAppiumDriversUsed == MAX_NUMBER_OF_APPIUM_DRIVERS) {
            throw new InvalidTestDataException(
                    String.format("Unable to create more than '%d' drivers for user persona: '%s' on platform: '%s'", numberOfAppiumDriversUsed, userPersona, forPlatform.name()));
        }
        if(numberOfAppiumDriversUsed < MAX_NUMBER_OF_APPIUM_DRIVERS) {
            AppiumDriver<WebElement> windowsDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            String appName = userPersonaApps.get(userPersona);

            String runningOn = Runner.isRunningInCI() ? "CI" : "local";
            context.addTestState(TEST_CONTEXT.WINDOWS_DEVICE_ON, runningOn);
            currentDriver = new Driver(context.getTestName() + "-" + userPersona, userPersona, appName, runningOn, windowsDriver);
            Capabilities windowsDriverCapabilities = windowsDriver.getCapabilities();
            LOGGER.info("CAPABILITIES: " + windowsDriverCapabilities);
            userPersonaDriverCapabilities.put(userPersona, windowsDriverCapabilities);
        } else {
            throw new InvalidTestDataException(String.format(
                    "Current number of WindowsDriver instances used: '%d'. " + "Unable to create more than '%d' drivers for user persona: '%s' " + "on platform: '%s'",
                    numberOfAppiumDriversUsed, MAX_NUMBER_OF_APPIUM_DRIVERS, userPersona, forPlatform.name()));
        }
        numberOfAppiumDriversUsed++;
        LOGGER.info(String.format("createWindowsDriverForUser: done: userPersona: '%s', Platform: '%s', Number of windowsDrivers: '%d'", userPersona, forPlatform.name(),
                                  numberOfAppiumDriversUsed));
        return currentDriver;
    }

    private AppiumDriver allocateNewDeviceAndStartAppiumDriver(TestExecutionContext context, String capabilityFile) {
        try {
            String testName = context.getTestName();
            String normalisedScenarioName = context.getTestStateAsString(TEST_CONTEXT.NORMALISED_SCENARIO_NAME);
            Integer scenarioRunCount = (Integer) context.getTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT);
            DeviceAllocationManager deviceAllocationManager = DeviceAllocationManager.getInstance();
            AppiumDevice availableDevice = deviceAllocationManager.getNextAvailableDevice();
            deviceAllocationManager.allocateDevice(availableDevice);
            AppiumDriver driver = new AppiumDriverManager().startAppiumDriverInstance(testName, capabilityFile);
            additionalDevices.add(availableDevice);
            updateAvailableDeviceInformation(availableDevice);
            try {
                String deviceLogFileName = availableDevice.startDataCapture(normalisedScenarioName, scenarioRunCount);
                LOGGER.info("Started device log capture in file: " + deviceLogFileName);
            } catch(IOException | InterruptedException e) {
                LOGGER.info("Error in starting data capture: " + e.getMessage());
                e.printStackTrace();
            }
            ReportPortal.emitLog("allocateNewDeviceAndStartAppiumDriver: Device Info\n" + availableDevice, DEBUG, new Date());
            return driver;
        } catch(Exception e) {
            LOGGER.info(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    private void disableNotificationsAndToastsOnDevice(Driver currentDriver, String deviceOn, String udid) {
        if(Runner.isRunningInCI()) {
            if(deviceOn.equalsIgnoreCase("pCloudy")) {
                Object disableToasts = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand",
                                                                                                     "adb shell appops set " + Runner.getAppPackageName() + " TOAST_WINDOW deny");
                LOGGER.info("@disableToastsCommandResponse: " + disableToasts);
                Object disableNotifications = ((AppiumDriver) currentDriver.getInnerDriver()).executeScript("pCloudy_executeAdbCommand",
                                                                                                            "adb shell settings put global heads_up_notifications_enabled 0");
                LOGGER.info("@disableNotificationsCommandResponse: " + disableNotifications);
            }
        } else {
            String[] disableToastsCommand = new String[]{"adb", "-s", udid, "shell", "appops", "set", Runner.getAppPackageName(), "TOAST_WINDOW", "deny"};
            String[] disableNotificationsCommand = new String[]{"adb", "-s", udid, "shell", "settings", "put", "global", "heads_up_notifications_enabled", "0"};

            CommandLineResponse disableToastsCommandResponse = CommandLineExecutor.execCommand(disableToastsCommand);
            LOGGER.info("disableToastsCommandResponse: " + disableToastsCommandResponse);
            CommandLineResponse disableNotificationsCommandResponse = CommandLineExecutor.execCommand(disableNotificationsCommand);
            LOGGER.info("disableNotificationsCommandResponse: " + disableNotificationsCommandResponse);
        }
    }

    private String getBaseUrl(String userPersona) {
        String providedBaseUrlKey = Runner.getBaseURLForWeb();

        String appName = userPersonaApps.get(userPersona);
        if(!appName.equalsIgnoreCase(DEFAULT)) {
            providedBaseUrlKey = appName.toUpperCase() + "_BASE_URL";
        }
        System.out.println("Using BaseURL key: " + providedBaseUrlKey);

        if(null == providedBaseUrlKey) {
            throw new InvalidTestDataException("baseUrl not provided");
        }
        return String.valueOf(Runner.getFromEnvironmentConfiguration(providedBaseUrlKey));
    }

    private JSONObject getBrowserConfig() {
        String browserConfigFileContents = Runner.getBrowserConfigFileContents();
        String browserConfigFile = Runner.getBrowserConfigFile();
        return JsonSchemaValidator.validateJsonFileAgainstSchema(browserConfigFile, browserConfigFileContents, BROWSER_CONFIG_SCHEMA_FILE);
    }

    private void checkConnectivityToBaseUrl(String baseUrl) {
        LOGGER.info(String.format("Check connectivity to baseUrl: '%s'", baseUrl));
        String[] curlCommand = new String[]{"curl -m 60 --insecure -I " + baseUrl};
        CommandLineExecutor.execCommand(curlCommand);
    }

    @NotNull
    private WebDriver createNewWebDriver(String forUserPersona, String browserType, TestExecutionContext testExecutionContext, JSONObject browserConfig) {

        DriverManagerType driverManagerType = setupBrowserDriver(testExecutionContext, browserType);

        WebDriver driver = null;
        switch(driverManagerType) {
            case CHROME:
                driver = createChromeDriver(forUserPersona, testExecutionContext, browserConfig.getJSONObject(driverManagerType.getBrowserNameLowerCase()));
                break;
            case FIREFOX:
                driver = createFirefoxDriver(forUserPersona, testExecutionContext, browserConfig.getJSONObject(driverManagerType.getBrowserNameLowerCase()));
                break;
            case SAFARI:
                driver = createSafariDriver(forUserPersona, testExecutionContext, browserConfig.getJSONObject(driverManagerType.getBrowserNameLowerCase()));
                break;
            case EDGE:
            case IEXPLORER:
            case CHROMIUM:
            case OPERA:
                throw new InvalidTestDataException(String.format("Browser: '%s' is NOT supported", browserType));
        }
        LOGGER.info("Driver created");

        if(shouldBrowserBeMaximized && !isRunInHeadlessMode) {
            driver.manage()
                  .window()
                  .maximize();
        } else if(isRunInHeadlessMode) {
            driver.manage()
                  .window()
                  .setSize(new Dimension(1920, 1080));
        }
        LOGGER.info("Reset browser window size");
        return driver;
    }

    private AppiumDevice updateAvailableDeviceInformation(AppiumDevice availableDevice) {
        org.openqa.selenium.Capabilities capabilities = AppiumDriverManager.getDriver()
                                                                           .getCapabilities();
        LOGGER.info("allocateDeviceAndStartDriver: " + capabilities);

        String udid = capabilities.is("udid") ? getCapabilityFor(capabilities, "udid") : getCapabilityFor(capabilities, "deviceUDID");
        Device device = availableDevice.getDevice();
        device.setUdid(udid);
        device.setDeviceManufacturer(getCapabilityFor(capabilities, "deviceManufacturer"));
        device.setDeviceModel(getCapabilityFor(capabilities, "deviceModel"));
        device.setName(getCapabilityFor(capabilities, "deviceName") + " " + getCapabilityFor(capabilities, "deviceModel"));
        device.setApiLevel(getCapabilityFor(capabilities, "deviceApiLevel"));
        device.setDeviceType(getCapabilityFor(capabilities, "platformName"));
        device.setScreenSize(getCapabilityFor(capabilities, "deviceScreenSize"));
        return availableDevice;
    }

    @NotNull
    private DriverManagerType setupBrowserDriver(TestExecutionContext testExecutionContext, String browserType) {
        DriverManagerType driverManagerType = DriverManagerType.valueOf(browserType.toUpperCase());
        String webDriverManagerProxyUrl = (null == Runner.getWebDriverManagerProxyURL()) ? "" : Runner.getWebDriverManagerProxyURL();
        LOGGER.info(String.format("Using webDriverManagerProxyUrl: '%s' for getting the WebDriver for browser: '%s'", webDriverManagerProxyUrl, browserType));

        WebDriverManager webDriverManager = WebDriverManager.getInstance(driverManagerType)
                                                            .proxy(webDriverManagerProxyUrl);
        webDriverManager.setup();
        String downloadedDriverVersion = webDriverManager.getDownloadedDriverVersion();

        String message = String.format("Using %s browser version: %s", driverManagerType, downloadedDriverVersion);
        LOGGER.info(message);
        ReportPortal.emitLog(message, "info", new Date());
        return driverManagerType;
    }

    @NotNull
    private WebDriver createChromeDriver(String forUserPersona, TestExecutionContext testExecutionContext, JSONObject chromeConfiguration) {

        boolean enableVerboseLogging = chromeConfiguration.getBoolean("verboseLogging");
        boolean acceptInsecureCerts = chromeConfiguration.getBoolean("acceptInsecureCerts");
        shouldBrowserBeMaximized = chromeConfiguration.getBoolean("maximize");
        String proxyUrl = Runner.getProxyURL();

        ChromeOptions chromeOptions = new ChromeOptions();

        setLogFileName(forUserPersona, testExecutionContext, "Chrome");

        JSONArray excludeSwitches = chromeConfiguration.getJSONArray("excludeSwitches");
        List<String> excludeSwitchesAsString = new ArrayList<>();
        excludeSwitches.forEach(switchToBeExcluded -> excludeSwitchesAsString.add(switchToBeExcluded.toString()));
        chromeOptions.setExperimentalOption("excludeSwitches", excludeSwitchesAsString);

        JSONObject excludedSchemes = chromeConfiguration.getJSONObject("excludedSchemes");
        JSONObject preferences = chromeConfiguration.getJSONObject("preferences");
        preferences.put("protocol_handler.excluded_schemes", excludedSchemes);
        chromeOptions.setExperimentalOption("prefs", preferences);

        LOGGER.info("Set Logging preferences");
        LoggingPreferences logPrefs = new LoggingPreferences();
        if(enableVerboseLogging) {
            System.setProperty("webdriver.chrome.verboseLogging", "true");
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        } else {
            logPrefs.enable(LogType.BROWSER, Level.ALL);
        }
        chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        if(null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            chromeOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }

        JSONObject headlessOptions = chromeConfiguration.getJSONObject("headlessOptions");
        isRunInHeadlessMode = headlessOptions.getBoolean("headless");

        chromeOptions.setHeadless(isRunInHeadlessMode);
        chromeOptions.setAcceptInsecureCerts(acceptInsecureCerts);

        JSONArray arguments = chromeConfiguration.getJSONArray("arguments");
        arguments.forEach(argument -> chromeOptions.addArguments(argument.toString()));

        if(isRunInHeadlessMode) {
            JSONArray includeArguments = headlessOptions.getJSONArray("include");
            includeArguments.forEach(argument -> chromeOptions.addArguments(argument.toString()));
        }

        if (null != testExecutionContext.getTestState(TEST_CONTEXT.MOBILE_EMULATION_DEVICE)) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", testExecutionContext.getTestStateAsString(TEST_CONTEXT.MOBILE_EMULATION_DEVICE));
            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        LOGGER.info("ChromeOptions: " + chromeOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(chromeOptions) : new ChromeDriver(chromeOptions);
        LOGGER.info("Chrome driver created");
        Capabilities capabilities = Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities() : ((ChromeDriver) driver).getCapabilities();
        userPersonaDriverCapabilities.put(forUserPersona, capabilities);
        LOGGER.info("Chrome driver capabilities extracted for further use");
        return driver;
    }

    private WebDriver createFirefoxDriver(String forUserPersona, TestExecutionContext testExecutionContext, JSONObject firefoxConfiguration) {

        boolean enableVerboseLogging = firefoxConfiguration.getBoolean("verboseLogging");
        boolean acceptInsecureCerts = firefoxConfiguration.getBoolean("acceptInsecureCerts");
        shouldBrowserBeMaximized = firefoxConfiguration.getBoolean("maximize");
        String proxyUrl = Runner.getProxyURL();

        FirefoxOptions firefoxOptions = new FirefoxOptions();

        setLogFileName(forUserPersona, testExecutionContext, "Firefox");

        FirefoxProfile firefoxProfile = new FirefoxProfile();
        JSONObject profileObject = firefoxConfiguration.getJSONObject("firefoxProfile");
        profileObject.keySet()
                     .forEach(key -> {
                         if(profileObject.get(key) instanceof Boolean) {
                             firefoxProfile.setPreference(key, profileObject.getBoolean(key));
                         } else if(profileObject.get(key) instanceof String) {
                             firefoxProfile.setPreference(key, profileObject.getString(key));
                         }
                     });
        firefoxOptions.setProfile(firefoxProfile);

        JSONObject preferencesObject = firefoxConfiguration.getJSONObject("preferences");
        preferencesObject.keySet()
                         .forEach(key -> {
                             if(preferencesObject.get(key) instanceof Boolean) {
                                 firefoxOptions.addPreference(key, preferencesObject.getBoolean(key));
                             } else if(preferencesObject.get(key) instanceof String) {
                                 firefoxOptions.addPreference(key, preferencesObject.getString(key));
                             }
                         });

        JSONArray arguments = firefoxConfiguration.getJSONArray("arguments");
        arguments.forEach(argument -> firefoxOptions.addArguments(argument.toString()));

        LoggingPreferences logPrefs = new LoggingPreferences();
        if(enableVerboseLogging) {
            firefoxOptions.setLogLevel(FirefoxDriverLogLevel.DEBUG);
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        } else {
            firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
            logPrefs.enable(LogType.BROWSER, Level.ALL);
        }
        firefoxOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        if(null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            firefoxOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }

        JSONObject headlessOptions = firefoxConfiguration.getJSONObject("headlessOptions");
        isRunInHeadlessMode = headlessOptions.getBoolean("headless");

        firefoxOptions.setHeadless(isRunInHeadlessMode);
        firefoxOptions.setAcceptInsecureCerts(acceptInsecureCerts);

        if (null != testExecutionContext.getTestState(TEST_CONTEXT.MOBILE_EMULATION_DEVICE)) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", testExecutionContext.getTestStateAsString(TEST_CONTEXT.MOBILE_EMULATION_DEVICE));
            firefoxOptions.setCapability("mobileEmulation", mobileEmulation);
        }

        LOGGER.info("FirefoxOptions: " + firefoxOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(firefoxOptions) : new FirefoxDriver(firefoxOptions);
        LOGGER.info("Firefox driver created");
        Capabilities capabilities = Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities() : ((FirefoxDriver) driver).getCapabilities();
        userPersonaDriverCapabilities.put(forUserPersona, capabilities);
        LOGGER.info("Firefox driver capabilities extracted for further use");
        return driver;
    }

    private WebDriver createSafariDriver(String forUserPersona, TestExecutionContext testExecutionContext, JSONObject safariConfigurations) {
        SafariOptions safariOptions = new SafariOptions();
        DesiredCapabilities caps = DesiredCapabilities.safari();
        boolean setUseTechnologyPreview = safariConfigurations.getBoolean("setUseTechnologyPreview");
        boolean acceptInsecureCerts = safariConfigurations.getBoolean("acceptInsecureCerts");
        String proxyUrl = Runner.getProxyURL();
        shouldBrowserBeMaximized = safariConfigurations.getBoolean("maximize");
        caps.setCapability("acceptInsecureCerts", acceptInsecureCerts);
        if(null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            safariOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }
        safariOptions.setUseTechnologyPreview(setUseTechnologyPreview); // setUseTechnologyPreview is false bydefault turn it on only if system supports
        LOGGER.info("SafariOptions: " + safariOptions.asMap());
        setLogFileName(forUserPersona, testExecutionContext, "Safari");
        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(safariOptions) : new SafariDriver(safariOptions);
        LOGGER.info("Safari driver created");
        Capabilities capabilities = Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities() : ((SafariDriver) driver).getCapabilities();
        userPersonaDriverCapabilities.put(forUserPersona, capabilities);
        LOGGER.info("Safari driver capabilities extracted for further use");
        // webpush notifications are disabled bydefault in safari , headless is not supported by safari browser and user profiles cannot be set in safari
        return driver;
    }

    private String getCapabilityFor(org.openqa.selenium.Capabilities capabilities, String name) {
        Object capability = capabilities.getCapability(name);
        return null == capability ? "" : capability.toString();
    }

    private void setLogFileName(String forUserPersona, TestExecutionContext testExecutionContext, String browserType) {
        String logFile = NOT_SET;
        String scenarioLogDir = Runner.USER_DIRECTORY + testExecutionContext.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        logFile = scenarioLogDir + File.separator + "deviceLogs" + File.separator + browserType + "-" + forUserPersona + ".log";

        File file = new File(logFile);
        file.getParentFile()
            .mkdirs();

        String logMessage = String.format("Creating %s logs in file: %s", browserType, logFile);
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
        System.setProperty("webdriver." + browserType.toLowerCase() + ".logfile", logFile);
        userPersonaBrowserLogs.put(forUserPersona, logFile);
    }

    @NotNull
    private RemoteWebDriver createRemoteWebDriver(MutableCapabilities chromeOptions) {
        try {
            String cloudName = Runner.getCloudName();
            String webDriverHubSuffix = "/wd/hub";
            String remoteUrl = "http://localhost:" + Runner.getRemoteDriverGridPort() + webDriverHubSuffix;
            if(cloudName.equalsIgnoreCase("headspin")) {
                String authenticationKey = Runner.getCloudKey();
                String capabilityFile = System.getProperty(CAPS);
                Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
                ArrayList hostMachinesList = (ArrayList) loadedCapabilityFile.get("hostMachines");
                Map hostMachines = (Map) hostMachinesList.get(0);
                String remoteServerURL = String.valueOf(hostMachines.get("machineIP"));
                remoteUrl = remoteServerURL.endsWith("/") ? remoteServerURL + authenticationKey + webDriverHubSuffix
                                                          : remoteServerURL + "/" + authenticationKey + webDriverHubSuffix;
                remoteUrl = remoteUrl.startsWith("https") ? remoteUrl : "https://" + remoteUrl;
            }
            LOGGER.info("Starting RemoteWebDriver using url: " + remoteUrl);
            RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(remoteUrl), chromeOptions);
            LOGGER.info("RemoteWebDriver created using url: " + remoteUrl);
            return remoteWebDriver;
        } catch(MalformedURLException e) {
            throw new EnvironmentSetupException("Unable to create a new RemoteWebDriver", e);
        }
    }

    public Driver createDriverFor(String userPersona, String appName, Platform forPlatform, TestExecutionContext context) {
        return createDriverFor(userPersona, appName, Runner.getBrowser(), forPlatform, context);
    }

    public Driver getDriverForUser(String userPersona) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getDriverForUser: Drivers available for userPersonas: " + userPersonaDrivers.keySet());
            throw new InvalidTestDataException(String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaDrivers.get(userPersona);
    }

    public String getDeviceNameForUser(String userPersona) {
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String deviceName = (String) userPersonaCapabilities.getCapability(DEVICE_NAME);
        if(null == deviceName) {
            LOGGER.info("getDeviceNameForUser: Capabilities available for userPersona: '" + userPersona + "': " + userPersonaCapabilities.asMap()
                                                                                                                                         .keySet());
            throw new InvalidTestDataException(String.format(DEVICE_NAME + " capability NOT found for user persona: '%s'\n%s", userPersona, userPersonaCapabilities.asMap()
                                                                                                                                                                   .keySet()));
        }
        return deviceName;
    }

    public Platform getPlatformForUser(String userPersona) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("getPlatformForUser: Platforms available for userPersonas: ");
            userPersonaPlatforms.keySet()
                                .forEach(key -> LOGGER.info("\tUser Persona: " + key + ": Platform: " + userPersonaPlatforms.get(key)
                                                                                                                            .name()));
            throw new InvalidTestDataException(String.format("No Driver found for user persona: '%s'", userPersona));
        }

        return userPersonaPlatforms.get(userPersona);
    }

    public void attachLogsAndCloseAllWebDrivers() {
        LOGGER.info("Close all drivers:");
        userPersonaDrivers.keySet()
                          .forEach(userPersona -> {
                              LOGGER.info("\tUser Persona: " + userPersona);
                              validateVisualTestResults(userPersona);
                              attachLogsAndCloseDriver(userPersona);
                          });
        for(AppiumDevice additionalDevice : additionalDevices) {
            LOGGER.info("Freeing device: " + additionalDevice.getDevice()
                                                             .getName());
            additionalDevice.freeDevice();
            additionalDevice.setChromeDriverPort(0);
        }

    }

    private void validateVisualTestResults(String userPersona) {
        Driver driver = userPersonaDrivers.get(userPersona);
        driver.getVisual()
              .handleTestResults(userPersona, driver.getType());
    }

    private void attachLogsAndCloseDriver(String userPersona) {
        Driver driver = userPersonaDrivers.get(userPersona);

        switch(driver.getType()) {
            case Driver.WEB_DRIVER:
                closeWebDriver(userPersona, driver);
                break;
            case Driver.APPIUM_DRIVER:
                if(Runner.platform.equals(Platform.windows)) {
                    closeWindowsAppOnMachine(userPersona, driver);
                } else {
                    closeAndroidAppOnDevice(userPersona, driver);
                }
                break;
            default:
                throw new InvalidTestDataException(String.format("Unexpected driver type: '%s'", driver.getType()));
        }
    }

    private void closeWebDriver(String userPersona,
                                @NotNull
                                        Driver driver) {
        String logFileName = userPersonaBrowserLogs.get(userPersona);
        String logMessage = String.format("Browser logs for user: %s" + "%nlogFileName: %s", userPersona, logFileName);
        LOGGER.info(logMessage);
        //        if (isRunningInCI()) {
        //            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        //        } else {
        ReportPortal.emitLog(logMessage, DEBUG, new Date(), new File(logFileName));
        //        }

        WebDriver webDriver = driver.getInnerDriver();
        if(null == webDriver) {
            logMessage = String.format("Strange. But WebDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            logMessage = String.format("Closing WebDriver for user: '%s'", userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
            webDriver.quit();
        }
    }

    private void closeWindowsAppOnMachine(String userPersona,
                                          @NotNull
                                                  Driver driver) {
        String logMessage;
        String appPackageName = Runner.getAppPackageName();
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if(null == appiumDriver) {
            logMessage = String.format("Strange. But WindowsDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            logMessage = String.format("Closing WindowsDriver for App '%s' for user '%s'", appPackageName, userPersona);
            LOGGER.info(logMessage);
            appiumDriver.closeApp();
            TestExecutionContext context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                                                        .getId());
            AppiumDriver<WebElement> atdAppiumDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            if(appiumDriver.equals(atdAppiumDriver)) {
                LOGGER.info(String.format("ATD will quit the driver for persona: '%s'", userPersona));
            } else {
                LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
                appiumDriver.quit();
            }
            logMessage = String.format("App: '%s' terminated", appPackageName);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        }
    }

    private void closeAndroidAppOnDevice(String userPersona,
                                         @NotNull
                                                 Driver driver) {
        String appPackageName = Runner.getAppPackageName();
        String logMessage;
        AppiumDriver appiumDriver = (AppiumDriver) driver.getInnerDriver();
        if(null == appiumDriver) {
            logMessage = String.format("Strange. But AppiumDriver for user '%s' already closed", userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            LOGGER.info("Terminate app: " + appPackageName);
            ApplicationState applicationState = appiumDriver.queryAppState(appPackageName);

            logMessage = String.format("App: '%s' Application state before closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());

            appiumDriver.closeApp();
            appiumDriver.terminateApp(appPackageName);
            applicationState = appiumDriver.queryAppState(appPackageName);
            logMessage = String.format("App: '%s' Application state after closing app: '%s'%n", appPackageName, applicationState);
            LOGGER.info(logMessage);
            TestExecutionContext context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                                                        .getId());
            AppiumDriver<WebElement> atdAppiumDriver = (AppiumDriver<WebElement>) context.getTestState(TEST_CONTEXT.APPIUM_DRIVER);
            if(appiumDriver.equals(atdAppiumDriver)) {
                LOGGER.info(String.format("ATD will quit the driver for persona: '%s'", userPersona));
            } else {
                LOGGER.info(String.format("Quit driver for persona: '%s'", userPersona));
                appiumDriver.quit();
            }
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        }
    }

    public Set<String> getAvailableUserPersonas() {
        return userPersonaDrivers.keySet();
    }

    public void assignNewPersonaToExistingDriver(String userPersona, String newUserPersona, TestExecutionContext context) {
        if(!userPersonaDrivers.containsKey(userPersona)) {
            LOGGER.info("assignNewPersonaToExistingDriver: Drivers available for userPersonas: " + userPersonaDrivers.keySet());
            throw new InvalidTestDataException(String.format("No Driver found for user persona: '%s'", userPersona));
        }

        Driver currentDriver = userPersonaDrivers.get(userPersona);
        Platform currentPlatform = userPersonaPlatforms.get(userPersona);
        Capabilities userPersonaCapabilities = userPersonaDriverCapabilities.get(userPersona);
        String logFileName = userPersonaBrowserLogs.get(userPersona);

        context.addTestState(TEST_CONTEXT.CURRENT_DRIVER, currentDriver);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, newUserPersona);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, currentPlatform);

        userPersonaDrivers.remove(userPersona);
        userPersonaPlatforms.remove(userPersona);
        userPersonaDriverCapabilities.remove(userPersona);
        userPersonaBrowserLogs.remove(userPersona);

        userPersonaDrivers.put(newUserPersona, currentDriver);
        userPersonaPlatforms.put(newUserPersona, currentPlatform);
        userPersonaDriverCapabilities.put(newUserPersona, userPersonaCapabilities);
        userPersonaBrowserLogs.put(newUserPersona, logFileName);
        LOGGER.info(String.format("assignNewPersonaToExistingDriver: Persona updated from '%s' to '%s'", userPersona, newUserPersona));
    }
}
