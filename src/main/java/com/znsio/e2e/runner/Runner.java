package com.znsio.e2e.runner;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.entities.TEST_CONTEXT;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Drivers;
import com.znsio.e2e.tools.Visual;
import io.cucumber.core.cli.Main;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.znsio.e2e.runner.Setup.*;

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

    public static Platform getPlatform() {
        return Setup.getPlatform();
    }

    public void run(List<String> args, String stepDefsDir, String featuresDir) {
        args.add("--glue");
        args.add(stepDefsDir);
        args.add(featuresDir);
        LOGGER.info("Begin running tests...");
        LOGGER.info("Args: " + args);
        String[] array = args.toArray(String[]::new);
        String logDir = Runner.USER_DIRECTORY + File.separator + Setup.getFromConfigs(
                LOG_DIR) + File.separator + REPORTS_DIR;
        LOGGER.info(logDir);
        byte status = Main.run(array);
        Setup.cleanUpExecutionEnvironment();
        System.out.println(generateReport(logDir));
        System.exit(status);
    }

    private static String generateReport(String reportsDir) {
        System.out.println("================================");
        System.out.println("Generating reports");
        System.out.println("================================");
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"},
                                                         true);
        System.out.println(
                String.format("\tFound '%s' result files for processing", jsonFiles.size()));
        if(jsonFiles.size() == 0) {
            return "Reports not generated";
        }
        List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
        jsonFiles.forEach(file -> {
            System.out.println("\tProcessing result file: " + file.getAbsolutePath());
            jsonPaths.add(file.getAbsolutePath());
        });
        String richReportsPath = reportsDir + File.separator + "richReports";
        System.out.println("\tCreating rich reports: " + richReportsPath);
        Configuration config = new Configuration(new File(richReportsPath), Setup.getFromConfigs(APP_NAME));

        String tagsToExclude = System.getProperty(
                TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT);
        if(null != tagsToExclude) {
            config.setTagsToExcludeFromChart(tagsToExclude.trim().split(","));
        }
        addClassifications(config);

        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
        return "Reports available here: " + config.getReportDirectory()
                                                  .getAbsolutePath() + "/cucumber-html-reports" +
               "/overview-features.html";
    }

    private static void addClassifications(Configuration config) {
        config.addClassifications("Environment", Setup.getFromConfigs(TARGET_ENVIRONMENT));
        config.addClassifications("Platform", Setup.getFromConfigs(PLATFORM));
        config.addClassifications("Tags", Setup.getFromConfigs(TAG));
        config.addClassifications("RUN_IN_CI", Setup.getBooleanValueAsStringFromConfigs(RUN_IN_CI));
        config.addClassifications("IS_VISUAL", Setup.getBooleanValueAsStringFromConfigs(IS_VISUAL));
        config.addClassifications("CLOUD_NAME", Setup.getFromConfigs(CLOUD_NAME));
        config.addClassifications("EXECUTED_ON", Setup.getFromConfigs(EXECUTED_ON));
    }

    public static Map getApplitoolsConfiguration() {
        return initialiseApplitoolsConfiguration();
    }

    public static String getCloudName() {
        return Setup.getFromConfigs(CLOUD_NAME);
    }

    public static String getCloudUser() {
        return Setup.getFromConfigs(CLOUD_USER);
    }

    public static String getCloudKey() {
        return Setup.getFromConfigs(CLOUD_KEY);
    }

    public static String getRemoteDriverGridPort() {
        return Setup.getFromConfigs(REMOTE_WEBDRIVER_GRID_PORT);
    }

    public static int getMaxNumberOfAppiumDrivers() {
        return Setup.getIntegerValueFromConfigs(MAX_NUMBER_OF_APPIUM_DRIVERS);
    }

    public static int getMaxNumberOfWebDrivers() {
        return Setup.getIntegerValueFromConfigs(MAX_NUMBER_OF_WEB_DRIVERS);
    }

    public static boolean isVisualTestingEnabled() {
        return Setup.getBooleanValueFromConfigs(IS_VISUAL);
    }

    public static void remove(long threadId) {
        SessionContext.remove(threadId);
    }

    public static String getFromEnvironmentConfiguration(String key) {
        try {
            return Setup.getFromEnvironmentConfiguration(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(
                    String.format("Invalid key name ('%s') provided", key), npe);
        }
    }

    public static String getTestData(String key) {
        try {
            return Setup.getTestDataValueAsStringForEnvironmentFor(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(
                    String.format("Invalid key name ('%s') provided", key), npe);
        }
    }

    public static Map getTestDataAsMap(String key) {
        try {
            return Setup.getTestDataAsMapForEnvironmentFor(key);
        } catch(NullPointerException npe) {
            throw new InvalidTestDataException(
                    String.format("Invalid key name ('%s') provided", key), npe);
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

    public static Driver fetchDriver(long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(
                TEST_CONTEXT.CURRENT_USER_PERSONA);
        return Drivers.getDriverForUser(userPersona);
    }

    public static TestExecutionContext getTestExecutionContext(long threadId) {
        return SessionContext.getTestExecutionContext(threadId);
    }

    public static String fetchDeviceName(long threadId, String forUserPersona) {
        return Drivers.getDeviceNameForUser(forUserPersona);
    }

    public static Visual fetchEyes(long threadId) {
        String userPersona = getTestExecutionContext(threadId).getTestStateAsString(
                TEST_CONTEXT.CURRENT_USER_PERSONA);
        return Drivers.getDriverForUser(userPersona).getVisual();
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

    public static void closeAllDrivers(long threadId) {
        Drivers.attachLogsAndCloseAllWebDrivers();
    }

    public static String getTargetEnvironment() {
        return Setup.getFromConfigs(TARGET_ENVIRONMENT);
    }

    public static String getBaseURLForWeb() {
        return Setup.getFromConfigs(BASE_URL_FOR_WEB);
    }

    public static String getAppPackageName() {
        return Setup.getFromConfigs(APP_PACKAGE_NAME);
    }

    public static boolean isRunningInCI() {
        return Setup.getBooleanValueFromConfigs(RUN_IN_CI);
    }

    public static String getBrowser() {
        return Setup.getFromConfigs(BROWSER);
    }

    public static String getProxyURL() {
        String proxyURL = Setup.getFromConfigs(PROXY_URL);
        LOGGER.info("Using proxyURL: " + proxyURL);
        return proxyURL;
    }

    public static String getWebDriverManagerProxyURL() {
        String webDriverManagerProxyURL = Setup.getFromConfigs(WEBDRIVER_MANAGER_PROXY_URL);
        LOGGER.info("webDriverManagerProxyURL: " + webDriverManagerProxyURL);
        return webDriverManagerProxyURL;
    }

    public static String getBrowserConfigFileContents() {
        InputStream inputStream;
        String browserConfigFile = Setup.getFromConfigs(BROWSER_CONFIG_FILE);
        try {
            if(browserConfigFile.contains("default")) {
                inputStream = Runner.class.getResourceAsStream(DEFAULT_BROWSER_CONFIG_FILE);
            } else {
                inputStream = Files.newInputStream(Paths.get(browserConfigFile));
            }
        } catch(Exception e) {
            throw new InvalidTestDataException(
                    String.format("There was a problem while setting browser config file '%s'",
                                  browserConfigFile));
        }
        Setup.addToConfigs(BROWSER_CONFIG_FILE_CONTENTS,
                    new JSONObject(new JSONTokener(inputStream)).toString());
        return Setup.getFromConfigs(BROWSER_CONFIG_FILE_CONTENTS);
    }

    public static String getBrowserConfigFile() {
        return Setup.getFromConfigs(BROWSER_CONFIG_FILE);
    }
}
