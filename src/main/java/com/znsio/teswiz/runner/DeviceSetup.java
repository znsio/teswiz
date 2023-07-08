package com.znsio.teswiz.runner;

import com.google.gson.internal.LinkedTreeMap;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
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
    private static final String DEFAULT_TEMP_SAMPLE_APP_DIRECTORY =
            System.getProperty("user.dir") + File.separator +
                    "temp" + File.separator + "sampleApps";

    private DeviceSetup() {
        LOGGER.debug("DeviceSetup - private constructor");
    }

    static void saveNewCapabilitiesFile(String platformName, String capabilityFile,
                                        Map<String, Map> loadedCapabilityFile,
                                        ArrayList listOfAndroidDevices) {
        Object pluginConfig = ((LinkedTreeMap) loadedCapabilityFile.get("serverConfig").get("server")).get(
                "plugin");
        Map cloudConfig = (Map) ((LinkedTreeMap) ((LinkedTreeMap) pluginConfig).get("device-farm")).get(
                "cloud");
        cloudConfig.put("devices", listOfAndroidDevices);

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
        if (Setup.getPlatform().equals(Platform.android)) {
            verifyAppExistsAtMentionedPath();
            fetchAndroidAppVersion();
            if (Setup.getBooleanValueFromConfigs(RUN_IN_CI)) {
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
        if (appPath.equals(NOT_SET)) {
            appPath = getAppPathFromCapabilities();
            appPath = downloadAppToDirectoryIfNeeded(appPath, DEFAULT_TEMP_SAMPLE_APP_DIRECTORY);
            Setup.addToConfigs(APP_PATH, appPath);
        } else {
            appPath = downloadAppToDirectoryIfNeeded(appPath, DEFAULT_TEMP_SAMPLE_APP_DIRECTORY);
            LOGGER.info(String.format("\tUsing AppPath provided as environment variable -  %s",
                    appPath));
        }
    }

    public static String downloadAppToDirectoryIfNeeded(String appPath, String saveToLocalDirectory) {
        String fileName = appPath.split(File.separator)[appPath.split(File.separator).length - 1];
        String localFilePath = saveToLocalDirectory + File.separator + fileName;
        if (isAppPathAUrl(appPath)) {
            LOGGER.info(String.format("App url '%s' is provided in capabilities. Download it, if " +
                                              "not already available at '%s'", appPath, localFilePath));
            downloadFileIfDoesNotExist(appPath, localFilePath, saveToLocalDirectory);
            LOGGER.info("Changing value of appPath from URL to file path");
            LOGGER.info(String.format("Before change, appPath value: %s", appPath));
            appPath = localFilePath;
            LOGGER.info(String.format("After change, appPath value: %s", localFilePath));
        } else {
            LOGGER.info(String.format("App file path '%s' is provided in capabilities.", appPath));
            if (!(new File(appPath).exists())) {
                throw new InvalidTestDataException(String.format("App file path '%s' provided in capabilities is incorrect", appPath));
//                checkEitherFilePathIsIncorrectOrFileIsMissing(appPath, localFilePath);
            }
        }
        LOGGER.info(String.format("App file path '%s' is provided in capabilities.", appPath));
        LOGGER.info(String.format("File available at App file path '%s'", appPath));
        return appPath;
    }

    private static void createDirectory(String directoryPath) {
        try {
            LOGGER.info("Directory doesn't exist, Creating directory");
            Files.createDirectories(Path.of(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to create directory: %s, error occurred%s", directoryPath, e));
        }
    }

    private static void downloadFile(String url, String filePath, String saveToDirectory) {
        LOGGER.info(String.format("Downloading App from url: '%s'", url));
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = getHttpURLConnection(fileUrl);
            downloadFileFromHttpURL(filePath, saveToDirectory, connection);
            String formattedSize = getDownloadedAppSize(Path.of(filePath));
            LOGGER.info(String.format("App downloaded at path: '%s', having size: '%s MB'", filePath, formattedSize));
        } catch (IOException e) {
            throw new InvalidTestDataException("An error occurred while opening the URL/downloading file: " + e.getMessage());
        }
    }

    private static String getDownloadedAppSize(Path filePath) {
        long fileSizeBytes = 0;
        try {
            fileSizeBytes = Files.size(filePath);
        } catch (IOException e) {
            throw new InvalidTestDataException("Unable to get downloaded app file size. Download " +
                                                       "may be corrupt. Check and fix before " +
                                                       "rerunning the test.", e);
        }
        double fileSizeMB = (double) fileSizeBytes / (1024 * 1024);
        return new DecimalFormat("#.##").format(fileSizeMB);
    }

    private static void downloadFileFromHttpURL(String filePath, String saveToDirectory, HttpURLConnection connection)  {
        try (InputStream inputStream = connection.getInputStream()) {
            createDirectoryIfNotExists(saveToDirectory);
            Files.copy(inputStream, Path.of(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidTestDataException(String.format("Unable to download file '%s'", connection.getURL().toString()), e);
        }
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection(URL fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new InvalidTestDataException(String.format("Unable to connect to url: '%s'. Got connection error '%d'", fileUrl, responseCode));
            }
            return connection;
        } catch (IOException e) {
            throw new InvalidTestDataException(String.format("Unable to connect to url: '%s'.", fileUrl));
        }
    }

    private static void createDirectoryIfNotExists(String directory) throws IOException {
        Path directoryPath = Path.of(directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    private static void downloadFileIfDoesNotExist(String appPath, String filePath, String saveToDirectory){
        if (!(new File(filePath).exists())) {
            LOGGER.info(String.format("App is not available at path: '%s'. Download it.", appPath));
            downloadFile(appPath, filePath, saveToDirectory);
        } else {
            LOGGER.info(String.format("App is already available at path: '%s'. No need to download it.", appPath));
        }
    }

    private static void fetchAndroidAppVersion() {
        Pattern versionNamePattern = Pattern.compile("versionName='(\\d+(\\.\\d+)+)'",
                Pattern.MULTILINE);
        String searchPattern = "grep";
        if (Runner.IS_WINDOWS) {
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
        } catch (Exception e) {
            LOGGER.info(
                    String.format("fetchAndroidAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    static void setupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        String deviceLabURL = NOT_SET;
        switch (cloudName.toLowerCase()) {
            case "headspin":
                deviceLabURL = getCloudUrlFromCapabilities();
                HeadSpinSetup.updateHeadspinCapabilities(deviceLabURL);
                break;
            case "pcloudy":
                deviceLabURL = getCloudApiUrlFromCapabilities();
                PCloudySetup.updatePCloudyCapabilities(deviceLabURL);
                break;
            case "browserstack":
                deviceLabURL = getCloudApiUrlFromCapabilities();
                BrowserStackSetup.updateBrowserStackCapabilities(deviceLabURL);
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
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile, new String[]{Setup.getPlatform().name(), "app"});
    }

    private static void checkIfAppExistsAtTheMentionedPath(String appPath,
                                                           String capabilitiesFileName) {
        if (!isAppPathAUrl(appPath)) {
            if (Files.exists(Paths.get(appPath))) {
                LOGGER.info(String.format("\tUsing AppPath: %s in file: %s:: %s", appPath,
                        capabilitiesFileName, Setup.getPlatform()));
            } else {
                LOGGER.info(String.format("\tAppPath: %s not found!", appPath));
                throw new InvalidTestDataException(
                        String.format("App file not found at the mentioned path: %s", appPath));
            }
        }
    }

    private static boolean isAppPathAUrl(String appPathUrl) {
        URL url;
        try {
            url = new URL(appPathUrl);
            LOGGER.info(String.format("'%s' is a URL.", appPathUrl));
            isAppUrlValid(appPathUrl);
            return true;
        } catch (MalformedURLException e) {
            LOGGER.info(String.format("'%s' is not a URL.", appPathUrl));
            return false;
        }
    }

    private static void isAppUrlValid(String appPathUrl) {
        int responseCode;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(appPathUrl).openConnection();
            connection.setRequestMethod("HEAD");
            responseCode = connection.getResponseCode();
            connection.disconnect();
        } catch (IOException e) {
            throw new InvalidTestDataException(String.format("Failed to make a connection using url: '%s'", appPathUrl) + e);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            LOGGER.info(String.format("'%s' is an invalid URL.", appPathUrl));
            throw new InvalidTestDataException("URL is not accessible: " + appPathUrl);
        }
        LOGGER.info(String.format("'%s' is a valid URL.", appPathUrl));
    }

    private static void fetchAppVersion(String[] commandToGetAppVersion, Pattern pattern) {
        CommandLineResponse commandResponse = CommandLineExecutor.execCommand(
                commandToGetAppVersion);
        String commandOutput = commandResponse.getStdOut();
        if (!(null == commandOutput || commandOutput.isEmpty())) {
            Matcher matcher = pattern.matcher(commandOutput);
            if (matcher.find()) {
                Setup.addToConfigs(APP_VERSION, matcher.group(1));
                LOGGER.info(String.format("APP_VERSION: %s", matcher.group(1)));
            }
        } else {
            LOGGER.info("fetchAppVersion: " + commandResponse.getErrOut());
        }
    }

    static String getCloudNameFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "cloudName"});
    }

    private static String getCloudUrlFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "url"});
    }

    private static String getCloudApiUrlFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getNodeValueAsStringFromJsonFile(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "apiUrl"});
    }

    static ArrayList<String> setupWindowsExecution() {
        ArrayList<String> windowsCukeArgs = new ArrayList<>();
        if (Setup.getPlatform().equals(Platform.windows)) {
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
        } catch (IOException e) {
            LOGGER.info(
                    String.format("fetchWindowsAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    static void cleanupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        switch (cloudName.toLowerCase()) {
            case "browserstack":
                BrowserStackSetup.cleanUp();
                break;
            case "headspin":
            case "pcloudy":
            case "saucelabs":
                LOGGER.info(String.format("No cleanup required for cloud: '%s'", cloudName));
                break;
            case "docker":
                LOGGER.info(String.format("No cleanup required for: '%s'", cloudName));
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Provided cloudName: '%s' is not supported", cloudName));
        }
    }
}
