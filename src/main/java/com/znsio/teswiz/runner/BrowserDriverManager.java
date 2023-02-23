package com.znsio.teswiz.runner;

import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.JsonSchemaValidator;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.log4j.Logger;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import static com.znsio.teswiz.runner.Runner.*;
import static com.znsio.teswiz.runner.Setup.CAPS;

public class BrowserDriverManager {
    private static final Map<String, String> userPersonaBrowserLogs = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(BrowserDriverManager.class.getName());
    private static final int MAX_NUMBER_OF_WEB_DRIVERS = Runner.getMaxNumberOfWebDrivers();
    private static final String BROWSER_CONFIG_SCHEMA_FILE = "BrowserConfigSchema.json";
    private static int numberOfWebDriversUsed = 0;
    private static boolean shouldBrowserBeMaximized = false;
    private static boolean isRunInHeadlessMode = false;

    @NotNull
    static Driver createWebDriverForUser(String userPersona, String browserName,
                                         Platform forPlatform, TestExecutionContext context) {
        JSONObject browserConfig = null;
        LOGGER.info(String.format(
                "createWebDriverForUser: begin: userPersona: '%s', browserName: '%s', Platform: " + "'%s', Number of webdrivers: '%d'%n",
                userPersona, browserName, forPlatform.name(), numberOfWebDriversUsed));

        String baseUrl = getBaseUrl(userPersona);
        String appName = Drivers.getAppNamefor(userPersona);

        if(numberOfWebDriversUsed == 0) {
            browserConfig = getBrowserConfig();
            context.addTestState(TEST_CONTEXT.BROWSER_CONFIG, browserConfig);
            checkConnectivityToBaseUrl(baseUrl);
        } else {
            browserConfig = (JSONObject) context.getTestState(TEST_CONTEXT.BROWSER_CONFIG);
        }

        Driver currentDriver;
        if(Platform.web.equals(
                forPlatform) && numberOfWebDriversUsed == MAX_NUMBER_OF_WEB_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfWebDriversUsed, userPersona, forPlatform.name()));
        }
        String updatedTestName = context.getTestName() + "-" + userPersona;
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        if(numberOfWebDriversUsed < MAX_NUMBER_OF_WEB_DRIVERS) {
            LOGGER.info("Create new webdriver instance");
            WebDriver newWebDriver = createNewWebDriver(userPersona, browserName, context,
                                                        browserConfig);
            LOGGER.info("Webdriver instance created");
            newWebDriver.get(baseUrl);
            LOGGER.info("Navigated to baseUrl: " + baseUrl);
            currentDriver = new Driver(updatedTestName, forPlatform, runningOn, userPersona,
                                       appName, newWebDriver, isRunInHeadlessMode,
                                       shouldBrowserBeMaximized);
            LOGGER.info("New Driver with Visual instance created");
        } else {
            throw new InvalidTestDataException(String.format(
                    "Current number of WebDriver instances used: '%d'. " + "Unable to create " +
                    "more" + " than '%d' drivers for user persona: '%s' " + "on platform: '%s'",
                    numberOfWebDriversUsed, MAX_NUMBER_OF_WEB_DRIVERS, userPersona,
                    forPlatform.name()));
        }
        numberOfWebDriversUsed++;

        LOGGER.info(String.format(
                "createWebDriverForUser: done: userPersona: '%s', Platform: '%s', appName: '%s', "
                + "Number of webdrivers: '%d'",
                userPersona, forPlatform.name(), appName, numberOfWebDriversUsed));
        return currentDriver;
    }

    private static String getBaseUrl(String userPersona) {
        String providedBaseUrlKey = Runner.getBaseURLForWeb();

        String appName = Drivers.getAppNamefor(userPersona);
        if(!appName.equalsIgnoreCase(DEFAULT)) {
            providedBaseUrlKey = appName.toUpperCase() + "_BASE_URL";
        }
        LOGGER.info("Using BASE_URL key: " + providedBaseUrlKey);

        if(null == providedBaseUrlKey) {
            throw new InvalidTestDataException("baseUrl not provided");
        }
        String retrievedBaseUrl = String.valueOf(
                Runner.getFromEnvironmentConfiguration(providedBaseUrlKey));
        LOGGER.info("baseUrl: " + retrievedBaseUrl);
        return retrievedBaseUrl;
    }

    private static JSONObject getBrowserConfig() {
        String browserConfigFileContents = Runner.getBrowserConfigFileContents();
        String browserConfigFile = Runner.getBrowserConfigFile();
        return JsonSchemaValidator.validateJsonFileAgainstSchema(browserConfigFile,
                                                                 browserConfigFileContents,
                                                                 BROWSER_CONFIG_SCHEMA_FILE);
    }

    private static void checkConnectivityToBaseUrl(String baseUrl) {
        LOGGER.info(String.format("Check connectivity to baseUrl: '%s'", baseUrl));
        String[] curlCommand = new String[]{"curl -m 60 --insecure -I " + baseUrl};
        CommandLineExecutor.execCommand(curlCommand);
    }

    @NotNull
    private static WebDriver createNewWebDriver(String forUserPersona, String browserType,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject browserConfig) {

        DriverManagerType driverManagerType = setupBrowserDriver(testExecutionContext, browserType);

        WebDriver driver = null;
        switch(driverManagerType) {
            case CHROME:
                driver = createChromeDriver(forUserPersona, testExecutionContext,
                                            browserConfig.getJSONObject(
                                                    driverManagerType.getBrowserNameLowerCase()));
                break;
            case FIREFOX:
                driver = createFirefoxDriver(forUserPersona, testExecutionContext,
                                             browserConfig.getJSONObject(
                                                     driverManagerType.getBrowserNameLowerCase()));
                break;
            case SAFARI:
                driver = createSafariDriver(forUserPersona, testExecutionContext,
                                            browserConfig.getJSONObject(
                                                    driverManagerType.getBrowserNameLowerCase()));
                break;
            case EDGE:
            case IEXPLORER:
            case CHROMIUM:
            case OPERA:
                throw new InvalidTestDataException(
                        String.format("Browser: '%s' is NOT supported", browserType));
        }
        LOGGER.info("Driver created");

        if(shouldBrowserBeMaximized && !isRunInHeadlessMode) {
            driver.manage().window().maximize();
        } else if(isRunInHeadlessMode) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        }
        LOGGER.info("Reset browser window size");
        return driver;
    }

    @NotNull
    private static DriverManagerType setupBrowserDriver(TestExecutionContext testExecutionContext,
                                                        String browserType) {
        DriverManagerType driverManagerType = DriverManagerType.valueOf(browserType.toUpperCase());
        String webDriverManagerProxyUrl = (null == Runner.getWebDriverManagerProxyURL()) ? ""
                                                                                         :
                                          Runner.getWebDriverManagerProxyURL();
        LOGGER.info(String.format(
                "Using webDriverManagerProxyUrl: '%s' for getting the WebDriver for browser: '%s'",
                webDriverManagerProxyUrl, browserType));

        WebDriverManager webDriverManager = WebDriverManager.getInstance(driverManagerType)
                                                            .proxy(webDriverManagerProxyUrl);
        webDriverManager.setup();
        String downloadedDriverVersion = webDriverManager.getDownloadedDriverVersion();

        String message = String.format("Using %s browser version: %s", driverManagerType,
                                       downloadedDriverVersion);
        LOGGER.info(message);
        ReportPortal.emitLog(message, INFO, new Date());
        return driverManagerType;
    }

    @NotNull
    private static WebDriver createChromeDriver(String forUserPersona,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject chromeConfiguration) {

        boolean enableVerboseLogging = chromeConfiguration.getBoolean("verboseLogging");
        boolean acceptInsecureCerts = chromeConfiguration.getBoolean("acceptInsecureCerts");
        shouldBrowserBeMaximized = chromeConfiguration.getBoolean("maximize");
        String proxyUrl = Runner.getProxyURL();

        ChromeOptions chromeOptions = new ChromeOptions();

        setLogFileName(forUserPersona, testExecutionContext, "Chrome");

        JSONArray excludeSwitches = chromeConfiguration.getJSONArray("excludeSwitches");
        List<String> excludeSwitchesAsString = new ArrayList<>();
        excludeSwitches.forEach(
                switchToBeExcluded -> excludeSwitchesAsString.add(switchToBeExcluded.toString()));
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

        if(null != testExecutionContext.getTestState(TEST_CONTEXT.MOBILE_EMULATION_DEVICE)) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", testExecutionContext.getTestStateAsString(
                    TEST_CONTEXT.MOBILE_EMULATION_DEVICE));
            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        }

        LOGGER.info("ChromeOptions: " + chromeOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(chromeOptions)
                                                  : new ChromeDriver(chromeOptions);
        LOGGER.info("Chrome driver created");
        Capabilities capabilities =
                Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities()
                                       : ((ChromeDriver) driver).getCapabilities();
        Drivers.addUserPersonaDriverCapabilities(forUserPersona, capabilities);
        LOGGER.info("Chrome driver capabilities extracted for further use");
        return driver;
    }

    private static WebDriver createFirefoxDriver(String forUserPersona,
                                                 TestExecutionContext testExecutionContext,
                                                 JSONObject firefoxConfiguration) {

        boolean enableVerboseLogging = firefoxConfiguration.getBoolean("verboseLogging");
        boolean acceptInsecureCerts = firefoxConfiguration.getBoolean("acceptInsecureCerts");
        shouldBrowserBeMaximized = firefoxConfiguration.getBoolean("maximize");
        String proxyUrl = Runner.getProxyURL();

        FirefoxOptions firefoxOptions = new FirefoxOptions();

        setLogFileName(forUserPersona, testExecutionContext, "Firefox");

        FirefoxProfile firefoxProfile = new FirefoxProfile();
        JSONObject profileObject = firefoxConfiguration.getJSONObject("firefoxProfile");
        profileObject.keySet().forEach(key -> {
            if(profileObject.get(key) instanceof Boolean) {
                firefoxProfile.setPreference(key, profileObject.getBoolean(key));
            } else if(profileObject.get(key) instanceof String) {
                firefoxProfile.setPreference(key, profileObject.getString(key));
            }
        });
        firefoxOptions.setProfile(firefoxProfile);

        JSONObject preferencesObject = firefoxConfiguration.getJSONObject("preferences");
        preferencesObject.keySet().forEach(key -> {
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

        LOGGER.info("FirefoxOptions: " + firefoxOptions.asMap());

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(firefoxOptions)
                                                  : new FirefoxDriver(firefoxOptions);
        LOGGER.info("Firefox driver created");
        Capabilities capabilities =
                Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities()
                                       : ((FirefoxDriver) driver).getCapabilities();
        Drivers.addUserPersonaDriverCapabilities(forUserPersona, capabilities);
        LOGGER.info("Firefox driver capabilities extracted for further use");
        return driver;
    }

    private static WebDriver createSafariDriver(String forUserPersona,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject safariConfigurations) {
        SafariOptions safariOptions = new SafariOptions();
        DesiredCapabilities caps = DesiredCapabilities.safari();
        boolean setUseTechnologyPreview = safariConfigurations.getBoolean(
                "setUseTechnologyPreview");
        boolean acceptInsecureCerts = safariConfigurations.getBoolean("acceptInsecureCerts");
        String proxyUrl = Runner.getProxyURL();
        shouldBrowserBeMaximized = safariConfigurations.getBoolean("maximize");
        caps.setCapability("acceptInsecureCerts", acceptInsecureCerts);
        if(null != proxyUrl) {
            LOGGER.info("Setting Proxy for browser: " + proxyUrl);
            safariOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }
        safariOptions.setUseTechnologyPreview(
                setUseTechnologyPreview); // setUseTechnologyPreview is false bydefault turn it
        // on only if system supports
        LOGGER.info("SafariOptions: " + safariOptions.asMap());
        setLogFileName(forUserPersona, testExecutionContext, "Safari");
        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(safariOptions)
                                                  : new SafariDriver(safariOptions);
        LOGGER.info("Safari driver created");
        Capabilities capabilities =
                Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities()
                                       : ((SafariDriver) driver).getCapabilities();
        Drivers.addUserPersonaDriverCapabilities(forUserPersona, capabilities);
        LOGGER.info("Safari driver capabilities extracted for further use");
        // webpush notifications are disabled bydefault in safari , headless is not supported by
        // safari browser and user profiles cannot be set in safari
        return driver;
    }

    private static void setLogFileName(String forUserPersona,
                                       TestExecutionContext testExecutionContext,
                                       String browserType) {
        String logFile = NOT_SET;
        String scenarioLogDir = Runner.USER_DIRECTORY + testExecutionContext.getTestStateAsString(
                TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        logFile =
                scenarioLogDir + File.separator + "deviceLogs" + File.separator + browserType +
                "-" + forUserPersona + ".log";

        File file = new File(logFile);
        file.getParentFile().mkdirs();

        String logMessage = String.format("Creating %s logs in file: %s", browserType, logFile);
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
        System.setProperty("webdriver." + browserType.toLowerCase() + ".logfile", logFile);
        userPersonaBrowserLogs.put(forUserPersona, logFile);
    }

    @NotNull
    private static RemoteWebDriver createRemoteWebDriver(MutableCapabilities chromeOptions) {
        try {
            String cloudName = Runner.getCloudName();
            String webDriverHubSuffix = "/wd/hub";
            String remoteUrl =
                    "http://localhost:" + Runner.getRemoteDriverGridPort() + webDriverHubSuffix;
            if(cloudName.equalsIgnoreCase("headspin")) {
                String authenticationKey = Runner.getCloudKey();
                String capabilityFile = System.getProperty(CAPS);
                Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
                ArrayList hostMachinesList = (ArrayList) loadedCapabilityFile.get("hostMachines");
                Map hostMachines = (Map) hostMachinesList.get(0);
                String remoteServerURL = String.valueOf(hostMachines.get("machineIP"));
                remoteUrl = remoteServerURL.endsWith("/")
                            ? remoteServerURL + authenticationKey + webDriverHubSuffix
                            : remoteServerURL + "/" + authenticationKey + webDriverHubSuffix;
                remoteUrl = remoteUrl.startsWith("https") ? remoteUrl : "https://" + remoteUrl;
            }
            LOGGER.info("Starting RemoteWebDriver using url: " + remoteUrl);
            RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(remoteUrl),
                                                                  chromeOptions);
            LOGGER.info("RemoteWebDriver created using url: " + remoteUrl);
            return remoteWebDriver;
        } catch(MalformedURLException e) {
            throw new EnvironmentSetupException("Unable to create a new RemoteWebDriver", e);
        }
    }

    static void closeWebDriver(String userPersona,
                               @NotNull
                               Driver driver) {
        String logFileName = userPersonaBrowserLogs.get(userPersona);
        String logMessage = String.format("Browser logs for user: %s" + "%nlogFileName: %s",
                                          userPersona, logFileName);
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date(), new File(logFileName));

        WebDriver webDriver = driver.getInnerDriver();
        if(null == webDriver) {
            logMessage = String.format("Strange. But WebDriver for user '%s' already closed",
                                       userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
        } else {
            logMessage = String.format("Closing WebDriver for user: '%s'", userPersona);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
            webDriver.quit();
        }
    }

    public static String getBrowserLogFileNameFor(String userPersona) {
        return userPersonaBrowserLogs.get(userPersona);
    }

    public static void removeBrowserLogsFor(String userPersona) {
        userPersonaBrowserLogs.remove(userPersona);
    }

    public static void addBrowserLogFileNamefor(String newUserPersona, String logFileName) {
        userPersonaBrowserLogs.put(newUserPersona, logFileName);
    }
}
