package com.znsio.teswiz.runner;

import com.browserstack.local.Local;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.Randomizer;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

class BrowserStackSetup {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackSetup.class.getName());
    private static final String DEVICE = "device";
    private static Local bsLocal;
    private static final String BROWSERSTACK_LOCAL_IDENTIFIER = Randomizer.randomize(10);

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
        HashMap<String, Object> bstackOptions = new HashMap<String, Object>();
        copyLegacyBrowserStackOptionsToBstackOptions(loadedPlatformCapability, bstackOptions);
        bstackOptions.put("userName", authenticationUser);
        bstackOptions.put("accessKey", authenticationKey);
        Object appiumVersion = loadedPlatformCapability.get("browserstack.appiumVersion");
        if (null != appiumVersion) {
            bstackOptions.put("appiumVersion", appiumVersion);
        }
        bstackOptions.put("projectName", Setup.getFromConfigs(Setup.APP_NAME));
        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "")
                .replace("\\", "");
        bstackOptions.put("buildName", Setup.getFromConfigs(Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);
//        bstackOptions.put("sessionName", Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName());
        bstackOptions.put("debug", "true");
        bstackOptions.put("networkLogs", "true");
        bstackOptions.put("appProfiling", "true");
//        loadedPlatformCapability.put("browserstack.user", authenticationUser);
//        loadedPlatformCapability.put("browserstack.key", authenticationKey);
//        loadedPlatformCapability.put("browserstack.LoggedInUser", USER_NAME);
//        loadedPlatformCapability.put("browserstack.MachineName", getHostName());
        setupLocalTesting(authenticationKey, bstackOptions);
//        loadedPlatformCapability.put("build", Setup.getFromConfigs(
//                Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);
//        bstackOptions.put("project", Setup.getFromConfigs(Setup.APP_NAME));
        loadedPlatformCapability.put("bstack:options", bstackOptions);
        removeLegacyBrowserStackCapabilities(loadedPlatformCapability);
//        loadedPlatformCapability.put("deviceName", String.valueOf(loadedCapabilityFile.get(platformName).getOrDefault(DEVICE, "")));
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
            LOGGER.info(String.format(
                    "CLOUD_USE_LOCAL_TESTING=true. Setting up BrowserStackLocal testing using " + "identified: '%s'",
                    BROWSERSTACK_LOCAL_IDENTIFIER));
            startBrowserStackLocal(authenticationKey, BROWSERSTACK_LOCAL_IDENTIFIER);
            loadedPlatformCapability.put("local", "true");
            loadedPlatformCapability.put("localIdentifier", BROWSERSTACK_LOCAL_IDENTIFIER);
        }
    }

    static MutableCapabilities updateBrowserStackCapabilities(MutableCapabilities capabilities) {

        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Platform.web.name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);

        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "")
                                     .replace("\\", "");

        capabilities.setCapability("browserName", loadedPlatformCapability.get("browserName"));

        Map<String, String> browserstackOptions = getBrowserStackOptionsForWeb(loadedPlatformCapability);
        browserstackOptions.put("projectName", Setup.getFromConfigs(Setup.APP_NAME));
        browserstackOptions.put("buildName",
                                Setup.getFromConfigs(Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);

        browserstackOptions.put("sessionName", Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName());
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            LOGGER.info(String.format(
                    "CLOUD_USE_LOCAL_TESTING=true. Setting up BrowserStackLocal testing using " + "identified: '%s'",
                    BROWSERSTACK_LOCAL_IDENTIFIER));
            startBrowserStackLocal(authenticationKey, BROWSERSTACK_LOCAL_IDENTIFIER);
            browserstackOptions.put(ACCEPT_INSECURE_CERTS, "true");
            browserstackOptions.put("local", "true");
            browserstackOptions.put("localIdentifier", BROWSERSTACK_LOCAL_IDENTIFIER);
        }
        capabilities.setCapability("bstack:options", browserstackOptions);

        return capabilities;
    }

    private static Map<String, String> getBrowserStackOptionsForWeb(Map loadedPlatformCapability) {
        Object browserstackOptionsRaw = loadedPlatformCapability.get("browserstackOptions");
        if (browserstackOptionsRaw instanceof Map) {
            return (Map<String, String>) browserstackOptionsRaw;
        }
        Object bstackOptionsRaw = loadedPlatformCapability.get("bstack:options");
        if (bstackOptionsRaw instanceof Map) {
            return (Map<String, String>) bstackOptionsRaw;
        }
        return new HashMap<>();
    }

    private static void copyLegacyBrowserStackOptionsToBstackOptions(Map loadedPlatformCapability,
                                                                      Map<String, Object> bstackOptions) {
        loadedPlatformCapability.forEach((key, value) -> {
            String keyAsString = String.valueOf(key);
            if (keyAsString.startsWith("browserstack.")) {
                String normalizedKey = keyAsString.substring("browserstack.".length());
                if ("locale".equals(normalizedKey)) {
                    LOGGER.warn(String.format(
                            "Ignoring unsupported BrowserStack option '%s' in bstack:options. Use appium locale capabilities instead if needed.",
                            keyAsString));
                    return;
                }
                bstackOptions.put(normalizedKey, value);
            }
        });
    }

    private static void removeLegacyBrowserStackCapabilities(Map loadedPlatformCapability) {
        List<String> keysToRemove = new ArrayList<>();
        loadedPlatformCapability.forEach((key, value) -> {
            String keyAsString = String.valueOf(key);
            if (keyAsString.startsWith("browserstack.")) {
                keysToRemove.add(keyAsString);
            }
        });
        keysToRemove.forEach(loadedPlatformCapability::remove);
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
        bsLocalArgs.put("force", "true");
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

        JsonObject uploadResponse;
        try {
            uploadResponse = JsonFile.convertToMap(uploadAPKToBrowserStackResponse.getStdOut())
                                     .getAsJsonObject();
        } catch(IllegalStateException | JsonSyntaxException e) {
            throw new InvalidTestDataException(String.format(
                    "Failed to parse BrowserStack upload response for app: '%s'. ExitCode: %d, StdOut: '%s', StdErr: '%s'",
                    appPath, uploadAPKToBrowserStackResponse.getExitCode(),
                    uploadAPKToBrowserStackResponse.getStdOut(),
                    uploadAPKToBrowserStackResponse.getErrOut()), e);
        }

        JsonElement appUrl = uploadResponse.get("app_url");
        if (null == appUrl || appUrl.isJsonNull()) {
            JsonElement error = uploadResponse.get("error");
            String errorMessage = null != error && !error.isJsonNull()
                    ? error.getAsString()
                    : String.format("Missing 'app_url' in response: %s", uploadResponse);
            LOGGER.warn(String.format(
                    "Failed to upload app '%s' to BrowserStack. Attempting fallback to recent_apps lookup. Error: %s",
                    appPath, errorMessage));
            try {
                String existingAppIdFromBrowserStack = getAppIdFromBrowserStack(authenticationKey,
                                                                                 appPath, uploadUrl);
                LOGGER.info(String.format(
                        "Fallback succeeded. Using existing BrowserStack app id from recent_apps: '%s'",
                        existingAppIdFromBrowserStack));
                Setup.addToConfigs(Setup.APP_PATH, existingAppIdFromBrowserStack);
                return existingAppIdFromBrowserStack;
            } catch(InvalidTestDataException fallbackException) {
                throw new InvalidTestDataException(String.format(
                        "Failed to upload app '%s' to BrowserStack. Upload error: %s. Fallback to recent_apps also failed: %s",
                        appPath, errorMessage, fallbackException.getMessage()), fallbackException);
            }
        }
        String uploadedApkId = appUrl.getAsString();
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
