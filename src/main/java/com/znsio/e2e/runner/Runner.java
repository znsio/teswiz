package com.znsio.e2e.runner;

import com.applitools.eyes.BatchInfo;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.github.device.Device;
import com.ssts.pcloudy.Connector;
import com.ssts.pcloudy.dto.file.PDriveFileDTO;
import com.ssts.pcloudy.exception.ConnectError;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.JsonFile;
import com.znsio.e2e.tools.Visual;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import io.cucumber.core.cli.Main;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.appium.utils.Variable.*;

public class Runner {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().startsWith("windows");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().startsWith("mac");
    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String USER_NAME = System.getProperty("user.name");
    public static final String BASE_URL_FOR_WEB = "BASE_URL_FOR_WEB";
    private static final String APP_NAME = "APP_NAME";
    private static final String IS_VISUAL = "IS_VISUAL";
    private static final String CHROME = "chrome";
    private static final String PLUGIN = "--plugin";
    private static final String tempDirectory = "temp";
    private static final String NOT_SET = "not-set";
    private static final Platform DEFAULT_PLATFORM = Platform.android;
    private static final int DEFAULT_PARALLEL = 1;
    private static final ArrayList<String> cukeArgs = new ArrayList<>();
    private static final String BRANCH_NAME = NOT_SET;
    private static final String LOG_PROPERTIES_FILE = NOT_SET;
    private static final String DEFAULT_LOG_DIR = "target";
    private static final String APP_PATH = "APP_PATH";
    private static final String BROWSER = "BROWSER";
    private static final String CAPS = "CAPS";
    private static final String CONFIG_FILE = "CONFIG_FILE";
    private static final String DEVICE_LAB_URL = "DEVICE_LAB_URL";
    private static final String ENVIRONMENT_CONFIG_FILE = "ENVIRONMENT_CONFIG_FILE";
    private static final String EXECUTED_ON = NOT_SET;
    private static final String INFERRED_TAGS = "INFERRED_TAGS";
    private static final String LAUNCH_NAME = "LAUNCH_NAME";
    private static final String LOG_DIR = "LOG_DIR";
    private static final String PARALLEL = "PARALLEL";
    private static final String PLATFORM = "Platform";
    private static final String RUN_ON_CLOUD = "RUN_ON_CLOUD";
    private static final String TAG = "TAG";
    private static final String TARGET_ENVIRONMENT = "TARGET_ENVIRONMENT";
    private static final String TEST_DATA_FILE = "TEST_DATA_FILE";
    private static final Map<String, String> configs = new HashMap();
    private static final Map<String, Boolean> configsBoolean = new HashMap();
    private static final Map<String, Integer> configsInteger = new HashMap();
    public static Platform platform = Platform.android;
    public static BatchInfo batchName;
    private static Map<String, Map> environmentConfiguration;
    private static Map<String, Map> testDataForEnvironment;
    private final Properties properties;

    public Runner () {
        throw new InvalidTestDataException("Required args not provided to Runner");
    }

