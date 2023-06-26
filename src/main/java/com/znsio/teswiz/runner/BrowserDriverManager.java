package com.znsio.teswiz.runner;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.JsonSchemaValidator;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
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

class BrowserDriverManager {
    private static final Logger LOGGER = Logger.getLogger(BrowserDriverManager.class.getName());
    private static final int MAX_NUMBER_OF_WEB_DRIVERS = Runner.getMaxNumberOfWebDrivers();
    private static final String BROWSER_CONFIG_SCHEMA_FILE = "BrowserConfigSchema.json";
    private static final String ACCEPT_INSECURE_CERTS = "acceptInsecureCerts";
    private static final String VERBOSE_LOGGING = "verboseLogging";
    private static final String MAXIMIZE = "maximize";
    private static final String EXCLUDE_SWITCHES = "excludeSwitches";
    private static final String SETTING_PROXY_FOR_BROWSER = "Setting Proxy for browser: ";
    private static int numberOfWebDriversUsed = 0;
    private static boolean shouldBrowserBeMaximized = false;
    private static boolean isRunInHeadlessMode = false;

    private BrowserDriverManager() {
        LOGGER.debug("BrowserDriverManager - private constructor");
    }

    @NotNull
    static Driver createWebDriverForUser(String userPersona, String browserName,
                                         Platform forPlatform, TestExecutionContext context) {
        LOGGER.info(String.format(
                "createWebDriverForUser: begin: userPersona: '%s', browserName: '%s', Platform: " + "'%s', Number of webdrivers: '%d'%n",
                userPersona, browserName, forPlatform.name(), numberOfWebDriversUsed));
        LOGGER.info("Active thread count: " + Thread.activeCount());

        String baseUrl = getBaseUrl(userPersona);
        String appName = Drivers.getAppNamefor(userPersona);

        JSONObject browserConfig = (JSONObject) context.getTestState(TEST_CONTEXT.BROWSER_CONFIG);
        if(null == browserConfig) {
            browserConfig = getBrowserConfig();
            context.addTestState(TEST_CONTEXT.BROWSER_CONFIG, browserConfig);
        }
        context.addTestState(TEST_CONTEXT.BROWSER_CONFIG, browserConfig);

        checkConnectivityToBaseUrl(baseUrl);

        Driver currentDriver;
        if(numberOfWebDriversUsed == MAX_NUMBER_OF_WEB_DRIVERS) {
            throw new InvalidTestDataException(String.format(
                    "Unable to create more than '%d' drivers for user persona: '%s' on platform: "
                    + "'%s'",
                    numberOfWebDriversUsed, userPersona, forPlatform.name()));
        }
        String updatedTestName = context.getTestName() + "-" + userPersona;
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        if(numberOfWebDriversUsed < MAX_NUMBER_OF_WEB_DRIVERS) {
            LOGGER.info(String.format(
                    "Create new webdriver instance for: %s, on: %s, with browserConfig: %s",
                    userPersona, browserName, browserConfig));
            WebDriver newWebDriver = createNewWebDriver(userPersona, browserName, context,
                                                        browserConfig);
            LOGGER.info("Webdriver instance created");
            newWebDriver.get(baseUrl);
            LOGGER.info("Navigated to baseUrl: " + baseUrl);
            currentDriver = new Driver(updatedTestName, forPlatform, userPersona, appName,
                                       newWebDriver, isRunInHeadlessMode);
            numberOfWebDriversUsed++;
            LOGGER.info(
                    "New Driver with Visual instance created: numberOfWebDriversUsed: " + numberOfWebDriversUsed);
        } else {
            throw new InvalidTestDataException(String.format(
                    "Current number of WebDriver instances used: '%d'. " + "Unable to create " +
                    "more" + " than '%d' drivers for user persona: '%s' " + "on platform: '%s'",
                    numberOfWebDriversUsed, MAX_NUMBER_OF_WEB_DRIVERS, userPersona,
                    forPlatform.name()));
        }

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
        LOGGER.info(String.format("Using BASE_URL key: %s", providedBaseUrlKey));

        if(null == providedBaseUrlKey) {
            throw new InvalidTestDataException("baseUrl not provided");
        }
        String retrievedBaseUrl = String.valueOf(
                Runner.getFromEnvironmentConfiguration(providedBaseUrlKey));
        LOGGER.info(String.format("baseUrl: %s", retrievedBaseUrl));
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
        if(numberOfWebDriversUsed == 0) {
            LOGGER.info(String.format("Check connectivity to baseUrl: '%s'", baseUrl));
            String[] curlCommand = new String[]{"curl -m 60 --insecure -I " + baseUrl};
            CommandLineExecutor.execCommand(curlCommand);
        }
    }

