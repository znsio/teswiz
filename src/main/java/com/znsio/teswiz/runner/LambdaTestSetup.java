package com.znsio.teswiz.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

class LambdaTestSetup {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTestSetup.class.getName());
    private static final String LT_OPTIONS = "LT:Options";
    private static final String LT_OPTIONS_APPIUM = "lt:options";
    private static final String DEVICE = "device";
    private static final String PLATFORM_VERSION = "platformVersion";
    private static final String OS_VERSION = "os_version";

    private LambdaTestSetup() {
        LOGGER.debug("LambdaTestSetup - private constructor");
    }

    static void updateLambdaTestCapabilities(String apiUrl) {
        String authenticationUser = Setup.getFromConfigs(Setup.CLOUD_USERNAME);
        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Setup.getPlatform().name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);

        addAppOrBrowserNameToLambdaTestCapabilities(apiUrl, loadedPlatformCapability,
                authenticationUser, authenticationKey);

        Map<String, Object> ltOptions = getOrCreateMobileLtOptions(loadedPlatformCapability);
        ltOptions.put("username", authenticationUser);
        ltOptions.put("accessKey", authenticationKey);
        ltOptions.put("project", Setup.getFromConfigs(Setup.APP_NAME));
        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "")
                .replace("\\", "");
        ltOptions.put("build", Setup.getFromConfigs(Setup.LAUNCH_NAME) + "-" + subsetOfLogDir);
        ltOptions.put("w3c", true);

        if (Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            ltOptions.put("tunnel", true);
        }

        normalizeMobileCapabilitiesForLambdaTest(loadedPlatformCapability);
        loadedPlatformCapability.put(LT_OPTIONS_APPIUM, ltOptions);

        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                getExistingCloudDevices(loadedCapabilityFile));
    }

    static MutableCapabilities updateLambdaTestCapabilities(MutableCapabilities capabilities) {
        String platformName = Platform.web.name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String authenticationUser = Setup.getFromConfigs(Setup.CLOUD_USERNAME);
        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        String subsetOfLogDir = Setup.getFromConfigs(Setup.LOG_DIR).replace("/", "").replace("\\", "");
        String buildName = Setup.getFromConfigs(Setup.LAUNCH_NAME) + "-" + subsetOfLogDir;
        String sessionName = Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName();

        // Keep only W3C-valid top-level keys.
        setCapabilityIfPresent(capabilities, "browserName", loadedPlatformCapability, "browserName",
                "browser");
        Object browserVersion = getValueFrom(loadedPlatformCapability, "browserVersion", "version",
                "browser_version");
        if (browserVersion != null) {
            capabilities.setCapability("browserVersion", browserVersion);
        }

        Object platformValue = getValueFrom(loadedPlatformCapability, "platformName", "platform");
        if (platformValue == null) {
            Object os = loadedPlatformCapability.get("os");
            Object osVersion = loadedPlatformCapability.get("os_version");
            if (os != null && osVersion != null) {
                platformValue = os + " " + osVersion;
            }
        }
        if (platformValue != null) {
            capabilities.setCapability("platformName", platformValue);
        }

        // Put LambdaTest-specific keys under LT:Options to satisfy W3C capability validation.
        Map<String, Object> ltOptions = new HashMap<>(getOrCreateWebLtOptions(loadedPlatformCapability));
        ltOptions.put("username", authenticationUser);
        ltOptions.put("accessKey", authenticationKey);
        ltOptions.put("project", Setup.getFromConfigs(Setup.APP_NAME));
        ltOptions.put("build",
                getValueFrom(loadedPlatformCapability, "build", "buildName") != null
                        ? getValueFrom(loadedPlatformCapability, "build", "buildName")
                        : buildName);
        ltOptions.put("name",
                getValueFrom(loadedPlatformCapability, "name", "sessionName") != null
                        ? getValueFrom(loadedPlatformCapability, "name", "sessionName")
                        : sessionName);
        ltOptions.put("w3c", true);

        setOptionIfPresent(ltOptions, "resolution", loadedPlatformCapability, "resolution");
        setOptionIfPresent(ltOptions, "network", loadedPlatformCapability, "network");
        setOptionIfPresent(ltOptions, "appProfiling", loadedPlatformCapability, "appProfiling");
        setOptionIfPresent(ltOptions, "console", loadedPlatformCapability, "console");
        setOptionIfPresent(ltOptions, "visual", loadedPlatformCapability, "visual");
        setOptionIfPresent(ltOptions, "tunnel", loadedPlatformCapability, "tunnel");
        if (browserVersion != null) {
            ltOptions.putIfAbsent("browserVersion", browserVersion);
        }
        if (platformValue != null) {
            ltOptions.putIfAbsent("platformName", platformValue);
        }
        if (Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING)) {
            ltOptions.put("tunnel", true);
        }
        capabilities.setCapability(LT_OPTIONS, ltOptions);
        return capabilities;
    }

    private static void normalizeMobileCapabilitiesForLambdaTest(Map loadedPlatformCapability) {
        Object platformVersion = loadedPlatformCapability.getOrDefault(PLATFORM_VERSION,
                loadedPlatformCapability.getOrDefault(OS_VERSION, ""));
        if (!String.valueOf(platformVersion).isEmpty()) {
            loadedPlatformCapability.put(PLATFORM_VERSION, platformVersion);
        }

        Object deviceName = loadedPlatformCapability.getOrDefault("deviceName",
                loadedPlatformCapability.getOrDefault(DEVICE, ""));
        if (!String.valueOf(deviceName).isEmpty()) {
            loadedPlatformCapability.put("deviceName", deviceName);
        }

        loadedPlatformCapability.remove(DEVICE);
        loadedPlatformCapability.remove(OS_VERSION);
    }

    private static Map<String, Object> getOrCreateMobileLtOptions(Map loadedPlatformCapability) {
        Object existing = loadedPlatformCapability.get(LT_OPTIONS_APPIUM);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> ltOptions = new HashMap<>();
        loadedPlatformCapability.put(LT_OPTIONS_APPIUM, ltOptions);
        return ltOptions;
    }

    private static Map<String, Object> getOrCreateWebLtOptions(Map loadedPlatformCapability) {
        Object existing = loadedPlatformCapability.get(LT_OPTIONS);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }

        existing = loadedPlatformCapability.get("ltOptions");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }

        existing = loadedPlatformCapability.get(LT_OPTIONS_APPIUM);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        return new HashMap<>();
    }

    private static void addAppOrBrowserNameToLambdaTestCapabilities(String apiUrl,
            Map loadedPlatformCapability,
            String authenticationUser,
            String authenticationKey) {
        Object browserName = loadedPlatformCapability.get("browserName");
        if (null != browserName) {
            return;
        }

        if (Setup.getBooleanValueFromConfigs(Setup.CLOUD_UPLOAD_APP)) {
            String appPath = new File(Setup.getFromConfigs(Setup.APP_PATH)).getAbsolutePath();
            String appIdFromLambdaTest = uploadAppToLambdaTest(authenticationUser, authenticationKey,
                    appPath, apiUrl);
            LOGGER.info(String.format("App uploaded to LambdaTest with app url: %s", appIdFromLambdaTest));
            loadedPlatformCapability.put("app", appIdFromLambdaTest);
        } else {
            String lambdaTestAppReference = getLambdaTestAppReference(loadedPlatformCapability);
            LOGGER.info(String.format("Skip uploading the app to LambdaTest. Using app reference: %s",
                    lambdaTestAppReference));
            loadedPlatformCapability.put("app", lambdaTestAppReference);
        }
    }

    private static String getLambdaTestAppReference(Map loadedPlatformCapability) {
        String configuredAppPath = Setup.getFromConfigs(Setup.APP_PATH);
        if (isLambdaTestAppReference(configuredAppPath)) {
            return configuredAppPath;
        }

        Object capabilityApp = loadedPlatformCapability.get("app");
        if (capabilityApp != null && isLambdaTestAppReference(String.valueOf(capabilityApp))) {
            return String.valueOf(capabilityApp);
        }

        throw new InvalidTestDataException(
                "CLOUD_UPLOAD_APP=false for LambdaTest requires APP_PATH or the platform capability 'app' to be a valid LambdaTest app id like 'lt://APP123'.");
    }

    private static boolean isLambdaTestAppReference(String appReference) {
        return appReference != null && appReference.startsWith("lt://");
    }

    private static String uploadAppToLambdaTest(String authenticationUser,
            String authenticationKey,
            String appPath,
            String apiUrl) {
        String uploadUrl = apiUrl.endsWith("/") ? apiUrl + "app/upload/realDevice" : apiUrl + "/app/upload/realDevice";
        String[] curlCommand = new String[] {
                "curl --insecure " + Setup.getCurlProxyCommand() + " -u \"" + authenticationUser + ":"
                        + authenticationKey + "\"",
                "-X POST \"" + uploadUrl + "\"",
                "-F \"appFile=@" + appPath + "\""
        };
        CommandLineResponse uploadResponse = CommandLineExecutor.execCommand(curlCommand);
        try {
            JsonObject uploadResult = JsonFile.convertToMap(uploadResponse.getStdOut()).getAsJsonObject();
            if (uploadResult.has("app_url")) {
                String uploadedAppUrl = uploadResult.get("app_url").getAsString();
                Setup.addToConfigs(Setup.APP_PATH, uploadedAppUrl);
                return uploadedAppUrl;
            }
        } catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
            throw new InvalidTestDataException("Unable to parse LambdaTest app upload response", e);
        }
        throw new InvalidTestDataException(String.format("Unable to upload app '%s' to LambdaTest. Response: %s",
                appPath, uploadResponse.getStdOut()));
    }

    private static void setCapabilityIfPresent(MutableCapabilities capabilities,
            String capabilityName,
            Map loadedPlatformCapability,
            String... sourceKeys) {
        Object value = getValueFrom(loadedPlatformCapability, sourceKeys);
        if (value != null) {
            capabilities.setCapability(capabilityName, value);
        }
    }

    private static Object getValueFrom(Map loadedPlatformCapability, String... sourceKeys) {
        for (String sourceKey : sourceKeys) {
            if (loadedPlatformCapability.containsKey(sourceKey)) {
                return loadedPlatformCapability.get(sourceKey);
            }
        }
        return null;
    }

    private static void setOptionIfPresent(Map<String, Object> options,
            String optionName,
            Map loadedPlatformCapability,
            String... sourceKeys) {
        Object value = getValueFrom(loadedPlatformCapability, sourceKeys);
        if (value != null) {
            options.put(optionName, value);
        }
    }

    private static ArrayList getExistingCloudDevices(Map<String, Map> loadedCapabilityFile) {
        Object cloudObject = ((Map) ((Map) ((Map) loadedCapabilityFile.get("serverConfig")).get("server"))
                .get("plugin")).get("device-farm");
        Object devices = ((Map) ((Map) cloudObject).get("cloud")).get("devices");
        if (devices instanceof ArrayList) {
            return (ArrayList) devices;
        }
        return new ArrayList();
    }
}
