package com.znsio.teswiz.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.znsio.teswiz.runner.Runner.NOT_SET;

class HeadSpinSetup {
    private static final Logger LOGGER = LogManager.getLogger(HeadSpinSetup.class.getName());
    private static final String PLATFORM_VERSION = "platformVersion";

    private HeadSpinSetup() {
        LOGGER.debug("HeadSpinSetup - private constructor");
    }

    static void updateHeadspinCapabilities(String deviceLabURL) {
        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Setup.getPlatform().name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String appPath = Setup.getFromConfigs(Setup.APP_PATH);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map<String, Map> serverConfig = (Map<String, Map>) loadedCapabilityFile.get("serverConfig").get("server");
        Map<String, Map> deviceFarm = (Map<String, Map>) serverConfig.get("plugin").get("device-farm");
        String cloudUrl = (String) deviceFarm.get("cloud").get("url");
        cloudUrl = cloudUrl.replace("token", authenticationKey);
        deviceFarm.get("cloud").put("url", cloudUrl);

        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        String appIdFromHeadspin;
        if (Setup.getBooleanValueFromConfigs(Setup.CLOUD_UPLOAD_APP)) {
            appIdFromHeadspin = uploadAPKToHeadspin(authenticationKey, appPath, deviceLabURL);
        } else {
            LOGGER.info("Skip uploading the apk to Device Farm");
            appIdFromHeadspin = getAppIdFromHeadspin(authenticationKey,
                    Setup.getFromConfigs(Setup.APP_PACKAGE_NAME),
                    deviceLabURL);
        }
        LOGGER.info("Using appId: " + appIdFromHeadspin);
        loadedPlatformCapability.put("headspin:app.id", appIdFromHeadspin);
        loadedPlatformCapability.remove("app");
        String osVersion = String.valueOf(loadedPlatformCapability.getOrDefault(PLATFORM_VERSION, ""));
        String deviceManufacturer = loadedPlatformCapability.getOrDefault("device", "").toString().toUpperCase();
//        loadedPlatformCapability.put("headspin:selector", "os_version:>=" + osVersion + "+device_type:" + platformName + "+manufacturer:" + deviceManufacturer);
        loadedPlatformCapability.remove(PLATFORM_VERSION);
        updateCapabilities(loadedCapabilityFile, osVersion, deviceManufacturer);
    }

    private static String uploadAPKToHeadspin(String authenticationKey, String appPath, String deviceLabURL) {
        LOGGER.info(String.format("uploadAPKToHeadspin for: '%s'%n", authenticationKey));
        String[] curlCommand = new String[]{
                "curl --insecure " + Setup.getCurlProxyCommand() + " -X POST ",
                "https://" + authenticationKey + "@" + deviceLabURL + "/app/upload " +
                        "-F app='@" + appPath + "'"};
        CommandLineResponse uploadAPKToHeadspinResponse = CommandLineExecutor.execCommand(
                curlCommand);

        JsonObject uploadResponse = JsonFile.convertToMap(uploadAPKToHeadspinResponse.getStdOut())
                .getAsJsonObject();
        String uploadedApkId = uploadResponse.get("app_id").getAsString();
        LOGGER.info(String.format("App: '%s' uploaded to Headspin. Response: '%s'", appPath,
                uploadResponse));

        JsonObject listOfAppPackages = getListOfAppPackagesFromHeadSpin(authenticationKey, deviceLabURL);
        String uploadedAppName = NOT_SET;
        for (JsonElement apps : listOfAppPackages.getAsJsonArray("apps")) {
            String appId = apps.getAsJsonObject().get("app_id").getAsString();
            if (appId.equals(uploadedApkId)) {
                uploadedAppName = String.valueOf(apps.getAsJsonObject().get("app_name"));
                break;
            }
        }
        Setup.addToConfigs(Setup.APP_PATH, uploadedAppName);
        return uploadedApkId;
    }

    private static String getAppIdFromHeadspin(String authenticationKey, String appPackageName,
                                               String deviceLabURL) {
        LOGGER.info("getAppIdFromHeadspin for package: " + appPackageName);

        AtomicReference<String> uploadedAppId = new AtomicReference<>(NOT_SET);
        JsonObject listOfAppPackages = getListOfAppPackagesFromHeadSpin(authenticationKey,
                deviceLabURL);
        if (!listOfAppPackages.keySet().isEmpty()) {
            getAppIdFromAvailableAppsFromHeadspin(appPackageName, listOfAppPackages, uploadedAppId);
        }

        if (uploadedAppId.get().equalsIgnoreCase(NOT_SET)) {
            throw new InvalidTestDataException(
                    String.format("App with package: '%s' not available in Headspin",
                            appPackageName));
        }

        return uploadedAppId.get();
    }

    static void updateCapabilities(Map<String, Map> loadedCapabilityFile, String osVersion, String deviceManufacturer) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String platformName = Setup.getPlatform().name();
        ArrayList listOfAndroidDevices = new ArrayList();
        for (int numDevices = 0;
             numDevices < Setup.getIntegerValueFromConfigs(Setup.MAX_NUMBER_OF_APPIUM_DRIVERS);
             numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("platform", platformName.toLowerCase());
            deviceInfo.put("platformVersion", osVersion);
            deviceInfo.put("deviceName", deviceManufacturer);
            listOfAndroidDevices.add(deviceInfo);
        }
        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                listOfAndroidDevices);
    }

    private static JsonObject getListOfAppPackagesFromHeadSpin(String authenticationKey,
                                                               String deviceLabURL) {
        String[] curlCommand = new String[]{"curl --insecure", Setup.getCurlProxyCommand(),
                "https://" + authenticationKey + "@" + deviceLabURL + "/apps?limit=0"};
        CommandLineResponse listOfUploadedFilesInHeadspinResponse = CommandLineExecutor.execCommand(
                curlCommand);

        JsonObject listOfAppPackages = JsonFile.convertToMap(
                listOfUploadedFilesInHeadspinResponse.getStdOut()).getAsJsonObject();
        JsonElement statusCode = listOfAppPackages.get("status_code");
        if (null != statusCode && statusCode.getAsInt() != 200) {
            throw new InvalidTestDataException(
                    "There was a problem getting the list of apps in Headspin");
        }
        return listOfAppPackages;
    }

    private static void getAppIdFromAvailableAppsFromHeadspin(String appPackageName,
                                                              JsonObject listOfAppPackages,
                                                              AtomicReference<String> uploadedAppId) {

        for (JsonElement apps : listOfAppPackages.getAsJsonArray("apps")) {
            String appIdentifier = apps.getAsJsonObject().get("app_identifier").getAsString();
            if (appIdentifier.equals(appPackageName)) {
                uploadedAppId.set(apps.getAsJsonObject().get("app_id").getAsString());
                Setup.addToConfigs(Setup.APP_PATH, uploadedAppId.get());
                break;
            }
        }
    }

}