    public Runner (String configFilePath, String stepDefDirName, String featuresDirName, String logPropertiesFile) {
        System.out.printf("Runner called from user directory: '%s'%n", Runner.USER_DIRECTORY);
        Path path = Paths.get(configFilePath);
        if (!Files.exists(path)) {
            throw new InvalidTestDataException(String.format("Invalid path ('%s') provided for config", configFilePath));
        }
        properties = loadProperties(configFilePath);
        printLoadedConfigProperties(configFilePath);

        loadAndUpdateConfigParameters(configFilePath, logPropertiesFile);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(configs.get(CAPS));
        System.out.println("loadedCapabilityFile: " + loadedCapabilityFile);

        environmentConfiguration = loadEnvironmentConfiguration(configs.get(TARGET_ENVIRONMENT));
        testDataForEnvironment = loadTestDataForEnvironment(configs.get(TARGET_ENVIRONMENT));

        cleanupDirectories();
        setupDirectories();

        getPlatformTagsAndLaunchName();
        addCucumberPlugsToArgs();
        setupAndroidExecution();
        setupWebExecution();

        setBranchName();

        System.setProperty("CONFIG_FILE", configs.get(CONFIG_FILE));
        System.setProperty("CAPS", configs.get(CAPS));
        System.setProperty("Platform", platform.name());
        System.setProperty("atd_" + platform.name() + "_app_local", configs.get(APP_PATH));
        System.setProperty("rp.description", configs.get(APP_NAME) + " End-2-End scenarios on " + platform.name());
        System.setProperty("rp.launch", configs.get(LAUNCH_NAME));

        String rpAttributes = "Username:" + USER_NAME + "; " +
                "Platform:" + platform.name() + "; " +
                "Installer:" + configs.get(APP_PATH) + "; " +
                "TargetEnvironment:" + configs.get(TARGET_ENVIRONMENT) + "; " +
                "ExecutedOn:" + configs.get(EXECUTED_ON) + "; " +
                "VisualEnabled:" + configs.get(IS_VISUAL) + "; " +
                "AutomationBranch:" + configs.get(BRANCH_NAME) + "; " +
                "OS:" + OS_NAME + "; " +
                "ParallelCount:" + configsInteger.get(PARALLEL) + "; " +
                "Tags:" + configs.get(TAG) + "; ";

        System.setProperty("rp.attributes", rpAttributes);

        batchName = new BatchInfo(configs.get(LAUNCH_NAME) + "-" + configs.get(TARGET_ENVIRONMENT));

        run(cukeArgs, stepDefDirName, featuresDirName);
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
        System.out.println("unified-e2e Runner");
        System.out.println("Provided parameters:");
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
        if (args.length != 4) {
            throw new InvalidTestDataException("Expected following parameters: 'String configFilePath, String stepDefDirName, String featuresDirName, String logDirName");
        }
        new Runner(args[0], args[1], args[2], args[3]);
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
        System.out.printf("Loading environment configuration from ENVIRONMENT_CONFIG_FILE: '%s' for environment: '%s'%n", envConfigFile, environment);
        System.out.println("ENVIRONMENT_CONFIG_FILE: " + envConfigFile);
        return (NOT_SET.equalsIgnoreCase(envConfigFile)) ? new HashMap<>() : JsonFile.getNodeValueAsMapFromJsonFile(environment, envConfigFile);
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
        System.out.printf("Loading test data from TEST_DATA_FILE: '%s' for environment: '%s'%n", testDataFile, environment);
        return (NOT_SET.equalsIgnoreCase(testDataFile)) ? new HashMap<>() : JsonFile.getNodeValueAsMapFromJsonFile(environment, testDataFile);
    }

    public static String getTargetEnvironment () {
        return configs.get(TARGET_ENVIRONMENT);
    }

    public static String getAppName () {
        return configs.get(APP_NAME);
    }

    private void setBranchName () {
        String[] listOfDevices = new String[]{"git", "rev-parse", "--abbrev-ref", "HEAD"};
        CommandLineResponse response = CommandLineExecutor.execCommand(listOfDevices);
        String branchName = response.getStdOut();
        System.out.println("BRANCH_NAME: " + branchName);
        configs.put(BRANCH_NAME, branchName);
    }

