package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.znsio.teswiz.runner.Runner.NOT_SET;
import static com.znsio.teswiz.runner.Setup.APP_PATH;
import static com.znsio.teswiz.runner.Setup.APP_VERSION;
import static com.znsio.teswiz.runner.Setup.CAPS;
import static com.znsio.teswiz.runner.Setup.EXECUTED_ON;
import static com.znsio.teswiz.runner.Setup.LOG_DIR;
import static com.znsio.teswiz.runner.Setup.PARALLEL;
import static com.znsio.teswiz.runner.Setup.PLUGIN;
import static com.znsio.teswiz.runner.Setup.RUN_IN_CI;

class DeviceSetup {
    private static final Logger LOGGER = Logger.getLogger(DeviceSetup.class.getName());

    private DeviceSetup() {
        LOGGER.debug("DeviceSetup - private constructor");
    }

    static void saveNewCapabilitiesFile(String platformName, String capabilityFile,
                                        Map<String, Map> loadedCapabilityFile,
                                        ArrayList listOfAndroidDevices) {
        Map loadedCloudCapability = loadedCapabilityFile.get("cloud");
        loadedCloudCapability.put(platformName, listOfAndroidDevices);

        LOGGER.info(
                String.format("Updated Device Lab Capabilities file: %n%s", loadedCapabilityFile));

        String updatedCapabilitiesFile = getPathForFileInLogDir(capabilityFile);
        JsonFile.saveJsonToFile(loadedCapabilityFile, updatedCapabilitiesFile);
        Setup.addToConfigs(CAPS, updatedCapabilitiesFile);
    }

    static String getPathForFileInLogDir(String fullFilePath) {
        LOGGER.info(String.format("\tgetPathForFileInLogDir: fullFilePath: %s", fullFilePath));
        Path path = Paths.get(fullFilePath);
        String fileName = path.getFileName().toString();
        String newFileName = new File(
                Setup.getFromConfigs(LOG_DIR) + File.separator + fileName).getAbsolutePath();
        LOGGER.info(String.format("\tNew file available here: %s", newFileName));
        return newFileName;
    }

    static ArrayList<String> setupAndroidExecution() {
        ArrayList<String> androidCukeArgs = new ArrayList<>();
        if(Setup.getPlatform().equals(Platform.android)) {
            verifyAppExistsAtMentionedPath();
            fetchAndroidAppVersion();
            if(Setup.getBooleanValueFromConfigs(RUN_IN_CI)) {
                setupCloudExecution();
            } else {
                LocalDevicesSetup.setupLocalExecution();
            }
            androidCukeArgs.add("--threads");
            androidCukeArgs.add(Setup.getIntegerValueAsStringFromConfigs(PARALLEL));
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add("com.cucumber.listener.CucumberScenarioListener");
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add("com.cucumber.listener.CucumberScenarioReporterListener");
        }
        return androidCukeArgs;
    }

    static void verifyAppExistsAtMentionedPath() {
        String appPath = Setup.getFromConfigs(APP_PATH);
        LOGGER.info(String.format("Update path to Apk: %s", appPath));
        if(appPath.equals(NOT_SET)) {
            appPath = getAppPathFromCapabilities();
            Setup.addToConfigs(APP_PATH, appPath);
            String capabilitiesFileName = Setup.getFromConfigs(CAPS);
            checkIfAppExistsAtTheMentionedPath(appPath, capabilitiesFileName);
        } else {
            LOGGER.info(String.format("\tUsing AppPath provided as environment variable -  %s",
                                      appPath));
        }
    }

