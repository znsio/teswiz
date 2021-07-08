package com.znsio.e2e.runner;

import com.applitools.eyes.BatchInfo;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.APPLITOOLS;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.exceptions.TestExecutionFailedException;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.Visual;
import io.cucumber.core.cli.Main;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.appium.utils.OverriddenVariable.getOverriddenStringValue;
import static com.znsio.e2e.runner.Setup.APP_NAME;
import static com.znsio.e2e.runner.Setup.APP_PACKAGE_NAME;
import static com.znsio.e2e.runner.Setup.BASE_URL_FOR_WEB;
import static com.znsio.e2e.runner.Setup.BROWSER;
import static com.znsio.e2e.runner.Setup.BROWSER_HEADLESS;
import static com.znsio.e2e.runner.Setup.BROWSER_MAXIMIZE;
import static com.znsio.e2e.runner.Setup.BROWSER_VERBOSE_LOGGING;
import static com.znsio.e2e.runner.Setup.IS_VISUAL;
import static com.znsio.e2e.runner.Setup.LAUNCH_NAME;
import static com.znsio.e2e.runner.Setup.MAX_NUMBER_OF_APPIUM_DRIVERS;
import static com.znsio.e2e.runner.Setup.MAX_NUMBER_OF_WEB_DRIVERS;
import static com.znsio.e2e.runner.Setup.PROXY_URL;
import static com.znsio.e2e.runner.Setup.RUN_IN_CI;
import static com.znsio.e2e.runner.Setup.TARGET_ENVIRONMENT;
import static com.znsio.e2e.runner.Setup.applitoolsConfiguration;
import static com.znsio.e2e.runner.Setup.environmentConfiguration;
import static com.znsio.e2e.runner.Setup.getApplitoolsConfigFromProvidedConfigFile;
import static com.znsio.e2e.runner.Setup.getMatchLevel;
import static com.znsio.e2e.runner.Setup.getViewportSize;
import static com.znsio.e2e.runner.Setup.isBenchmarkingEnabled;
import static com.znsio.e2e.runner.Setup.testDataForEnvironment;

public class Runner {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().startsWith("windows");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().startsWith("mac");
    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String USER_NAME = System.getProperty("user.name");
    public static final String NOT_SET = "not-set";
    static final Map<String, String> configs = new HashMap();
    static final Map<String, Boolean> configsBoolean = new HashMap();
    static final Map<String, Integer> configsInteger = new HashMap();
    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());
    public static Platform platform = Platform.android;
    private static ArrayList<String> cukeArgs = new ArrayList<>();

    public Runner () {
        throw new InvalidTestDataException("Required args not provided to Runner");
    }

    public Runner (String configFilePath, String stepDefDirName, String featuresDirName) {
        Path path = Paths.get(configFilePath);
        if (!Files.exists(path)) {
            throw new InvalidTestDataException(String.format("Invalid path ('%s') provided for config", configFilePath));
        }
        cukeArgs = new Setup(configFilePath).getExecutionArguments();
        run(cukeArgs, stepDefDirName, featuresDirName);
    }

    public static int getMaxNumberOfAppiumDrivers () {
        return configsInteger.get(MAX_NUMBER_OF_APPIUM_DRIVERS);
    }

    public static int getMaxNumberOfWebDrivers () {
        return configsInteger.get(MAX_NUMBER_OF_WEB_DRIVERS);
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

    public static Map getTestDataAsMap (String key) {
        try {
            return testDataForEnvironment.get(key);
        } catch (NullPointerException npe) {
            throw new InvalidTestDataException(String.format("Invalid key name ('%s') provided", key), npe);
        }
    }

    public static void main (String[] args) {
        LOGGER.info("teswiz Runner");
        LOGGER.info("Provided parameters:");
        for (int i = 0; i < args.length; i++) {
            LOGGER.info("\t" + args[i]);
        }
        if (args.length != 3) {
            throw new InvalidTestDataException("Expected following parameters: 'String configFilePath, String stepDefDirName, String featuresDirName");
        }
        new Runner(args[0], args[1], args[2]);
    }

    public static Driver fetchDriver (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona);
    }

    public static String fetchDeviceName (long threadId, String forUserPersona) {
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDeviceNameForUser(forUserPersona);
    }

    public static TestExecutionContext getTestExecutionContext (long threadId) {
        return SessionContext.getTestExecutionContext(threadId);
    }

    public static Visual fetchEyes (long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(TEST_CONTEXT.CURRENT_USER_PERSONA);
        Drivers allDrivers = (Drivers) getTestExecutionContext(threadId).getTestState(TEST_CONTEXT.ALL_DRIVERS);
        return allDrivers.getDriverForUser(userPersona).getVisual();
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
            applitoolsConfiguration.put(APPLITOOLS.APP_NAME, configs.get(APP_NAME));
            applitoolsConfiguration.put(APPLITOOLS.API_KEY, getOverriddenStringValue("APPLITOOLS_API_KEY", String.valueOf(applitoolsConfiguration.get(APPLITOOLS.API_KEY))));
            applitoolsConfiguration.put(APPLITOOLS.BATCH_NAME, new BatchInfo(configs.get(LAUNCH_NAME) + "-" + configs.get(TARGET_ENVIRONMENT)));
            applitoolsConfiguration.put(APPLITOOLS.DEFAULT_MATCH_LEVEL, getMatchLevel());
            applitoolsConfiguration.put(APPLITOOLS.RECTANGLE_SIZE, getViewportSize());
            applitoolsConfiguration.put(APPLITOOLS.IS_BENCHMARKING_ENABLED, isBenchmarkingEnabled());
        }
        LOGGER.info("applitoolsConfiguration: " + applitoolsConfiguration);
        return applitoolsConfiguration;
    }

    public static String getBrowser () {
        return configs.get(BROWSER);
    }

    public static boolean isRunInHeadlessMode () {
        return configsBoolean.get(BROWSER_HEADLESS);
    }

    public static boolean enableVerboseLoggingInBrowser () {
        return configsBoolean.get(BROWSER_VERBOSE_LOGGING);
    }

    public static String getProxyURL () {
        String proxyURL = configs.get(PROXY_URL);
        LOGGER.info("proxyURL: " + proxyURL);
        return proxyURL;
    }

    public static boolean shouldMaximizeBrowser () {
        return configsBoolean.get(BROWSER_MAXIMIZE);
    }
}