    @NotNull
    private static WebDriver createNewWebDriver(String forUserPersona, String browserName,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject browserConfig) {

        DriverManagerType driverManagerType = setupBrowserDriver(testExecutionContext, browserName);
        WebDriver driver = null;
        LOGGER.info(
                BrowserDriverManager.class.getName() + "-createNewWebDriver: " + driverManagerType.getBrowserNameLowerCase());
        JSONObject browserConfigForBrowserType = browserConfig.getJSONObject(
                driverManagerType.getBrowserNameLowerCase());
        switch(driverManagerType) {
            case CHROME:
                driver = createChromeDriver(forUserPersona, testExecutionContext,
                                            browserConfigForBrowserType);
                break;
            case FIREFOX:
                driver = createFirefoxDriver(forUserPersona, testExecutionContext,
                                             browserConfigForBrowserType);
                break;
            case SAFARI:
                driver = createSafariDriver(forUserPersona, testExecutionContext,
                                            browserConfigForBrowserType);
                break;
            case EDGE:
            case IEXPLORER:
            case CHROMIUM:
            case OPERA:
                throw new InvalidTestDataException(
                        String.format("Browser: '%s' is NOT supported", browserName));
        }
        LOGGER.info("Driver created");

        if(null == driver) {
            throw new EnvironmentSetupException(
                    String.format("Unable to create %s browser driver for user: %s", browserName,
                                  forUserPersona));
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

        // TODO - get browser version from local or container. What about cloud?
        WebDriverManager webDriverManager = WebDriverManager.getInstance(driverManagerType)
                                                            .proxy(webDriverManagerProxyUrl);
        webDriverManager.setup();
        String downloadedDriverVersion = webDriverManager.getDownloadedDriverVersion();

        String message = String.format("Using %s browser version: %s", driverManagerType,
                                       downloadedDriverVersion);
        LOGGER.info(message);
        ReportPortalLogger.logInfoMessage(message);
        return driverManagerType;
    }

    @NotNull
    private static WebDriver createChromeDriver(String forUserPersona,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject chromeConfiguration) {

        boolean enableVerboseLogging = chromeConfiguration.getBoolean(VERBOSE_LOGGING);
        boolean acceptInsecureCerts = chromeConfiguration.getBoolean(ACCEPT_INSECURE_CERTS);
        shouldBrowserBeMaximized = chromeConfiguration.getBoolean(MAXIMIZE);
        String proxyUrl = Runner.getProxyURL();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--disable-gpu");

        setLogFileName(forUserPersona, testExecutionContext, "Chrome");

        JSONArray excludeSwitches = chromeConfiguration.getJSONArray(EXCLUDE_SWITCHES);
        List<String> excludeSwitchesAsString = new ArrayList<>();
        excludeSwitches.forEach(
                switchToBeExcluded -> excludeSwitchesAsString.add(switchToBeExcluded.toString()));
        chromeOptions.setExperimentalOption(EXCLUDE_SWITCHES, excludeSwitchesAsString);

        JSONObject excludedSchemes = chromeConfiguration.getJSONObject("excludedSchemes");
        JSONObject preferences = chromeConfiguration.getJSONObject("preferences");
        preferences.put("protocol_handler.excluded_schemes", excludedSchemes);
        chromeOptions.setExperimentalOption("prefs", preferences);

        LOGGER.info("Set Logging preferences");
        LoggingPreferences logPrefs = new LoggingPreferences();
        if(enableVerboseLogging) {
            System.setProperty("webdriver.chrome.verboseLogging", "true");
            chromeOptions.setLogLevel(ChromeDriverLogLevel.DEBUG);
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        } else {
            chromeOptions.setLogLevel(ChromeDriverLogLevel.INFO);
            logPrefs.enable(LogType.BROWSER, Level.INFO);
            logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
        }
        chromeOptions.setCapability(ChromeOptions.LOGGING_PREFS, logPrefs);

        if(null != proxyUrl) {
            LOGGER.info(SETTING_PROXY_FOR_BROWSER + proxyUrl);
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

        LOGGER.info(String.format("ChromeOptions: %s", chromeOptions.asMap()));

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(chromeOptions)
                                                  : new ChromeDriver(chromeOptions);
        LOGGER.info("Chrome driver created");
        Capabilities capabilities =
                Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities()
                                       : ((ChromeDriver) driver).getCapabilities();
        Drivers.addUserPersonaDriverCapabilities(forUserPersona, capabilities);
        LOGGER.info("Chrome driver capabilities extracted for further use");
        manageWindowSizeAndHeadlessMode(driver);
        return driver;
    }

    private static void manageWindowSizeAndHeadlessMode(WebDriver driver) {
        if(shouldBrowserBeMaximized && !isRunInHeadlessMode) {
            driver.manage().window().maximize();
        } else if(isRunInHeadlessMode) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        }
    }

    private static WebDriver createFirefoxDriver(String forUserPersona,
                                                 TestExecutionContext testExecutionContext,
                                                 JSONObject firefoxConfiguration) {

        boolean enableVerboseLogging = firefoxConfiguration.getBoolean(VERBOSE_LOGGING);
        boolean acceptInsecureCerts = firefoxConfiguration.getBoolean(ACCEPT_INSECURE_CERTS);
        shouldBrowserBeMaximized = firefoxConfiguration.getBoolean(MAXIMIZE);
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
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        } else {
            firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
            logPrefs.enable(LogType.BROWSER, Level.INFO);
            logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
        }
        firefoxOptions.setCapability("moz:firefoxOptions",logPrefs);

        if(null != proxyUrl) {
            LOGGER.info(SETTING_PROXY_FOR_BROWSER + proxyUrl);
            firefoxOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }

        JSONObject headlessOptions = firefoxConfiguration.getJSONObject("headlessOptions");
        isRunInHeadlessMode = headlessOptions.getBoolean("headless");

        firefoxOptions.setHeadless(isRunInHeadlessMode);
        firefoxOptions.setAcceptInsecureCerts(acceptInsecureCerts);

        LOGGER.info(String.format("FirefoxOptions: %s", firefoxOptions.asMap()));

        WebDriver driver = Runner.isRunningInCI() ? createRemoteWebDriver(firefoxOptions)
                                                  : new FirefoxDriver(firefoxOptions);
        LOGGER.info("Firefox driver created");
        Capabilities capabilities =
                Runner.isRunningInCI() ? ((RemoteWebDriver) driver).getCapabilities()
                                       : ((FirefoxDriver) driver).getCapabilities();
        Drivers.addUserPersonaDriverCapabilities(forUserPersona, capabilities);
        LOGGER.info("Firefox driver capabilities extracted for further use");
        manageWindowSizeAndHeadlessMode(driver);
        return driver;
    }