    private void loadAndUpdateConfigParameters (String configFilePath, String logPropertiesFile) {
        configs.put(CONFIG_FILE, configFilePath);
        configs.put(LOG_PROPERTIES_FILE, logPropertiesFile);
        buildMapOfRequiredProperties();

        configs.forEach((k, v) -> {
            if (NOT_SET.equalsIgnoreCase(v) && properties.containsKey(k.toUpperCase())) {
                configs.put(k, properties.getProperty(k.toUpperCase()));
            }
        });

        configsBoolean.forEach((k, v) -> {
            if (properties.containsKey(k.toUpperCase())) {
                configsBoolean.put(k, Boolean.valueOf(properties.getProperty(k.toUpperCase())));
            }
        });

        configsInteger.forEach((k, v) -> {
            if (properties.containsKey(k.toUpperCase())) {
                configsInteger.put(k, Integer.valueOf(properties.getProperty(k.toUpperCase())));
            }
        });

        System.out.println("Updated string values from property file for missing properties: \n" + configs);
        System.out.println("Updated boolean values from property file for missing properties: \n" + configsBoolean);
        System.out.println("Updated integer values from property file for missing properties: \n" + configsInteger);
    }

    public void run (ArrayList<String> args, String stepDefsDir, String featuresDir) {
        args.add("--glue");
        args.add(stepDefsDir);
        args.add(featuresDir);
        System.out.println("Begin running tests...");
        System.out.println("Args: " + args);
        String[] array = args.stream().toArray(String[]::new);
        byte exitStatus = Main.run(array);
        System.out.println("Output of test run: " + exitStatus);
    }