    private static void fetchAndroidAppVersion() {
        Pattern versionNamePattern = Pattern.compile("versionName='(\\d+(\\.\\d+)+)'",
                                                     Pattern.MULTILINE);
        String searchPattern = "grep";
        if(Runner.IS_WINDOWS) {
            searchPattern = "findstr";
        }

        try {
            File appFile = new File(Setup.getFromConfigs(APP_PATH));
            if (!isAppPathAUrl(appFile.getPath())) {
                String appFilePath = appFile.getCanonicalPath();
                String androidHomePath = System.getenv("ANDROID_HOME");
                File buildToolsFolder = new File(androidHomePath, "build-tools");
                File buildVersionFolder = Objects.requireNonNull(buildToolsFolder.listFiles())[0];
                File aaptExecutable = new File(buildVersionFolder, "aapt").getAbsoluteFile();

                String[] commandToGetAppVersion = new String[]{aaptExecutable.toString(), "dump",
                                                               "badging", appFilePath, "|",
                                                               searchPattern, "versionName"};
                fetchAppVersion(commandToGetAppVersion, versionNamePattern);
            }
        } catch(Exception e) {
            LOGGER.info(
                    String.format("fetchAndroidAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    static void setupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        switch(cloudName.toLowerCase()) {
            case "headspin":
                HeadSpinSetup.updateHeadspinCapabilities();
                break;
            case "pcloudy":
                PCloudySetup.updatePCloudyCapabilities();
                break;
            case "browserstack":
                BrowserStackSetup.updateBrowserStackCapabilities();
                break;
            case "saucelabs":
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Provided cloudName: '%s' is not supported", cloudName));
        }
        Setup.addToConfigs(EXECUTED_ON, cloudName);
    }

    private static String getAppPathFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile,
                                                         new String[]{Setup.getPlatform().name(), "app"});
    }

    private static void checkIfAppExistsAtTheMentionedPath(String appPath,
                                                           String capabilitiesFileName) {
        if(!isAppPathAUrl(appPath)) {
            if(Files.exists(Paths.get(appPath))) {
                LOGGER.info(String.format("\tUsing AppPath: %s in file: %s:: %s", appPath,
                                          capabilitiesFileName, Setup.getPlatform()));
            } else {
                LOGGER.info(String.format("\tAppPath: %s not found!", appPath));
                throw new InvalidTestDataException(
                        String.format("App file not found at the mentioned path: %s", appPath));
            }
        }
    }

    private static boolean isAppPathAUrl(String appPath) {
        boolean isUrl = appPath.toLowerCase().startsWith("http");
        LOGGER.info(String.format("\tAppPath refers to a url: %s", appPath));
        return isUrl;
    }

    private static void fetchAppVersion(String[] commandToGetAppVersion, Pattern pattern) {
        CommandLineResponse commandResponse = CommandLineExecutor.execCommand(
                commandToGetAppVersion);
        String commandOutput = commandResponse.getStdOut();
        if(!(null == commandOutput || commandOutput.isEmpty())) {
            Matcher matcher = pattern.matcher(commandOutput);
            if(matcher.find()) {
                Setup.addToConfigs(APP_VERSION, matcher.group(1));
                LOGGER.info(String.format("APP_VERSION: %s", matcher.group(1)));
            }
        } else {
            LOGGER.info("fetchAppVersion: " + commandResponse.getErrOut());
        }
    }

    private static String getCloudNameFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        ArrayList<Map> hostMachines = JsonFile.getNodeValueAsArrayListFromJsonFile(capabilityFile,
                                                                                   "hostMachines");
        return String.valueOf(hostMachines.get(0).get("cloudName"));
    }

    static ArrayList<String> setupWindowsExecution() {
        ArrayList<String> windowsCukeArgs = new ArrayList<>();
        if(Setup.getPlatform().equals(Platform.windows)) {
            verifyAppExistsAtMentionedPath();
            fetchWindowsAppVersion();
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add("com.cucumber.listener.CucumberScenarioListener");
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add("com.cucumber.listener.CucumberScenarioReporterListener");
            Setup.addToConfigs(EXECUTED_ON, "Local Desktop Apps");
        }
        return windowsCukeArgs;
    }

    private static void fetchWindowsAppVersion() {
        Pattern versionNamePattern = Pattern.compile("Version=(\\d+(\\.\\d+)+)", Pattern.MULTILINE);
        try {
            File appFile = new File(Setup.getFromConfigs(APP_PATH));
            String nameVariable = "name=\"" + appFile.getCanonicalPath()
                                                     .replace("\\", "\\\\") + "\"";
            String[] commandToGetAppVersion = new String[]{"wmic", "datafile", "where",
                                                           nameVariable, "get", "Version",
                                                           "/value"};
            fetchAppVersion(commandToGetAppVersion, versionNamePattern);
        } catch(IOException e) {
            LOGGER.info(
                    String.format("fetchWindowsAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    static void cleanupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        switch(cloudName.toLowerCase()) {
            case "browserstack":
                BrowserStackSetup.cleanUp();
                break;
            case "headspin":
            case "pcloudy":
            case "saucelabs":
                LOGGER.info(String.format("No cleanup required for cloud: '%s'", cloudName));
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Provided cloudName: '%s' is not supported", cloudName));
        }
    }
}