    private static WebDriver createSafariDriver(String forUserPersona,
                                                TestExecutionContext testExecutionContext,
                                                JSONObject safariConfigurations) {
        SafariOptions safariOptions = new SafariOptions();
        boolean setUseTechnologyPreview = safariConfigurations.getBoolean(
                "setUseTechnologyPreview");
        boolean acceptInsecureCerts = safariConfigurations.getBoolean(ACCEPT_INSECURE_CERTS);
        String proxyUrl = Runner.getProxyURL();
        shouldBrowserBeMaximized = safariConfigurations.getBoolean(MAXIMIZE);
        safariOptions.setCapability(ACCEPT_INSECURE_CERTS, acceptInsecureCerts);
        if(null != proxyUrl) {
            LOGGER.info(String.format("%s%s", SETTING_PROXY_FOR_BROWSER, proxyUrl));
            safariOptions.setProxy(new Proxy().setHttpProxy(proxyUrl));
        }
        safariOptions.setUseTechnologyPreview(
                setUseTechnologyPreview); // setUseTechnologyPreview is false bydefault turn it
        // on only if system supports
        LOGGER.info(String.format("SafariOptions: %s", safariOptions.asMap()));
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
        manageWindowSizeAndHeadlessMode(driver);
        return driver;
    }

    private static void setLogFileName(String forUserPersona,
                                       TestExecutionContext testExecutionContext,
                                       String browserType) {
        String scenarioLogDir = Runner.USER_DIRECTORY + testExecutionContext.getTestStateAsString(
                TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        browserType = browserType.toLowerCase();
        String logFile = String.format("%s%sdeviceLogs%s%s-%s.log", scenarioLogDir, File.separator,
                                       File.separator, browserType, forUserPersona);

        File file = new File(logFile);
        file.getParentFile().mkdirs();

        String logMessage = String.format("Creating %s logs in file: %s", browserType, logFile);
        LOGGER.info(logMessage);
        ReportPortalLogger.logDebugMessage(logMessage);
        System.setProperty("webdriver." + browserType + ".logfile", logFile);
        addBrowserLogFileNameFor(forUserPersona, Platform.web.name(), browserType, logFile);
    }

    private static void addBrowserLogFileNameFor(String userPersona, String forPlatform,
                                                 String browserType, String logFileName) {
        UserPersonaDetails userPersonaDetails = Drivers.getUserPersonaDetails(
                Runner.getTestExecutionContext(Thread.currentThread().getId()));
        userPersonaDetails.addBrowserLogFileNameFor(userPersona, forPlatform, browserType,
                                                    logFileName);
    }

    @NotNull
    private static RemoteWebDriver createRemoteWebDriver(MutableCapabilities capabilities) {
        try {
            String cloudName = Runner.getCloudName();
            String webDriverHubSuffix = "/wd/hub";
            String remoteUrl =
                    "http://localhost:" + Runner.getRemoteDriverGridPort() + webDriverHubSuffix;
            if (cloudName.equalsIgnoreCase("headspin")) {
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
            } else if (cloudName.equalsIgnoreCase("browserstack")) {
                String authenticationUser = Runner.getCloudUser();
                String authenticationKey = Runner.getCloudKey();
                remoteUrl = "https://" + authenticationUser + ":" + authenticationKey + "@hub.browserstack.com/wd/hub";
                capabilities = BrowserStackSetup.updateBrowserStackCapabilities(capabilities);
            }
            
            LOGGER.info(String.format("Starting RemoteWebDriver using url: %s", remoteUrl));
            RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(remoteUrl),
                    capabilities);
            LOGGER.info(String.format("RemoteWebDriver created using url: %s", remoteUrl));
            return remoteWebDriver;
        } catch (MalformedURLException e) {
            throw new EnvironmentSetupException("Unable to create a new RemoteWebDriver", e);
        }
    }

