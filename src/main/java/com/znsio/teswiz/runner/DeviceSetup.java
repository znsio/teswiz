package com.znsio.teswiz.runner;

import com.google.gson.internal.LinkedTreeMap;
import com.znsio.teswiz.config.app.AppPathResolver;
import com.znsio.teswiz.config.app.AppVersionDetector;
import com.znsio.teswiz.mobile.device.LocalMobileDeviceSetup;
import com.znsio.teswiz.mobile.provider.MobileCloudExecutionManager;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

import static com.znsio.teswiz.runner.Runner.NOT_SET;
import static com.znsio.teswiz.runner.Setup.*;

class DeviceSetup {
    private static final Logger LOGGER = LogManager.getLogger(DeviceSetup.class.getName());
    private static final String DEFAULT_TEMP_SAMPLE_APP_DIRECTORY =
            System.getProperty("user.dir") + File.separator +
                    "temp" + File.separator + "sampleApps";
    private static final String CUCUMBER_SCENARIO_LISTENER = "com.znsio.teswiz.listener.CucumberScenarioListener";
    private static final String CUCUMBER_SCENARIO_REPORTER_LISTENER = "com.znsio.teswiz.listener.CucumberScenarioReporterListener";
    private static final AppVersionDetector APP_VERSION_DETECTOR = new AppVersionDetector();
    private static final MobileCloudExecutionManager MOBILE_CLOUD_EXECUTION_MANAGER =
            new MobileCloudExecutionManager(
                    BrowserStackSetup::updateBrowserStackCapabilities,
                    LambdaTestSetup::updateLambdaTestCapabilities,
                    HeadSpinSetup::updateHeadspinCapabilities,
                    PCloudySetup::updatePCloudyCapabilities,
                    BrowserStackSetup::cleanUp);

    private DeviceSetup() {
        LOGGER.debug("DeviceSetup - private constructor");
    }