    private void buildMapOfRequiredProperties () {
        configs.put(APP_NAME, getOverriddenStringValue(APP_NAME, NOT_SET));
        configs.put(APP_PATH, NOT_SET);
        configs.put(BROWSER, getOverriddenStringValue(BROWSER, CHROME));
        configs.put(BASE_URL_FOR_WEB, getOverriddenStringValue(BASE_URL_FOR_WEB, NOT_SET));
        configs.put(CAPS, getOverriddenStringValue(CAPS, NOT_SET));
        configs.put(DEVICE_LAB_URL, getOverriddenStringValue(DEVICE_LAB_URL, NOT_SET));
        configs.put(ENVIRONMENT_CONFIG_FILE, getOverriddenStringValue(ENVIRONMENT_CONFIG_FILE, NOT_SET));
        configsBoolean.put(IS_VISUAL, getOverriddenBooleanValue(IS_VISUAL, false));
        configs.put(LOG_DIR, getOverriddenStringValue(LOG_DIR, DEFAULT_LOG_DIR));
        platform = Platform.valueOf(getOverriddenStringValue(PLATFORM, Platform.android.name()));
        configsInteger.put(PARALLEL, getOverriddenIntValue(PARALLEL, DEFAULT_PARALLEL));
        configsBoolean.put(RUN_ON_CLOUD, getOverriddenBooleanValue(RUN_ON_CLOUD, false));
        configs.put(TAG, getOverriddenStringValue(TAG, NOT_SET));
        configs.put(TARGET_ENVIRONMENT, getOverriddenStringValue(TARGET_ENVIRONMENT, NOT_SET));
        configs.put(TEST_DATA_FILE, getOverriddenStringValue(TEST_DATA_FILE, NOT_SET));
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
        System.out.println("Loaded property file: " + configFilePath);
        properties.keySet().forEach(key -> {
            System.out.println(key + " :: " + properties.get(key));
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
            if (configsBoolean.get(RUN_ON_CLOUD)) {
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
        System.out.println("getAppPath: " + appPath);
        if (appPath.equals(NOT_SET)) {
            appPath = getAppPathFromCapabilities();
            String capabilitiesFile = configs.get(CAPS);
            System.out.println("Update App Path as obtained from capabilities file: " + capabilitiesFile);
            configs.put(APP_PATH, appPath);
            System.out.printf("Using AppPath: '%s' in file: '%s'::'%s'%n", appPath, capabilitiesFile, platform);
        } else {
            System.out.printf("Using AppPath provided as environment variable - '%s'%n", appPath);
        }
    }

    private String getAppPathFromCapabilities () {
        String capabilityFile = configs.get(CAPS);
        System.out.println("getAppPathFromCapabilities: from file: " + capabilityFile);
        String appPathFromCapabilities = JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile, new String[]{platform.name(), "app", "local"});
        return appPathFromCapabilities;
    }

    private void updateCapabilities (String emailID, String authenticationKey) {
        String capabilityFile = configs.get(CAPS);
        String appPath = configs.get(APP_PATH);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        System.out.println("loadedCapabilityFile: " + loadedCapabilityFile);

        String platformName = platform.name();
        Map platformMap = loadedCapabilityFile.get(platformName);
        System.out.println("platformMap: " + platformMap);
        platformMap.remove("app");
        platformMap.put("pCloudy_Username", emailID);
        platformMap.put("pCloudy_ApiKey", authenticationKey);
        String[] splitAppPath = appPath.split("/");
        platformMap.put("pCloudy_ApplicationName", splitAppPath[splitAppPath.length - 1]);
        String osVersion = (String) platformMap.get("pCloudy_DeviceVersion");
        ArrayList listOfAndroidDevices = new ArrayList();
        for (int numDevices = 0; numDevices < configsInteger.get(PARALLEL); numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("osVersion", osVersion);
            listOfAndroidDevices.add(deviceInfo);
        }
        Map cloudMap = loadedCapabilityFile.get("cloud");
        cloudMap.remove(platformName);
        cloudMap.put(platformName, listOfAndroidDevices);

        System.out.println("Updated Mobilab Capabilities file: \n" + loadedCapabilityFile);

        JsonFile.saveJsonToFile(loadedCapabilityFile, getTempPathForFile(capabilityFile));
    }

    private String getTempPathForFile (String fullFilePath) {
        System.out.println("getTempPathForFile: fullFilePath: " + fullFilePath);
        Path path = Paths.get(fullFilePath);

        // call getFileName() and get FileName path object
        String fileName = path.getFileName().toString();
        System.out.println(fileName);

        return "./temp/" + fileName;
    }

    private void setupLocalExecution () {
        updateAppPath();
        List<Device> devices = getDevices();
        int parallelCount = devices.size();
        if (parallelCount == 0) {
            throw new EnvironmentSetupException("No devices available to run the tests");
        }
        configsInteger.put(PARALLEL, parallelCount);
        configs.put(EXECUTED_ON, "Local Devices");
    }

    private List<Device> getDevices () {
        startADBServer();
        JadbConnection jadb = new JadbConnection();
        List<JadbDevice> deviceList = new ArrayList<>();
        List<Device> connectedDevices = new ArrayList<>();
        try {
            deviceList = jadb.getDevices();
        } catch (IOException | JadbException e) {
            throw new EnvironmentSetupException("Unable to get devices information", e);
        }

        deviceList.forEach(jadbDevice -> {
            try {
                Device device = new Device();
                device.setName(jadbDevice.getSerial());
                device.setUdid(getAdbCommandOutput(jadbDevice, "getprop", "ro.serialno"));
                device.setApiLevel(getAdbCommandOutput(jadbDevice, "getprop", "ro.build.version.sdk"));
                device.setDeviceManufacturer(getAdbCommandOutput(jadbDevice, "getprop", "ro.product.brand"));
                device.setDeviceModel(getAdbCommandOutput(jadbDevice, "getprop", "ro.product.model"));
                device.setOsVersion(getAdbCommandOutput(jadbDevice, "getprop", "ro.build.version.release"));
                connectedDevices.add(device);
            } catch (IOException | JadbException e) {
                throw new EnvironmentSetupException("Unable to get devices information", e);
            }
        });

        System.out.println("Devices connected: " + connectedDevices);
        System.out.println("Number of Devices connected: " + connectedDevices.size());
        return connectedDevices;
    }

    @NotNull
    private String getAdbCommandOutput (JadbDevice device, String command, String args) throws IOException, JadbException {
        InputStream inputStream = device.executeShell(command, args);
        System.out.printf("adb command: '%s', args: '%s', ", command, args);
        String adbCommandOutput = Stream.readAll(inputStream, StandardCharsets.UTF_8).replaceAll("\n$", "");
        System.out.printf("Output: '%s'%n", adbCommandOutput);
        return adbCommandOutput;
    }

    private void startADBServer () {
        System.out.println("------------------------------------------------");
        System.out.println("Start ADB server");
        String[] listOfDevices = new String[]{"adb", "devices"};
        CommandLineExecutor.execCommand(listOfDevices);
        System.out.println("------------------------------------------------");
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
        String labUrl = configs.get(DEVICE_LAB_URL);
        String apkPath = configs.get(APP_PATH);
        System.out.println("uploadAPKTopCloudy: Url: " + labUrl + ", apkFile: " + apkPath + ", user: " + emailID);
        Connector connector = new Connector(labUrl);
        String authToken = null;
        try {
            authToken = connector.authenticateUser(emailID, authenticationKey);
            System.out.println("AuthToken: (after establishing connection)" + authToken);

            File fileToBeUploaded = new File(apkPath);
            PDriveFileDTO alreadyUploadedApp = null;
            alreadyUploadedApp = connector.getAvailableAppIfUploaded(authToken, fileToBeUploaded.getName());
            if (alreadyUploadedApp == null) {
                System.out.println("Uploading Apk: " + fileToBeUploaded.getAbsolutePath());
                PDriveFileDTO uploadedApp = connector.uploadApp(authToken, fileToBeUploaded, true);
                System.out.println("Apk uploaded");
                alreadyUploadedApp = new PDriveFileDTO();
                alreadyUploadedApp.file = uploadedApp.file;
                System.out.println("Verified Apk uploaded");
            } else {
                System.out.println("Apk already present. Not uploading... ");
            }
        } catch (IOException | ConnectError e) {
            throw new RuntimeException("Unable to upload APK to pCloudy", e);
        }
    }

    private void getPlatformTagsAndLaunchName () {
        System.out.println("getPlatformTagsAndLaunchName");
        String launchName = configs.get(APP_NAME) + " Tests";
        String inferredTags = getCustomTags();
        String providedTags = configs.get(TAG);
        if (providedTags.isEmpty()) {
            System.out.println("Tags not specified");
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
        System.out.printf("Running tests with platform: '%s' and the following tag criteria : '%s'%n", platform, inferredTags);
        System.out.printf("ReportPortal Tests Launch name: '%s'%n", launchName);

        configs.put(PLATFORM, platform.name());
        configs.put(LAUNCH_NAME, launchName);
        configs.put(INFERRED_TAGS, inferredTags);
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
        System.out.printf("Computed tags: '%s'%n", customTags);
        return customTags;
    }

    private void printArguments (String[] args) {
        System.out.println("Passed args: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("\targ: " + (i + 1) + " :: " + args[i]);
        }
    }

    private void printSystemProperties () {
        System.out.println("system properties");
        System.getProperties().forEach((key, value) -> {
            System.out.println("\t" + key + "\t:: " + value);
        });
    }

    private void printEnvironmentVariables () {
        System.out.println("environment variables");
        System.getenv().forEach((key, value) -> {
            System.out.println("\t" + key + "\t:: " + value);
        });
    }

    private void cleanupDirectories () {
        List<String> files = listOfDirectoriesToDelete();
        System.out.println("cleanupDirectories: " + files);
        for (String file : files) {
            System.out.println("Deleting directory: " + file);
            try {
                FileUtils.deleteDirectory(new java.io.File(file));
            } catch (IOException e) {
                throw new EnvironmentSetupException("Unable to cleanup & setup directories", e);
            }
        }
    }

    private void setupDirectories () {
        List<String> files = listOfDirectoriesToCreate();
        System.out.println("setupDirectories: " + files);
        for (String file : files) {
            System.out.println("Creating directory: " + file);
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
