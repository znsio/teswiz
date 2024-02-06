package com.znsio.teswiz.runner;

import com.browserstack.local.Local;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.Randomizer;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.MutableCapabilities;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.znsio.teswiz.runner.Runner.USER_NAME;
import static com.znsio.teswiz.runner.Runner.getHostName;
import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

class BrowserStackSetup {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackSetup.class.getName());
    private static final String DEVICE = "device";
    private static Local bsLocal;

    private BrowserStackSetup() {
        LOGGER.debug("BrowserStackSetup - private constructor");
    }

    static void updateBrowserStackCapabilities(String deviceLabURL) {
        String authenticationUser = Setup.getFromConfigs(Setup.CLOUD_USERNAME);
        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Setup.getPlatform().name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);

        addAppOrBrowserNameToBrowserStackCapablities(deviceLabURL, loadedPlatformCapability, authenticationUser, authenticationKey);
        loadedPlatformCapability.put("browserstack.user", authenticationUser);
        loadedPlatformCapability.put("browserstack.key", authenticationKey);
        loadedPlatformCapability.put("browserstack.LoggedInUser", USER_NAME);
        loadedPlatformCapability.put("browserstack.MachineName", getHostName());
        setupLocalTesting(authenticationKey, loadedPlatformCapability);
        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "")
                                     .replace("\\", "");
        loadedPlatformCapability.put("build", Setup.getFromConfigs(
                Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);
        loadedPlatformCapability.put("project", Setup.getFromConfigs(Setup.APP_NAME));
        updateBrowserStackDevicesInCapabilities(authenticationUser, authenticationKey,
                                                loadedCapabilityFile);
    }

    private static void addAppOrBrowserNameToBrowserStackCapablities(String deviceLabURL, Map loadedPlatformCapability, String authenticationUser, String authenticationKey) {
        Object browserName = loadedPlatformCapability.get(BROWSER_NAME);
        if (null != browserName) {
            LOGGER.info(String.format("app Id retreived from browser stack is: %s", browserName));
            loadedPlatformCapability.put("browserstack.browserName", browserName);
        } else {
            String appPath = new File(Setup.getFromConfigs(Setup.APP_PATH)).getAbsolutePath();
            String appIdFromBrowserStack = getAppIdFromBrowserStack(authenticationUser,
                    authenticationKey, appPath, deviceLabURL);
            LOGGER.info(String.format("app Id retreived from browser stack is: %s", appIdFromBrowserStack));
            loadedPlatformCapability.put("app", appIdFromBrowserStack);
        }
    }

    private static void setupLocalTesting(String authenticationKey, Map loadedPlatformCapability) {
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            String browserStackLocalIdentifier = Randomizer.randomize(10);
            LOGGER.info(String.format(
                    "CLOUD_USE_LOCAL_TESTING=true. Setting up BrowserStackLocal testing using " + "identified: '%s'",
                    browserStackLocalIdentifier));
            startBrowserStackLocal(authenticationKey, browserStackLocalIdentifier);
            loadedPlatformCapability.put("browserstack.local", "true");
            loadedPlatformCapability.put("browserstack.localIdentifier",
                                         browserStackLocalIdentifier);
        }
    }

    static MutableCapabilities updateBrowserStackCapabilities(MutableCapabilities capabilities) {

        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Platform.web.name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);

        String browserStackLocalIdentifier = Randomizer.randomize(10);
        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "")
                                     .replace("\\", "");

        capabilities.setCapability("browserName", loadedPlatformCapability.get("browserName"));

        Map<String, String> browserstackOptions =
                (Map<String, String>) loadedPlatformCapability.get(
                "browserstackOptions");
        browserstackOptions.put("projectName", Setup.getFromConfigs(Setup.APP_NAME));
        browserstackOptions.put("buildName",
                                Setup.getFromConfigs(Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);

        browserstackOptions.put("sessionName", Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName());
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            LOGGER.info(String.format(
                    "CLOUD_USE_LOCAL_TESTING=true. Setting up BrowserStackLocal testing using " + "identified: '%s'",
                    browserStackLocalIdentifier));
            startBrowserStackLocal(authenticationKey, browserStackLocalIdentifier);
            browserstackOptions.put(ACCEPT_INSECURE_CERTS, "true");
            browserstackOptions.put("local", "true");
            browserstackOptions.put("localIdentifier", browserStackLocalIdentifier);
        }
        capabilities.setCapability("bstack:options", browserstackOptions);

        return capabilities;
    }

    private static String getAppIdFromBrowserStack(String authenticationUser,
                                                   String authenticationKey, String appPath,
                                                   String apiUrl) {
        LOGGER.info(String.format("getAppIdFromBrowserStack: for %s", appPath));
        String appIdFromBrowserStack;
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_UPLOAD_APP)) {
            appIdFromBrowserStack = uploadAPKToBrowserStack(
                    authenticationUser + ":" + authenticationKey, appPath, apiUrl);
        } else {
            LOGGER.info("Skip uploading the apk to Device Farm");
            appIdFromBrowserStack = getAppIdFromBrowserStack(
                    authenticationUser + ":" + authenticationKey, appPath, apiUrl);
        }
        LOGGER.info("Using appId: " + appIdFromBrowserStack);
        return appIdFromBrowserStack;
    }

    private static void startBrowserStackLocal(String authenticationKey, String id) {
        bsLocal = new Local();

        HashMap<String, String> bsLocalArgs = new HashMap<>();
        bsLocalArgs.put("key", authenticationKey);
        bsLocalArgs.put("v", "true");
        bsLocalArgs.put("localIdentifier", id);
        bsLocalArgs.put("forcelocal", "true");
        bsLocalArgs.put("verbose", "3");
        try {
            LOGGER.info("Is BrowserStackLocal running? - " + bsLocal.isRunning());
            if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_PROXY)) {
                String proxyUrl = Setup.getFromConfigs(Setup.PROXY_URL);
                URL url = new URL(proxyUrl);
                String host = url.getHost();
                int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
                LOGGER.info(String.format("Using proxyHost: %s", host));
                LOGGER.info(String.format("Using proxyPort: %d", port));
                bsLocalArgs.put("proxyHost", host);
                bsLocalArgs.put("proxyPort", String.valueOf(port));
            }

            LOGGER.info(String.format("Start BrowserStackLocal using: %s", bsLocalArgs));
            bsLocal.start(bsLocalArgs);
            LOGGER.info(String.format("Is BrowserStackLocal started? - %s", bsLocal.isRunning()));
        } catch(Exception e) {
            throw new EnvironmentSetupException("Error starting BrowserStackLocal", e);
        }
    }

    private static void updateBrowserStackDevicesInCapabilities(String authenticationUser,
                                                                String authenticationKey,
                                                                Map<String, Map> loadedCapabilityFile) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String platformName = Setup.getPlatform().name();
        ArrayList listOfDevices = new ArrayList();

        String platformVersion = String.valueOf(
                loadedCapabilityFile.get(platformName).getOrDefault("platformVersion", ""));
        String deviceName =
                String.valueOf(loadedCapabilityFile.get(platformName).getOrDefault(DEVICE, ""));
        loadedCapabilityFile.get(platformName).remove(DEVICE);

        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("Platform", "mobile");// mobile-desktop
        filters.put("Os", platformName); // ios-android-Windows-OS X
        filters.put("Device", deviceName); // ios-android-Windows-OS X
        filters.put("Os_version", platformVersion); // os versions

        List<BrowserStackDevice> availableDevices = BrowserStackDeviceFilter.getFilteredDevices(
                authenticationUser, authenticationKey, filters,
                Setup.getFromConfigs(Setup.LOG_DIR));

        int deviceCount = Math.min(availableDevices.size(), Setup.getIntegerValueFromConfigs(
                Setup.MAX_NUMBER_OF_APPIUM_DRIVERS));
        LOGGER.info(String.format("Adding '%d' available devices for executing on BrowserStack",
                                  deviceCount));
        for(int numDevices = 0; numDevices < deviceCount; numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("platform", platformName.toLowerCase());
            deviceInfo.put("os_version", availableDevices.get(numDevices).getOs_version());
            deviceInfo.put("deviceName", availableDevices.get(numDevices).getDevice());
            listOfDevices.add(deviceInfo);
        }
        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                listOfDevices);
    }

    private static String uploadAPKToBrowserStack(String authenticationKey, String appPath,
                                                  String uploadUrl) {
        LOGGER.info(String.format("uploadAPKToBrowserStack for: '%s'%n", authenticationKey));

        String[] curlCommand = new String[]{
                "curl --insecure " + Setup.getCurlProxyCommand() + " -u \"" + authenticationKey + "\"",
                "-X POST \"" + uploadUrl + "upload\"",
                "-F \"file=@" + appPath + "\"", "-F \"custom_id=" + getAppName(appPath) + "\""};
        CommandLineResponse uploadAPKToBrowserStackResponse = CommandLineExecutor.execCommand(
                curlCommand);

        JsonObject uploadResponse = JsonFile.convertToMap(
                uploadAPKToBrowserStackResponse.getStdOut()).getAsJsonObject();
        String uploadedApkId = uploadResponse.get("app_url").getAsString();
        LOGGER.info(String.format("App: '%s' uploaded to BrowserStack. Response: '%s'", appPath,
                                  uploadResponse));
        Setup.addToConfigs(Setup.APP_PATH, uploadedApkId);
        return uploadedApkId;
    }

    private static String getAppIdFromBrowserStack(String authenticationKey, String appPath,
                                                   String apiUrl) {
        String appName = getAppName(appPath);
        LOGGER.info(String.format("getAppIdFromBrowserStack for: '%s' and appName: '%s'%n",
                                  authenticationKey, appName));
        String[] curlCommand = new String[]{
                "curl --insecure " + Setup.getCurlProxyCommand() + " -u \"" + authenticationKey + "\"",
                "-X GET \"" + apiUrl + "recent_apps/" + appName + "\""};
        String uploadedAppIdFromBrowserStack;
        try {
            CommandLineResponse uploadAPKToBrowserStackResponse = CommandLineExecutor.execCommand(
                    curlCommand);
            LOGGER.debug("uploadAPKToBrowserStackResponse: " + uploadAPKToBrowserStackResponse);

            JsonArray uploadResponse = JsonFile.convertToArray(
                    uploadAPKToBrowserStackResponse.getStdOut());
            uploadedAppIdFromBrowserStack = uploadResponse.get(0).getAsJsonObject().get("app_url")
                                                          .getAsString();
        } catch(IllegalStateException | NullPointerException | JsonSyntaxException e) {
            throw new InvalidTestDataException(String.format(
                    "App with id: '%s' is not uploaded to BrowserStack. %nError: '%s'", appName,
                    e.getMessage()));
        }
        LOGGER.info(String.format("getAppIdFromBrowserStack: AppId: '%s'%n",
                                  uploadedAppIdFromBrowserStack));
        return uploadedAppIdFromBrowserStack;
    }

    private static String getAppName(String appPath) {
        return new File(appPath).getName();
    }

    static void cleanUp() {
        stopBrowserStackLocal();
    }

    private static void stopBrowserStackLocal() {
        LOGGER.info(String.format("stopBrowserStackLocal: CLOUD_USE_LOCAL_TESTING=%s",
                                  Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)));
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            try {
                LOGGER.info(
                        String.format("Is BrowserStackLocal running? - %s", bsLocal.isRunning()));
                if(bsLocal.isRunning()) {
                    LOGGER.info("Stopping BrowserStackLocal");
                    bsLocal.stop();
                    LOGGER.info(String.format("Is BrowserStackLocal stopped? - %s",
                                              !bsLocal.isRunning()));
                }
            } catch(Exception e) {
                throw new EnvironmentSetupException("Exception in stopping BrowserStackLocal", e);
            }
        }
    }
}
