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
import com.znsio.teswiz.mobile.provider.LambdaTestMobileCapabilitySetup;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import com.znsio.teswiz.web.provider.selenium.LambdaTestWebCapabilitySetup;

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
        LambdaTestMobileCapabilitySetup.prepareCapabilities(
                loadedPlatformCapability,
                authenticationUser,
                authenticationKey,
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING));

        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                getExistingCloudDevices(loadedCapabilityFile));
    }

    static MutableCapabilities updateLambdaTestCapabilities(MutableCapabilities capabilities) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(Platform.web.name());
        return LambdaTestWebCapabilitySetup.updateLambdaTestCapabilities(
                capabilities,
                loadedPlatformCapability,
                Setup.getFromConfigs(Setup.CLOUD_USERNAME),
                Setup.getFromConfigs(Setup.CLOUD_KEY),
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName(),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING));
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
        return LambdaTestMobileCapabilitySetup.resolveAppReference(loadedPlatformCapability,
                Setup.getFromConfigs(Setup.APP_PATH));
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
