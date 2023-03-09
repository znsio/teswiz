package com.znsio.teswiz.runner;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import io.cucumber.core.cli.Main;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Runner {
    public static final String OS_NAME = System.getProperty("os.name");
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().startsWith("windows");
    public static final boolean IS_MAC = OS_NAME.toLowerCase().startsWith("mac");
    public static final String USER_DIRECTORY = System.getProperty("user.dir");
    public static final String USER_NAME = System.getProperty("user.name");
    public static final String NOT_SET = "not-set";
    public static final String DEFAULT = "default";
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";

    private static final Logger LOGGER = Logger.getLogger(Runner.class.getName());
    private static final String INVALID_KEY_MESSAGE = "Invalid key name ('%s') provided";

    public Runner() {
        throw new InvalidTestDataException("Required args not provided to Runner");
    }

    public Runner(String configFilePath, String stepDefDirName, String featuresDirName) {
        Path path = Paths.get(configFilePath);
        if(!Files.exists(path)) {
            throw new InvalidTestDataException(
                    String.format("Invalid path ('%s') provided for config", configFilePath));
        }
        Setup.load(configFilePath);
        List<String> cukeArgs = Setup.getExecutionArguments();
        run(cukeArgs, stepDefDirName, featuresDirName);
    }

    public static Platform getPlatformForUser(String userPersona) {
        return Drivers.getPlatformForUser(userPersona);
    }

    public void run(List<String> args, String stepDefsDir, String featuresDir) {
        args.add("--glue");
        args.add(stepDefsDir);
        args.add(featuresDir);
        LOGGER.info("Begin running tests...");
        LOGGER.info(String.format("Args: %s", args));
        String[] array = args.toArray(String[]::new);
        byte status = Main.run(array);
        Setup.cleanUpExecutionEnvironment();
        CustomReports.generateReport();
        System.exit(status);
    }

    public static Platform getPlatform() {
        return Setup.getPlatform();
    }

    public static Map getApplitoolsConfiguration() {
        return Setup.initialiseApplitoolsConfiguration();
    }

    public static String getCloudName() {
        return Setup.getFromConfigs(Setup.CLOUD_NAME);
    }

    public static String getCloudUser() {
        return Setup.getFromConfigs(Setup.CLOUD_USER);
    }

    public static String getCloudKey() {
        return Setup.getFromConfigs(Setup.CLOUD_KEY);
    }

    public static String getRemoteDriverGridPort() {
        return Setup.getFromConfigs(Setup.REMOTE_WEBDRIVER_GRID_PORT);
    }

    public static int getMaxNumberOfAppiumDrivers() {
        return Setup.getIntegerValueFromConfigs(Setup.MAX_NUMBER_OF_APPIUM_DRIVERS);
    }

    public static int getMaxNumberOfWebDrivers() {
        return Setup.getIntegerValueFromConfigs(Setup.MAX_NUMBER_OF_WEB_DRIVERS);
    }

    public static boolean isVisualTestingEnabled() {
        return Setup.getBooleanValueFromConfigs(Setup.IS_VISUAL);
    }

    public static String getFromEnvironmentConfiguration(String key) {
        try {
            return Setup.getFromEnvironmentConfiguration(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(String.format(INVALID_KEY_MESSAGE, key), npe);
        }
    }

    public static String getTestData(String key) {
        try {
            return Setup.getTestDataValueAsStringForEnvironmentFor(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(String.format(INVALID_KEY_MESSAGE, key), npe);
        }
    }

    public static Map<String, Object> getTestDataAsMap(String key) {
        try {
            return Setup.getTestDataAsMapForEnvironmentFor(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(String.format(INVALID_KEY_MESSAGE, key), npe);
        }
    }

    public static void main(String[] args) {
        String logDir = System.getProperty("LOG_DIR");
        System.setProperty("OUTPUT_DIRECTORY", logDir);
        LOGGER.info("teswiz Runner");
        LOGGER.info("Provided parameters:");
        for(String arg : args) {
            LOGGER.info("\t" + arg);
        }
        if(args.length != 3) {
            throw new InvalidTestDataException(
                    "Expected following parameters: 'String configFilePath, String " +
                    "stepDefDirName, String " + "featuresDirName");
        }
        new Runner(args[0], args[1], args[2]);
    }

    public static TestExecutionContext getTestExecutionContext(long threadId) {
        return SessionContext.getTestExecutionContext(threadId);
    }

    public static SoftAssertions getSoftAssertion(long threadId) {
        return (SoftAssertions) getTestExecutionContext(threadId).getTestState(
                TEST_CONTEXT.SOFT_ASSERTIONS);
    }

    public static Driver setCurrentDriverForUser(String userPersona, Platform forPlatform,
                                                 TestExecutionContext context) {
        return Drivers.setDriverFor(userPersona, forPlatform, context);
    }

    public static Platform fetchPlatform(long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(
                TEST_CONTEXT.CURRENT_USER_PERSONA);
        return Drivers.getPlatformForUser(userPersona);
    }

    public static String getTargetEnvironment() {
        return Setup.getFromConfigs(Setup.TARGET_ENVIRONMENT);
    }

    public static String getBaseURLForWeb() {
        return Setup.getFromConfigs(Setup.BASE_URL_FOR_WEB);
    }

    public static String getAppPackageName() {
        return Setup.getFromConfigs(Setup.APP_PACKAGE_NAME);
    }

    public static boolean isRunningInCI() {
        return Setup.getBooleanValueFromConfigs(Setup.RUN_IN_CI);
    }

    public static String getBrowser() {
        return Setup.getFromConfigs(Setup.BROWSER);
    }

    public static String getProxyURL() {
        String proxyURL = Setup.getFromConfigs(Setup.PROXY_URL);
        LOGGER.info(String.format("Using proxyURL: %s", proxyURL));
        return proxyURL;
    }

    public static String getWebDriverManagerProxyURL() {
        String webDriverManagerProxyURL = Setup.getFromConfigs(Setup.WEBDRIVER_MANAGER_PROXY_URL);
        LOGGER.info(String.format("webDriverManagerProxyURL: %s", webDriverManagerProxyURL));
        return webDriverManagerProxyURL;
    }

    public static String getBrowserConfigFileContents() {
        InputStream inputStream;
        String browserConfigFile = Setup.getFromConfigs(Setup.BROWSER_CONFIG_FILE);
        try {
            if(browserConfigFile.contains(DEFAULT)) {
                inputStream = Runner.class.getResourceAsStream(Setup.DEFAULT_BROWSER_CONFIG_FILE);
            } else {
                inputStream = Files.newInputStream(Paths.get(browserConfigFile));
            }
        } catch(Exception e) {
            throw new InvalidTestDataException(
                    String.format("There was a problem while setting browser config file '%s'",
                                  browserConfigFile));
        }
        Setup.addToConfigs(Setup.BROWSER_CONFIG_FILE_CONTENTS,
                           new JSONObject(new JSONTokener(inputStream)).toString());
        return Setup.getFromConfigs(Setup.BROWSER_CONFIG_FILE_CONTENTS);
    }

    public static String getBrowserConfigFile() {
        return Setup.getFromConfigs(Setup.BROWSER_CONFIG_FILE);
    }
}
