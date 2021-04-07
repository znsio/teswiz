package com.znsio.e2e.runner;

import com.appium.utils.Variable;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.github.device.Device;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.znsio.e2e.entities.APPLITOOLS;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.exceptions.TestExecutionFailedException;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.JsonFile;
import com.znsio.e2e.tools.Visual;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import io.cucumber.core.cli.Main;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.appium.utils.Variable.*;

public class Runner {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().startsWith("windows");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().startsWith("mac");
    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String USER_NAME = System.getProperty("user.name");
    private static final String BASE_URL_FOR_WEB = "BASE_URL_FOR_WEB";
    private static final String APP_NAME = "APP_NAME";
    private static final String IS_VISUAL = "IS_VISUAL";
    private static final String CHROME = "chrome";
    private static final String PLUGIN = "--plugin";
    private static final String tempDirectory = "temp";
    public static final String NOT_SET = "not-set";
    private static final Platform DEFAULT_PLATFORM = Platform.android;
    private static final int DEFAULT_PARALLEL = 1;
    private static final ArrayList<String> cukeArgs = new ArrayList<>();
    private static final String BRANCH_NAME = "BRANCH_NAME";
    private static final String LOG_PROPERTIES_FILE = "LOG_PROPERTIES_FILE";
    private static final String DEFAULT_LOG_DIR = "target";
    private static final String APP_PATH = "APP_PATH";
    private static final String BROWSER = "BROWSER";
    private static final String CAPS = "CAPS";
    private static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String DEVICE_LAB_URL = "DEVICE_LAB_URL";
    private static final String ENVIRONMENT_CONFIG_FILE = "ENVIRONMENT_CONFIG_FILE";
    private static final String EXECUTED_ON = "EXECUTED_ON";
    private static final String LAUNCH_NAME = "LAUNCH_NAME";
    private static final String LOG_DIR = "LOG_DIR";
    private static final String PARALLEL = "PARALLEL";
    private static final String PLATFORM = "PLATFORM";
    private static final String RUN_IN_CI = "RUN_IN_CI";
    private static final String TAG = "TAG";
    private static final String TARGET_ENVIRONMENT = "TARGET_ENVIRONMENT";
    private static final String TEST_DATA_FILE = "TEST_DATA_FILE";
    private static final Map<String, String> configs = new HashMap();
    private static final Map<String, Boolean> configsBoolean = new HashMap();
    private static final Map<String, Integer> configsInteger = new HashMap();
    private static final String APP_PACKAGE_NAME = "APP_PACKAGE_NAME";
    private static final String APPLITOOLS_CONFIGURATION = "APPLITOOLS_CONFIGURATION";
    public static Platform platform = Platform.android;
    private static Map<String, Map> environmentConfiguration;
    private static Map<String, Map> testDataForEnvironment;
    private static Map applitoolsConfiguration = new HashMap();
    private final Properties properties;
    private final String DEFAULT_LOG_PROPERTIES_FILE = "src/main/resources/log4j.properties";
    private List<Device> devices;
    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());

    public Runner () {
        throw new InvalidTestDataException("Required args not provided to Runner");
    }

    public Runner (String configFilePath, String stepDefDirName, String featuresDirName) {
        Path path = Paths.get(configFilePath);
        if (!Files.exists(path)) {
            throw new InvalidTestDataException(String.format("Invalid path ('%s') provided for config", configFilePath));
        }
        properties = loadProperties(configFilePath);
        loadAndUpdateConfigParameters(configFilePath);

        PropertyConfigurator.configure(configs.get(LOG_PROPERTIES_FILE));
        System.setProperty(LOG_DIR, configs.get(LOG_DIR));
        LOGGER.info("Runner called from user directory: " + Runner.USER_DIRECTORY);
        printLoadedConfigProperties(configFilePath);

        cleanupDirectories();
        setupDirectories();

        environmentConfiguration = loadEnvironmentConfiguration(configs.get(TARGET_ENVIRONMENT));
        testDataForEnvironment = loadTestDataForEnvironment(configs.get(TARGET_ENVIRONMENT));
        setupExecutionEnvironment();

        run(cukeArgs, stepDefDirName, featuresDirName);
    }

    private void setupExecutionEnvironment () {
        getPlatformTagsAndLaunchName();
        addCucumberPlugsToArgs();
        setupAndroidExecution();
        setupWebExecution();
        getBranchName();
        initialiseApplitoolsConfiguration();

        String rpAttributes =
                "AutomationBranch:" + configs.get(BRANCH_NAME) + "; " +
                        "ExecutedOn:" + configs.get(EXECUTED_ON) + "; " +
                        "Installer:" + configs.get(APP_PATH) + "; " +
                        "OS:" + OS_NAME + "; " +
                        "ParallelCount:" + configsInteger.get(PARALLEL) + "; " +
                        "Platform:" + platform.name() + "; " +
                        "RunOnCloud:" + configsBoolean.get(RUN_IN_CI) + "; " +
                        "Tags:" + configs.get(TAG) + "; " +
                        "TargetEnvironment:" + configs.get(TARGET_ENVIRONMENT) + "; " +
                        "Username:" + USER_NAME + "; " +
                        "VisualEnabled:" + configsBoolean.get(IS_VISUAL) + "; ";

        LOGGER.info("ReportPortal Test Execution Attributes: " + rpAttributes);

        System.setProperty("CONFIG_FILE", configs.get(CONFIG_FILE));
        System.setProperty("CAPS", configs.get(CAPS));
        System.setProperty("Platform", platform.name());
        System.setProperty("atd_" + platform.name() + "_app_local", configs.get(APP_PATH));
        System.setProperty("rp.description", configs.get(APP_NAME) + " End-2-End scenarios on " + platform.name());
        System.setProperty("rp.launch", configs.get(LAUNCH_NAME));
        System.setProperty("rp.attributes", rpAttributes);
    }

    public static boolean isVisualTestingEnabled () {
        return configsBoolean.get(IS_VISUAL);
    }

    public static void remove (long threadId) {
        SessionContext.remove(threadId);
    }

    public static String getFromEnvironmentConfiguration (String key) {
        try {
            return String.valueOf(environmentConfiguration.get(key));
        } catch (NullPointerException npe) {
            throw new InvalidTestDataException(String.format("Invalid key name ('%s') provided", key), npe);
        }
    }

    public static String getTestData (String key) {
        try {
            return String.valueOf(testDataForEnvironment.get(key));
        } catch (NullPointerException npe) {
            throw new InvalidTestDataException(String.format("Invalid key name ('%s') provided", key), npe);
        }
    }

    public static void main (String[] args) {
        LOGGER.info("unified-e2e Runner");
        LOGGER.info("Provided parameters:");
        for (int i = 0; i < args.length; i++) {
            LOGGER.info("\t" + args[i]);
        }
        if (args.length != 3) {
            throw new InvalidTestDataException("Expected following parameters: 'String configFilePath, String stepDefDirName, String featuresDirName, String logDirName");
        }
        new Runner(args[0], args[1], args[2]);
    }

    public static Driver fetchDriver (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona);
    }

    public static TestExecutionContext getTestExecutionContext (long threadId) {
        return SessionContext.getTestExecutionContext(threadId);
    }

    public static Visual fetchEyes (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona).getVisual();
    }

    private static Map<String, Map> loadEnvironmentConfiguration (String environment) {
        String envConfigFile = configs.get(ENVIRONMENT_CONFIG_FILE);
        LOGGER.info("Loading environment configuration from ENVIRONMENT_CONFIG_FILE: "
                + envConfigFile
                +" for environment: "
                + environment);
        return (NOT_SET.equalsIgnoreCase(envConfigFile))
                ? new HashMap<>()
                : JsonFile.getNodeValueAsMapFromJsonFile(environment, envConfigFile);
    }

    public static SoftAssertions getSoftAssertion (long threadId) {
        return (SoftAssertions) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.SOFT_ASSERTIONS);
    }

    public static Driver setCurrentDriverForUser (String userPersona, Platform forPlatform, TestExecutionContext context) {
        Drivers allDrivers = (Drivers) context.getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.setDriverFor(userPersona, forPlatform, context);
    }

    public static Platform fetchPlatform (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getPlatformForUser(userPersona);
    }

    public static void closeAllDrivers (long threadId) {
        TestExecutionContext context = getTestExecutionContext(threadId);
        Drivers allDrivers = (Drivers) context.getTestState(TEST_CONTEXT.ALL_DRIVERS);
        allDrivers.attachLogsAndCloseAllWebDrivers(context);
    }

    private static Map<String, Map> loadTestDataForEnvironment (String environment) {
        String testDataFile = configs.get(TEST_DATA_FILE);
        LOGGER.info("Loading test data from TEST_DATA_FILE: "
                + testDataFile
                +" for environment: "
                + environment);
        return (NOT_SET.equalsIgnoreCase(testDataFile)) ? new HashMap<>() : JsonFile.getNodeValueAsMapFromJsonFile(environment, testDataFile);
    }

    public static String getTargetEnvironment () {
        return configs.get(TARGET_ENVIRONMENT);
    }

    public static String getBaseURLForWeb () {
        return configs.get(BASE_URL_FOR_WEB);
    }

    public static String getAppPackageName () {
        return configs.get(APP_PACKAGE_NAME);
    }

    public static boolean isRunningInCI () {
        return configsBoolean.get(RUN_IN_CI);
    }

    public static Map initialiseApplitoolsConfiguration () {
        if (applitoolsConfiguration.isEmpty()) {
            getApplitoolsConfigFromProvidedConfigFile();
            applitoolsConfiguration.put(APPLITOOLS.APP_NAME, configs.get(Runner.APP_NAME));
            applitoolsConfiguration.put(APPLITOOLS.API_KEY, Variable.getOverriddenStringValue("APPLITOOLS_API_KEY", String.valueOf(applitoolsConfiguration.get(APPLITOOLS.API_KEY))));
            applitoolsConfiguration.put(APPLITOOLS.BATCH_NAME, new BatchInfo(configs.get(LAUNCH_NAME) + "-" + configs.get(TARGET_ENVIRONMENT)));
            applitoolsConfiguration.put(APPLITOOLS.DEFAULT_MATCH_LEVEL, getMatchLevel());
            applitoolsConfiguration.put(APPLITOOLS.RECTANGLE_SIZE, getViewportSize());
            applitoolsConfiguration.put(APPLITOOLS.IS_BENCHMARKING_ENABLED, isBenchmarkingEnabled());
        }
        LOGGER.info("applitoolsConfiguration: " + applitoolsConfiguration);
        return applitoolsConfiguration;
    }

    private static boolean isBenchmarkingEnabled () {
        return Boolean.parseBoolean(String.valueOf(applitoolsConfiguration.get(APPLITOOLS.ENABLE_BENCHMARK_PER_VALIDATION)));
    }

    private static void getApplitoolsConfigFromProvidedConfigFile () {
        String applitoolsConfigurationFileName = configs.get(APPLITOOLS_CONFIGURATION);
        if (applitoolsConfigurationFileName.equals(NOT_SET)) {
            LOGGER.info("Applitools configuration not provided. Will use defaults%n");
        } else {
            LOGGER.info("Loading Applitools configuration from: "+  applitoolsConfigurationFileName);
            applitoolsConfiguration = JsonFile.loadJsonFile(applitoolsConfigurationFileName);
        }
    }

    private static MatchLevel getMatchLevel () {
        MatchLevel matchLevel;
        try {
            matchLevel = MatchLevel.valueOf(String.valueOf(applitoolsConfiguration.get(APPLITOOLS.DEFAULT_MATCH_LEVEL)));
        } catch (IllegalArgumentException | NullPointerException e) {
            matchLevel = MatchLevel.STRICT;
        }
        return matchLevel;
    }

    @NotNull
    private static RectangleSize getViewportSize () {
        RectangleSize viewportSize = new RectangleSize(1280, 960);
        try {
            String[] viewP = ((String) applitoolsConfiguration.get(APPLITOOLS.VIEWPORT_SIZE)).split("x");
            viewportSize = new RectangleSize(Integer.parseInt(viewP[0]), Integer.parseInt(viewP[1]));
        } catch (NullPointerException e) {
            LOGGER.info("Unable to get viewport size from Applitools configuration. Using default: 1280x960");
        }
        return viewportSize;
    }

    private void getBranchName () {
        String[] listOfDevices = new String[]{"git", "rev-parse", "--abbrev-ref", "HEAD"};
        CommandLineResponse response = CommandLineExecutor.execCommand(listOfDevices);
        String branchName = response.getStdOut();
        LOGGER.info("BRANCH_NAME: " + branchName);
        configs.put(BRANCH_NAME, branchName);
    }

    private void loadAndUpdateConfigParameters (String configFilePath) {
        configs.put(CONFIG_FILE, configFilePath);
        buildMapOfRequiredProperties();

        LOGGER.info("Updated string values from property file for missing properties: \n" + configs);
        LOGGER.info("Updated boolean values from property file for missing properties: \n" + configsBoolean);
        LOGGER.info("Updated integer values from property file for missing properties: \n" + configsInteger);
    }

    public void run (ArrayList<String> args, String stepDefsDir, String featuresDir) {
        args.add("--glue");
        args.add(stepDefsDir);
        args.add(featuresDir);
        LOGGER.info("Begin running tests...");
        LOGGER.info("Args: " + args);
        String[] array = args.stream().toArray(String[]::new);
        byte exitStatus = Main.run(array);
        LOGGER.info("Output of test run: " + exitStatus);
        if (exitStatus != 0) {
            throw new TestExecutionFailedException("Test execution failed. Exit status: " + exitStatus);
        }
    }

    private void buildMapOfRequiredProperties () {
        configs.put(APP_NAME, getOverriddenStringValue(APP_NAME, getStringValueFromPropertiesIfAvailable(APP_NAME, NOT_SET)));
        configs.put(APP_PACKAGE_NAME, getOverriddenStringValue(APP_PACKAGE_NAME, getStringValueFromPropertiesIfAvailable(APP_PACKAGE_NAME, NOT_SET)));
        configs.put(APP_PATH, getOverriddenStringValue(APP_PATH, getStringValueFromPropertiesIfAvailable(APP_PATH, NOT_SET)));
        configs.put(APPLITOOLS_CONFIGURATION, getStringValueFromPropertiesIfAvailable(APPLITOOLS_CONFIGURATION, NOT_SET));
        configs.put(BROWSER, getOverriddenStringValue(BROWSER, getStringValueFromPropertiesIfAvailable(BROWSER, CHROME)));
        configs.put(BASE_URL_FOR_WEB, getOverriddenStringValue(BASE_URL_FOR_WEB, getStringValueFromPropertiesIfAvailable(BASE_URL_FOR_WEB, NOT_SET)));
        configs.put(CAPS, getOverriddenStringValue(CAPS, getStringValueFromPropertiesIfAvailable(CAPS, NOT_SET)));
        configs.put(DEVICE_LAB_URL, getOverriddenStringValue(DEVICE_LAB_URL, getStringValueFromPropertiesIfAvailable(DEVICE_LAB_URL, NOT_SET)));
        configs.put(ENVIRONMENT_CONFIG_FILE, getOverriddenStringValue(ENVIRONMENT_CONFIG_FILE, getStringValueFromPropertiesIfAvailable(ENVIRONMENT_CONFIG_FILE, NOT_SET)));
        configsBoolean.put(IS_VISUAL, getOverriddenBooleanValue(IS_VISUAL, getBooleanValueFromPropertiesIfAvailable(IS_VISUAL, false)));
        configs.put(LOG_DIR, getOverriddenStringValue(LOG_DIR, getStringValueFromPropertiesIfAvailable(LOG_DIR, DEFAULT_LOG_DIR)));
        configs.put(LOG_PROPERTIES_FILE, getStringValueFromPropertiesIfAvailable(LOG_PROPERTIES_FILE, DEFAULT_LOG_PROPERTIES_FILE));
        platform = Platform.valueOf(getOverriddenStringValue(PLATFORM, getStringValueFromPropertiesIfAvailable(PLATFORM, Platform.android.name())));
        configsInteger.put(PARALLEL, getOverriddenIntValue(PARALLEL, Integer.parseInt(getStringValueFromPropertiesIfAvailable(PARALLEL, String.valueOf(DEFAULT_PARALLEL)))));
        configsBoolean.put(RUN_IN_CI, getOverriddenBooleanValue(RUN_IN_CI, getBooleanValueFromPropertiesIfAvailable(RUN_IN_CI, false)));
        configs.put(TAG, getOverriddenStringValue(TAG, getStringValueFromPropertiesIfAvailable(TAG, NOT_SET)));
        configs.put(TARGET_ENVIRONMENT, getOverriddenStringValue(TARGET_ENVIRONMENT, getStringValueFromPropertiesIfAvailable(TARGET_ENVIRONMENT, NOT_SET)));
        configs.put(TEST_DATA_FILE, getOverriddenStringValue(TEST_DATA_FILE, getStringValueFromPropertiesIfAvailable(TEST_DATA_FILE, NOT_SET)));
    }

    private String getStringValueFromPropertiesIfAvailable (String key, String defaultValue) {
        return properties.getProperty(key, String.valueOf(defaultValue));
    }

    private boolean getBooleanValueFromPropertiesIfAvailable (String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    @NotNull
    private Properties loadProperties (String configFile) {
        final Properties properties;
        try (InputStream input = new FileInputStream(configFile)) {
            properties = new Properties();
            properties.load(input);
        } catch (IOException ex) {
            throw new InvalidTestDataException("Config file not found, or unable to read it", ex);
        }
        return properties;
    }

    private void printLoadedConfigProperties (String configFilePath) {
        LOGGER.info("\nLoaded property file: " + configFilePath);
        properties.keySet().forEach(key -> {
            LOGGER.info("\t" + key + " :: " + properties.get(key));
        });
    }

    private void setupWebExecution () {
        if (platform.equals(Platform.web)) {
            configs.put(APP_PATH, configs.get(BROWSER));
            cukeArgs.add("--threads");
            cukeArgs.add(String.valueOf(configsInteger.get(PARALLEL)));
            cukeArgs.add(PLUGIN);
            cukeArgs.add("com.znsio.e2e.listener.CucumberWebScenarioListener");
            cukeArgs.add(PLUGIN);
            cukeArgs.add("com.znsio.e2e.listener.CucumberWebScenarioReporterListener");
            configs.put(EXECUTED_ON, "Local Browsers");
        }
    }

    private void setupAndroidExecution () {
        if (platform.equals(Platform.android)) {
            if (configsBoolean.get(RUN_IN_CI)) {
                setupCloudExecution();
            } else {
                setupLocalExecution();
            }
            cukeArgs.add("--threads");
            cukeArgs.add(String.valueOf(configsInteger.get(PARALLEL)));
            cukeArgs.add(PLUGIN);
            cukeArgs.add("com.cucumber.listener.CucumberScenarioListener");
            cukeArgs.add(PLUGIN);
            cukeArgs.add("com.cucumber.listener.CucumberScenarioReporterListener");
        }
    }

    private void updateAppPath () {
        String appPath = String.valueOf(configs.get(APP_PATH));
        LOGGER.info("Update path to Apk: " + appPath);
        if (appPath.equals(NOT_SET)) {
            appPath = getAppPathFromCapabilities();
            configs.put(APP_PATH, appPath);
            String capabilitiesFile = configs.get(CAPS);
            LOGGER.info("\tUsing AppPath: "+ appPath +" in file: "+ capabilitiesFile +":: " +  platform);
        } else {
            LOGGER.info("\tUsing AppPath provided as environment variable -  "+ appPath);
        }
    }

    private String getAppPathFromCapabilities () {
        String capabilityFile = configs.get(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile, new String[]{platform.name(), "app", "local"});
    }

    private void updateCapabilities (String emailID, String authenticationKey) {
        String capabilityFile = configs.get(CAPS);
        String appPath = configs.get(APP_PATH);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);

        String platformName = platform.name();
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        loadedPlatformCapability.remove("app");
        loadedPlatformCapability.put("pCloudy_Username", emailID);
        loadedPlatformCapability.put("pCloudy_ApiKey", authenticationKey);
        String[] splitAppPath = appPath.split("/");
        loadedPlatformCapability.put("pCloudy_ApplicationName", splitAppPath[splitAppPath.length - 1]);
        String osVersion = (String) loadedPlatformCapability.get("pCloudy_DeviceVersion");
        ArrayList listOfAndroidDevices = new ArrayList();
        for (int numDevices = 0; numDevices < configsInteger.get(PARALLEL); numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("osVersion", osVersion);
            listOfAndroidDevices.add(deviceInfo);
        }
        Map loadedCloudCapability = loadedCapabilityFile.get("cloud");
        loadedCloudCapability.put(platformName, listOfAndroidDevices);

        LOGGER.info("Updated Device Lab Capabilities file: \n" + loadedCapabilityFile);

        String updatedCapabilitesFile = getTempPathForFile(capabilityFile);
        JsonFile.saveJsonToFile(loadedCapabilityFile, updatedCapabilitesFile);
        configs.put(CAPS, updatedCapabilitesFile);
    }

    private String getTempPathForFile (String fullFilePath) {
        LOGGER.info("\tgetTempPathForFile: fullFilePath: " + fullFilePath);
        Path path = Paths.get(fullFilePath);
        String fileName = path.getFileName().toString();
        String tempFileName = configs.get(LOG_DIR) + "/" + fileName;
        LOGGER.info("\tTemp file available here: " + tempFileName);
        return tempFileName;
    }

    private void setupLocalExecution () {
        updateAppPath();
        setupLocalDevices();
        int parallelCount = devices.size();
        if (parallelCount == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        configsInteger.put(PARALLEL, parallelCount);
        configs.put(EXECUTED_ON, "Local Devices");
    }

    private List<Device> setupLocalDevices () {
        startADBServer();
        if (null == devices) {
            JadbConnection jadb = new JadbConnection();
            List<JadbDevice> deviceList = new ArrayList<>();
            devices = new ArrayList<>();
            try {
                deviceList = jadb.getDevices();
            } catch (IOException | JadbException e) {
                throw new EnvironmentSetupException("Unable to get devices information", e);
            }

            extractInfoFromEachDevice(deviceList);

            LOGGER.info("Number of Devices connected: " + devices.size());
        }
        return devices;
    }

    private void extractInfoFromEachDevice (List<JadbDevice> deviceList) {
        deviceList.forEach(jadbDevice -> {
            try {
                Device device = new Device();
                device.setName(jadbDevice.getSerial());
                device.setUdid(jadbDevice.getSerial());
//                    device.setUdid(getAdbCommandOutput(jadbDevice, "getprop", "ro.serialno"));
                device.setApiLevel(getAdbCommandOutput(jadbDevice, "getprop", "ro.build.version.sdk"));
                device.setDeviceManufacturer(getAdbCommandOutput(jadbDevice, "getprop", "ro.product.brand"));
                device.setDeviceModel(getAdbCommandOutput(jadbDevice, "getprop", "ro.product.model"));
                device.setOsVersion(getAdbCommandOutput(jadbDevice, "getprop", "ro.build.version.release"));
                devices.add(device);
                uninstallAppFromDevice(device, configs.get(APP_PACKAGE_NAME));
            } catch (IOException | JadbException e) {
                throw new EnvironmentSetupException("Unable to get devices information", e);
            }
        });
    }

    private void uninstallAppFromDevice (Device device, String appPackageName) {
        String[] uninstallAppiumAutomator2Server = new String[]{"adb", "-s", device.getUdid(), "uninstall", "io.appium.uiautomator2.server"};
        CommandLineExecutor.execCommand(uninstallAppiumAutomator2Server);
        String[] uninstallAppiumSettings = new String[]{"adb", "-s", device.getUdid(), "uninstall", "io.appium.settings"};
        CommandLineExecutor.execCommand(uninstallAppiumSettings);
        String[] uninstallApp = new String[]{"adb", "-s", device.getUdid(), "uninstall", appPackageName};
        CommandLineExecutor.execCommand(uninstallApp);
    }

    @NotNull
    private String getAdbCommandOutput (JadbDevice device, String command, String args) throws IOException, JadbException {
        InputStream inputStream = device.executeShell(command, args);
        LOGGER.info("\tadb command: "+  command +", args: "+ args +", ");
        String adbCommandOutput = Stream.readAll(inputStream, StandardCharsets.UTF_8).replaceAll("\n$", "");
        LOGGER.info("\tOutput: "+ adbCommandOutput);
        return adbCommandOutput;
    }

    private void startADBServer () {
        LOGGER.info("Start ADB server");
        String[] listOfDevices = new String[]{"adb", "devices"};
        CommandLineExecutor.execCommand(listOfDevices);
    }

    private void setupCloudExecution () {
        updateAppPath();
        String emailID = System.getenv("CLOUD_USER");
        String authenticationKey = System.getenv("CLOUD_KEY");
        uploadAPKTopCloudy(emailID, authenticationKey);
        updateCapabilities(emailID, authenticationKey);
        configs.put(EXECUTED_ON, "Cloud Devices");
    }

    private void uploadAPKTopCloudy (String emailID, String authenticationKey) {
        String appPath = configs.get(APP_PATH);
        String deviceLabURL = configs.get(DEVICE_LAB_URL);

        String authToken = getpCloudyAuthToken(emailID, authenticationKey, appPath, deviceLabURL);
        if (isAPKAlreadyAvailableInCloud(authToken, appPath)) {
            LOGGER.info("\tAPK is already available in cloud. No need to upload it again");
        } else {
            LOGGER.info("\tAPK is NOT available in cloud. Upload it");
            configs.put(APP_PATH, uploadAPKToPCloudy(appPath, deviceLabURL, authToken));
        }
    }

    private boolean isAPKAlreadyAvailableInCloud (String authToken, String appPath) {
        Path path = Paths.get(appPath);
        String appNameFromPath = path.getFileName().toString();
        LOGGER.info("isAPKAlreadyAvailableInCloud: Start: " + appPath);

        CommandLineResponse uploadResponse = getListOfUploadedFilesInDeviceFarm(authToken);
        JsonObject result = JsonFile.convertToMap(uploadResponse.getStdOut()).getAsJsonObject("result");
        JsonArray availableFiles = result.getAsJsonArray("files");
        AtomicBoolean isFileAlreadyUploaded = new AtomicBoolean(false);
        availableFiles.forEach(file -> {
            String fileName = ((JsonObject) file).get("file").getAsString();
            LOGGER.info("\tThis file is available in Device Farm: " + fileName);
            if (appNameFromPath.equals(fileName)) {
                isFileAlreadyUploaded.set(true);
            }
        });

        return isFileAlreadyUploaded.get();
    }

    @NotNull
    private CommandLineResponse getListOfUploadedFilesInDeviceFarm (String authToken) {
        String deviceLabURL = configs.get(DEVICE_LAB_URL);
        Map payload = new HashMap();
        payload.put("token", authToken);
        payload.put("limit", 15);
        payload.put("filter", "all");
        String[] listOfDevices = new String[]{
                "curl --insecure",
                "-H",
                "Content-Type:application/json",
                "-d",
                "'" + JsonParser.parseString(payload.toString()) + "'",
                deviceLabURL + "/api/drive"};

        CommandLineResponse listFilesInPCloudyResponse = CommandLineExecutor.execCommand(listOfDevices);
        LOGGER.info("\tlistFilesInPCloudyResponse: " + listFilesInPCloudyResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(listFilesInPCloudyResponse.getStdOut()).getAsJsonObject("result");
        JsonElement resultCode = result.get("code");
        int uploadStatus = (null == resultCode) ? 400 : resultCode.getAsInt();
        if (200 != uploadStatus) {
            throw new EnvironmentSetupException(String.format("Unable to get list of uploaded files%n%s",
                    listFilesInPCloudyResponse));
        }

        return listFilesInPCloudyResponse;
    }

    private String uploadAPKToPCloudy (String appPath, String deviceLabURL, String authToken) {
        LOGGER.info("uploadAPKTopCloudy: " + appPath);
        String[] listOfDevices = new String[]{
                "curl --insecure",
                "-X",
                "POST",
                "-F",
                "file=@\"" + appPath + "\"",
                "-F",
                "\"source_type=raw\"",
                "-F",
                "\"token=" + authToken + "\"",
                "-F",
                "\"filter=apk\"",
                deviceLabURL + "/api/upload_file"};

        CommandLineResponse uploadApkResponse = CommandLineExecutor.execCommand(listOfDevices);
        LOGGER.info("\tuploadApkResponse: " + uploadApkResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(uploadApkResponse.getStdOut()).getAsJsonObject("result");
        int uploadStatus = result.get("code").getAsInt();
        if (200 != uploadStatus) {
            throw new EnvironmentSetupException(String.format("Unable to upload app: '%s' to '%s'%n%s",
                    appPath, deviceLabURL, uploadApkResponse));
        }
        String uploadedFileName = result.get("file").getAsString();
        LOGGER.info("\tuploadAPKToPCloudy: Uploaded: " + uploadedFileName);
        return uploadedFileName;
    }

    private String getpCloudyAuthToken (String emailID, String authenticationKey, String appPath, String deviceLabURL) {
        LOGGER.info("Get pCloudy Auth Token");
        String[] getAppToken = new String[]{
                "curl --insecure",
                "-u",
                "\"" + emailID + ":" + authenticationKey + "\"",
                deviceLabURL + "/api/access"
        };
        CommandLineResponse authTokenResponse = CommandLineExecutor.execCommand(getAppToken);
        LOGGER.info("\tauthTokenResponse: " + authTokenResponse.getStdOut());
        if (authTokenResponse.getStdOut().contains("error")) {
            throw new EnvironmentSetupException(String.format("Unable to get auth: '%s' to '%s'%n%s", appPath, deviceLabURL, authTokenResponse));
        }
        String authToken = JsonFile.convertToMap(authTokenResponse.getStdOut()).getAsJsonObject("result").get("token").getAsString();
        LOGGER.info("\tauthToken: " + authToken);
        return authToken;
    }

    private void getPlatformTagsAndLaunchName () {
        LOGGER.info("Get Platform, Tags and LaunchName");
        String launchName = configs.get(APP_NAME) + " Tests";
        if (configsBoolean.get(RUN_IN_CI)) {
            launchName += " on Device Farm";
        }
        String inferredTags = getCustomTags();
        String providedTags = configs.get(TAG);
        if (providedTags.isEmpty() || providedTags.equals(NOT_SET)) {
            LOGGER.info("\tTags not specified");
            launchName += " - " + platform;
        } else {
            if (providedTags.contains("multiuser-android-web")) {
                platform = Platform.android;
                inferredTags = providedTags + " and not @wip";
                launchName += " - Real User Simulation on Android & Web";
            } else if (providedTags.contains("multiuser-web-web")) {
                platform = Platform.web;
                inferredTags = providedTags + " and not @wip";
                launchName += " - Real User Simulation on Web";
            } else {
                launchName += " - " + platform;
            }
        }
        LOGGER.info("\tRunning tests with platform: "+ platform +" and the following tag criteria : "+  inferredTags);
        LOGGER.info("\tReportPortal Tests Launch name: " + launchName);

        configs.put(PLATFORM, platform.name());
        configs.put(LAUNCH_NAME, launchName);
        configs.put(TAG, inferredTags);
        cukeArgs.add("--tags");
        cukeArgs.add(inferredTags);
    }

    private void addCucumberPlugsToArgs () {
        cukeArgs.add(PLUGIN);
        cukeArgs.add("pretty");
        cukeArgs.add(PLUGIN);
        String logDir = configs.get(LOG_DIR);
        cukeArgs.add("html:" + logDir + "/reports/cucumber-html-report.html");
        cukeArgs.add(PLUGIN);
        cukeArgs.add("junit:" + logDir + "/reports/cucumber-junit-report.xml");
        cukeArgs.add(PLUGIN);
        cukeArgs.add("json:" + logDir + "/reports/cucumber-json-report.json");
        cukeArgs.add(PLUGIN);
        cukeArgs.add("message:" + logDir + "/reports/results.ndjson");
        cukeArgs.add(PLUGIN);
        cukeArgs.add("timeline:" + logDir + "/reports/timeline");
    }

    private String getCustomTags () {
        String customTags = "@" + platform + " and not @wip";
        String providedTags = configs.get(TAG);
        if (!providedTags.equalsIgnoreCase(NOT_SET)) {
            if (!providedTags.startsWith("@")) {
                providedTags = "@" + providedTags;
            }
            customTags = providedTags + " and " + customTags;
        }
        LOGGER.info("\tComputed tags: " + customTags);
        return customTags;
    }

    private void printArguments (String[] args) {
        LOGGER.info("Passed args: " + args.length);
        for (int i = 0; i < args.length; i++) {
            LOGGER.info("\targ: " + (i + 1) + " :: " + args[i]);
        }
    }

    private void printSystemProperties () {
        LOGGER.info("system properties");
        System.getProperties().forEach((key, value) -> {
            LOGGER.info("\t" + key + "\t:: " + value);
        });
    }

    private void printEnvironmentVariables () {
        LOGGER.info("environment variables");
        System.getenv().forEach((key, value) -> {
            LOGGER.info("\t" + key + "\t:: " + value);
        });
    }

    private void cleanupDirectories () {
        List<String> files = listOfDirectoriesToDelete();
        LOGGER.info("Delete Directories: " + files);
        for (String file : files) {
            LOGGER.info("\tDeleting directory: " + file);
            try {
                FileUtils.deleteDirectory(new java.io.File(file));
            } catch (IOException e) {
                throw new EnvironmentSetupException("Unable to cleanup & setup directories", e);
            }
        }
    }

    private void setupDirectories () {
        List<String> files = listOfDirectoriesToCreate();
        LOGGER.info("Create Directories: " + files);
        for (String file : files) {
            LOGGER.info("\tCreating directory: " + file);
            try {
                FileUtils.forceMkdir(new java.io.File(file));
            } catch (IOException e) {
                throw new EnvironmentSetupException("Unable to cleanup & setup directories", e);
            }
        }
    }

    private List<String> listOfDirectoriesToCreate () {
        List<String> files = new ArrayList<>();
        files.add(tempDirectory);
        files.add(configs.get(LOG_DIR));
        return files;
    }

    private List<String> listOfDirectoriesToDelete () {
        List<String> files = new ArrayList<>();
        files.add(configs.get(LOG_DIR));
        return files;
    }
}