    static void closeWebDriver(String userPersona,
                               @NotNull
                               Driver driver) {
        String browserNameForUser = Drivers.getBrowserNameForUser(userPersona);
        String logFileName = getBrowserLogFileNameFor(userPersona, Platform.web.name(),
                                                      browserNameForUser);
        --numberOfWebDriversUsed;
        LOGGER.info(String.format("Reduced numberOfWebDriversUsed: %d", numberOfWebDriversUsed));
        String logMessage = String.format("Browser logs for user: %s" + "%nlogFileName: %s",
                                          userPersona, logFileName);
        LOGGER.info(logMessage);
        ReportPortalLogger.attachFileInReportPortal(logMessage, new File(logFileName));

        WebDriver webDriver = driver.getInnerDriver();
        if(null == webDriver) {
            logMessage = String.format("Strange. But WebDriver for user '%s' already closed",
                                       userPersona);
            LOGGER.info(logMessage);
        } else {
            logMessage = String.format("Closing WebDriver for user: '%s'", userPersona);
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
            webDriver.quit();
        }
    }

    private static String getBrowserLogFileNameFor(String userPersona, String forPlatform,
                                                   String browserType) {
        UserPersonaDetails userPersonaDetails = Drivers.getUserPersonaDetails(
                Runner.getTestExecutionContext(Thread.currentThread().getId()));
        return userPersonaDetails.getBrowserLogFileNameFor(userPersona, forPlatform, browserType);
    }

}
