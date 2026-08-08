package com.znsio.teswiz.mobile.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;

public class LambdaTestMobileSetup {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTestMobileSetup.class.getName());

    private LambdaTestMobileSetup() {
        LOGGER.debug("LambdaTestMobileSetup - private constructor");
    }

    public static void updateLambdaTestCapabilities(String apiUrl) {
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

        com.znsio.teswiz.runner.DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                getExistingCloudDevices(loadedCapabilityFile));
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
        String[] curlCommand = LambdaTestMobileAppUpload.buildUploadCurlCommand(
                authenticationUser,
                authenticationKey,
                appPath,
                apiUrl,
                Setup.getCurlProxyCommand());
        CommandLineResponse uploadResponse = CommandLineExecutor.execCommand(curlCommand);
        String uploadedAppUrl = LambdaTestMobileAppUpload.parseUploadedAppUrl(
                appPath,
                uploadResponse.getStdOut());
        Setup.addToConfigs(Setup.APP_PATH, uploadedAppUrl);
        return uploadedAppUrl;
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