    static void saveNewCapabilitiesFile(String platformName, String capabilityFile,
                                        Map<String, Map> loadedCapabilityFile,
                                        ArrayList listOfDevices) {
        Object pluginConfig = ((LinkedTreeMap) loadedCapabilityFile.get("serverConfig").get("server")).get(
                "plugin");
        Map cloudConfig = (Map) ((LinkedTreeMap) ((LinkedTreeMap) pluginConfig).get("device-farm")).get(
                "cloud");
        cloudConfig.put("devices", listOfDevices);

        LOGGER.info(String.format("Updated Device Lab Capabilities file: %n%s", JsonPrettyPrinter.prettyPrint(loadedCapabilityFile)));

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
                LocalMobileDeviceSetup.setupLocalExecution();
            }
            androidCukeArgs.add("--threads");
            androidCukeArgs.add(Setup.getIntegerValueAsStringFromConfigs(PARALLEL));
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add(CUCUMBER_SCENARIO_LISTENER);
            androidCukeArgs.add(PLUGIN);
            androidCukeArgs.add(CUCUMBER_SCENARIO_REPORTER_LISTENER);
        }
        return androidCukeArgs;
    }

    static void verifyAppExistsAtMentionedPath() {
        String appPath = Setup.getFromConfigs(APP_PATH);
        LOGGER.info(String.format("Original path to apk/app: %s", SensitiveDataMasker.mask(appPath)));
        if (appPath.equals(NOT_SET)) {
            if (null == Setup.getLoadedCapabilities().get(Runner.getPlatform().name()).get("browserName")) {
                appPath = downloadAppToDirectoryIfNeeded(getAppPathFromCapabilities(), DEFAULT_TEMP_SAMPLE_APP_DIRECTORY);
            }
            LOGGER.info(String.format("Updated path to apk/app: %s", SensitiveDataMasker.mask(appPath)));
        } else {
            appPath = downloadAppToDirectoryIfNeeded(appPath, DEFAULT_TEMP_SAMPLE_APP_DIRECTORY);
            LOGGER.info(String.format("\tUsing AppPath provided as environment variable -  %s",
                    SensitiveDataMasker.mask(appPath)));
        }
        Setup.addToConfigs(APP_PATH, appPath);
    }

    public static String downloadAppToDirectoryIfNeeded(String appPath, String saveToLocalDirectory) {
        return AppPathResolver.resolveAppPath(appPath, saveToLocalDirectory);
    }

    private static void fetchAndroidAppVersion() {
        try {
            File appFile = new File(Setup.getFromConfigs(APP_PATH));
            if (!isAppPathAUrl(appFile.getPath())) {
                APP_VERSION_DETECTOR.detectAndroidAppVersion(appFile.getPath(), System.getenv("ANDROID_HOME"),
                                OsUtils.isWindows())
                        .ifPresent(DeviceSetup::setAppVersion);
            }
        } catch (Exception e) {
            LOGGER.info(
                    String.format("fetchAndroidAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    static void setupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        MOBILE_CLOUD_EXECUTION_MANAGER.setupCloudExecution(cloudName, getCloudApiUrlFromCapabilities());
        Setup.addToConfigs(EXECUTED_ON, cloudName);
    }

    private static String getAppPathFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{Setup.getPlatform().name(), "app"}, Setup.getLoadedCapabilities());
    }

    private static boolean isAppPathAUrl(String appPathUrl) {
        try {
            new URL(appPathUrl);
            LOGGER.info(String.format("'%s' is a URL.", appPathUrl));
            isAppUrlValid(appPathUrl);
            return true;
        } catch (MalformedURLException e) {
            LOGGER.info(String.format("'%s' is not a URL.", appPathUrl));
            return false;
        }
    }

    private static void isAppUrlValid(String appPathUrl) {
        int responseCode=999;
        String responseMessage=NOT_SET;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(appPathUrl).openConnection();
            connection.setRequestMethod("HEAD");
            responseMessage = connection.getResponseMessage();
            responseCode = connection.getResponseCode();
            connection.disconnect();
        } catch (IOException e) {
            LOGGER.info(MessageFormat.format("isAppUrlValid response message: {0}'', responseCode: {1}",
                    SensitiveDataMasker.mask(responseMessage), responseCode));
            throw new InvalidTestDataException(String.format("Failed to make a connection using url: '%s'", appPathUrl) + e);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            LOGGER.info(String.format("'%s' is an invalid URL.", appPathUrl));
            throw new InvalidTestDataException("URL is not accessible: " + appPathUrl);
        }
        LOGGER.info(String.format("'%s' is a valid URL.", appPathUrl));
    }

    static String getCloudNameFromCapabilities() {
        if (Runner.isRunningInCI() && !Runner.isAPI() && !Runner.isCLI() && !Runner.isPDF()) {
            String capabilityFile = Setup.getFromConfigs(CAPS);
            return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                    new String[]{"serverConfig", "server", "plugin",
                            "device-farm", "cloud", "cloudName"}, Setup.getLoadedCapabilities());
        } else {
            return NOT_SET;
        }
    }

    static String getCloudUrlFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "url"}, Setup.getLoadedCapabilities());
    }

    static String getCloudApiUrlFromCapabilities() {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "apiUrl"}, Setup.getLoadedCapabilities());
    }

    static ArrayList<String> setupWindowsExecution() {
        ArrayList<String> windowsCukeArgs = new ArrayList<>();
        if (Setup.getPlatform().equals(Platform.windows)) {
            verifyAppExistsAtMentionedPath();
            fetchWindowsAppVersion();
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add(CUCUMBER_SCENARIO_LISTENER);
            windowsCukeArgs.add(PLUGIN);
            windowsCukeArgs.add(CUCUMBER_SCENARIO_REPORTER_LISTENER);
            Setup.addToConfigs(EXECUTED_ON, "Local Desktop Apps");
        }
        return windowsCukeArgs;
    }

    private static void fetchWindowsAppVersion() {
        try {
            APP_VERSION_DETECTOR.detectWindowsAppVersion(Setup.getFromConfigs(APP_PATH))
                    .ifPresent(DeviceSetup::setAppVersion);
        } catch (IOException e) {
            LOGGER.info(
                    String.format("fetchWindowsAppVersion: Exception: %s", e.getLocalizedMessage()));
        }
    }

    private static void setAppVersion(String appVersion) {
        Setup.addToConfigs(APP_VERSION, appVersion);
        LOGGER.info(String.format("APP_VERSION: %s", appVersion));
    }

    static void cleanupCloudExecution() {
        String cloudName = getCloudNameFromCapabilities();
        MOBILE_CLOUD_EXECUTION_MANAGER.cleanupCloudExecution(cloudName);
    }

    static ArrayList<String> setupIOSExecution()  {
        ArrayList<String> iOSCukeArgs = new ArrayList<>();
        if (Setup.getPlatform().equals(Platform.iOS)) {
            verifyAppExistsAtMentionedPath();
//            TODO
//            fetchIOSAppVersion();
            if (Setup.getBooleanValueFromConfigs(RUN_IN_CI)) {
                setupCloudExecution();
            } else {
                LocalMobileDeviceSetup.setupLocalIOSExecution();
            }
            iOSCukeArgs.add("--threads");
            iOSCukeArgs.add(Setup.getIntegerValueAsStringFromConfigs(PARALLEL));
            iOSCukeArgs.add(PLUGIN);
            iOSCukeArgs.add(CUCUMBER_SCENARIO_LISTENER);
            iOSCukeArgs.add(PLUGIN);
            iOSCukeArgs.add(CUCUMBER_SCENARIO_REPORTER_LISTENER);
        }
        return iOSCukeArgs;
    }

}
