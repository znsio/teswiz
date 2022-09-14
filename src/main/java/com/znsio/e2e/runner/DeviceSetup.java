package com.znsio.e2e.runner;

import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.tools.JsonFile;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
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

import static com.znsio.e2e.runner.Runner.*;
import static com.znsio.e2e.runner.Setup.*;

public class DeviceSetup {
    private static final Logger LOGGER = Logger.getLogger(DeviceSetup.class.getName());

    static void saveNewCapabilitiesFile(String platformName, String capabilityFile, Map<String, Map> loadedCapabilityFile, ArrayList listOfAndroidDevices) {
        Map loadedCloudCapability = loadedCapabilityFile.get("cloud");
        loadedCloudCapability.put(platformName, listOfAndroidDevices);

        LOGGER.info("Updated Device Lab Capabilities file: \n" + loadedCapabilityFile);

        String updatedCapabilitiesFile = getPathForFileInLogDir(capabilityFile);
        JsonFile.saveJsonToFile(loadedCapabilityFile, updatedCapabilitiesFile);
        configs.put(CAPS, updatedCapabilitiesFile);
    }

    static String getPathForFileInLogDir(String fullFilePath) {
        LOGGER.info("\tgetPathForFileInLogDir: fullFilePath: " + fullFilePath);
        Path path = Paths.get(fullFilePath);
        String fileName = path.getFileName()
                              .toString();
        String newFileName = new File(configs.get(LOG_DIR) + File.separator + fileName).getAbsolutePath();
        LOGGER.info("\tNew file available here: " + newFileName);
        return newFileName;
    }

    static ArrayList<String> setupAndroidExecution() {
        ArrayList<String> androidCukeArgs = new ArrayList<>();
        if(platform.equals(Platform.android)) {
            verifyAppExistsAtMentionedPath();
            fetchAndroidAppVersion();
            if(configsBoolean.get(RUN_IN_CI)) {
                setupCloudExecution();
            } else {
                LocalDevicesSetup.setupLocalExecution();
            }
            androidCukeArgs.add("--threads");
            androidCukeArgs.add(String.valueOf(configsInteger.get(PARALLEL)));
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add("com.cucumber.listener.CucumberScenarioListener");
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add("com.cucumber.listener.CucumberScenarioReporterListener");
        }
        return androidCukeArgs;
    }

    static void verifyAppExistsAtMentionedPath() {
        String appPath = String.valueOf(configs.get(APP_PATH));
        LOGGER.info("Update path to Apk: " + appPath);
        if(appPath.equals(NOT_SET)) {
            appPath = getAppPathFromCapabilities();
            configs.put(APP_PATH, appPath);
            String capabilitiesFileName = configs.get(CAPS);
            checkIfAppExistsAtTheMentionedPath(appPath, capabilitiesFileName);
        } else {
            LOGGER.info("\tUsing AppPath provided as environment variable -  " + appPath);
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
                throw new InvalidTestDataException(String.format("Provided cloudName: '%s' is not supported", cloudName));
        }
        configs.put(EXECUTED_ON, cloudName);
    }

    private static void fetchAndroidAppVersion() {
        Pattern VERSION_NAME_PATTERN = Pattern.compile("versionName='([0-9]+(\\.[0-9]+)+)'", Pattern.MULTILINE);
        String searchPattern = "grep";
        if(Runner.IS_WINDOWS) {
            searchPattern = "findstr";
        }

        try {
            File appFile = new File(String.valueOf(configs.get(APP_PATH)));
            String appFilePath = appFile.getCanonicalPath();
            String androidHomePath = System.getenv("ANDROID_HOME");
            File buildToolsFolder = new File(androidHomePath, "build-tools");
            File buildVersionFolder = Objects.requireNonNull(buildToolsFolder.listFiles())[0];
            File aaptExecutable = new File(buildVersionFolder, "aapt").getAbsoluteFile();

            String[] commandToGetAppVersion = new String[]{aaptExecutable.toString(), "dump", "badging", appFilePath, "|", searchPattern, "versionName"};
            fetchAppVersion(commandToGetAppVersion, VERSION_NAME_PATTERN);
        } catch(Exception e) {
            LOGGER.info("fetchAndroidAppVersion: Exception: " + e.getLocalizedMessage());
        }
    }

    private static String getAppPathFromCapabilities() {
        String capabilityFile = configs.get(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile, new String[]{platform.name(), "app", "local"});
    }

    private static void checkIfAppExistsAtTheMentionedPath(String appPath, String capabilitiesFileName) {
        if(Files.exists(Paths.get(appPath))) {
            LOGGER.info("\tUsing AppPath: " + appPath + " in file: " + capabilitiesFileName + ":: " + platform);
        } else {
            LOGGER.info("\tAppPath: " + appPath + " not found!");
            throw new InvalidTestDataException("App file not found at the mentioned path: " + appPath);
        }
    }

    private static String getCloudNameFromCapabilities() {
        String capabilityFile = configs.get(CAPS);
        ArrayList<Map> hostMachines = JsonFile.getNodeValueAsArrayListFromJsonFile(capabilityFile, "hostMachines");
        return String.valueOf(hostMachines.get(0)
                                          .get("cloudName"));
    }

    private static void fetchAppVersion(String[] commandToGetAppVersion, Pattern pattern) {
        CommandLineResponse commandResponse = CommandLineExecutor.execCommand(commandToGetAppVersion);
        String commandOutput = commandResponse.getStdOut();
        if(!(null == commandOutput || commandOutput.isEmpty())) {
            Matcher matcher = pattern.matcher(commandOutput);
            if(matcher.find()) {
                configs.put(APP_VERSION, matcher.group(1));
                LOGGER.info("APP_VERSION: " + matcher.group(1));
            }
        } else {
            LOGGER.info("fetchAppVersion: " + commandResponse.getErrOut());
        }
    }

    static ArrayList<String> setupWindowsExecution() {
        ArrayList<String> windowsCukeArgs = new ArrayList<>();
        if(platform.equals(Platform.windows)) {
            verifyAppExistsAtMentionedPath();
            fetchWindowsAppVersion();
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add("com.cucumber.listener.CucumberScenarioListener");
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add("com.cucumber.listener.CucumberScenarioReporterListener");
            configs.put(EXECUTED_ON, "Local Desktop Apps");
        }
        return windowsCukeArgs;
    }

    private static void fetchWindowsAppVersion() {
        Pattern VERSION_NAME_PATTERN = Pattern.compile("Version=([0-9]+(\\.[0-9]+)+)", Pattern.MULTILINE);
        try {
            File appFile = new File(String.valueOf(configs.get(APP_PATH)));
            String nameVariable = "name=\"" + appFile.getCanonicalPath()
                                                     .replace("\\", "\\\\") + "\"";
            String[] commandToGetAppVersion = new String[]{"wmic", "datafile", "where", nameVariable, "get", "Version", "/value"};
            fetchAppVersion(commandToGetAppVersion, VERSION_NAME_PATTERN);
        } catch(IOException e) {
            LOGGER.info("fetchWindowsAppVersion: Exception: " + e.getLocalizedMessage());
        }
    }
}
