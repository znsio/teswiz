package com.znsio.teswiz.runner;

import com.znsio.teswiz.config.app.AppPathResolver;
import com.znsio.teswiz.config.app.AppVersionDetector;
import com.znsio.teswiz.config.capability.CapabilityConfigResolver;
import com.znsio.teswiz.config.capability.CapabilityFileManager;
import com.znsio.teswiz.mobile.device.LocalMobileDeviceSetup;
import com.znsio.teswiz.mobile.provider.MobileCloudExecutionManager;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.tools.OsUtils;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.znsio.teswiz.runner.Runner.NOT_SET;
import com.znsio.teswiz.mobile.provider.BrowserStackMobileSetup;
import com.znsio.teswiz.mobile.provider.LambdaTestMobileSetup;
import com.znsio.teswiz.mobile.provider.HeadSpinMobileSetup;
import com.znsio.teswiz.mobile.provider.PCloudyMobileSetup;

import static com.znsio.teswiz.runner.Setup.*;

public class DeviceSetup {
    private static final Logger LOGGER = LogManager.getLogger(DeviceSetup.class.getName());
    private static final String DEFAULT_TEMP_SAMPLE_APP_DIRECTORY =
            System.getProperty("user.dir") + File.separator +
                    "temp" + File.separator + "sampleApps";
    private static final String CUCUMBER_SCENARIO_LISTENER = "com.znsio.teswiz.listener.CucumberScenarioListener";
    private static final String CUCUMBER_SCENARIO_REPORTER_LISTENER = "com.znsio.teswiz.listener.CucumberScenarioReporterListener";
    private static final AppVersionDetector APP_VERSION_DETECTOR = new AppVersionDetector();
    private static final MobileCloudExecutionManager MOBILE_CLOUD_EXECUTION_MANAGER =
            new MobileCloudExecutionManager(
                    BrowserStackMobileSetup::updateBrowserStackCapabilities,
                    LambdaTestMobileSetup::updateLambdaTestCapabilities,
                    HeadSpinMobileSetup::updateHeadspinCapabilities,
                    PCloudyMobileSetup::updatePCloudyCapabilities,
                    BrowserStackMobileSetup::cleanUp);

    private DeviceSetup() {
        LOGGER.debug("DeviceSetup - private constructor");
    }

    public static void saveNewCapabilitiesFile(String platformName, String capabilityFile,
                                        Map<String, Map> loadedCapabilityFile,
                                        ArrayList listOfDevices) {
        String updatedCapabilitiesFile = CapabilityFileManager.saveDeviceFarmCapabilities(
                capabilityFile,
                loadedCapabilityFile,
                listOfDevices,
                Setup.getFromConfigs(LOG_DIR));
        Setup.addToConfigs(CAPS, updatedCapabilitiesFile);
    }

    static String getPathForFileInLogDir(String fullFilePath) {
        return CapabilityFileManager.getPathForFileInLogDir(fullFilePath, Setup.getFromConfigs(LOG_DIR));
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
            if (!AppPathResolver.isAppPathUrl(appFile.getPath())) {
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
        return CapabilityConfigResolver.getAppPath(
                Setup.getFromConfigs(CAPS),
                Setup.getPlatform().name(),
                Setup.getLoadedCapabilities());
    }

    public static String getCloudNameFromCapabilities() {
        return CapabilityConfigResolver.getCloudName(
                Runner.isRunningInCI(),
                Runner.isAPI(),
                Runner.isCLI(),
                Runner.isPDF(),
                Setup.getFromConfigs(CAPS),
                Setup.getLoadedCapabilities(),
                NOT_SET);
    }

    static String getCloudUrlFromCapabilities() {
        return CapabilityConfigResolver.getCloudUrl(Setup.getFromConfigs(CAPS), Setup.getLoadedCapabilities());
    }

    static String getCloudApiUrlFromCapabilities() {
        return CapabilityConfigResolver.getCloudApiUrl(Setup.getFromConfigs(CAPS), Setup.getLoadedCapabilities());
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
