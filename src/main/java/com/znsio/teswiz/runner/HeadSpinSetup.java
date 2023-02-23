package com.znsio.teswiz.runner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.znsio.teswiz.runner.Runner.NOT_SET;

public class HeadSpinSetup {
    private static final Logger LOGGER = Logger.getLogger(HeadSpinSetup.class.getName());
    private static final String PLATFORM_VERSION = "platformVersion";

    private HeadSpinSetup() {
        LOGGER.debug("HeadSpinSetup - private constructor");
    }
    
    static void updateHeadspinCapabilities() {
        String authenticationKey = Setup.getFromConfigs(Setup.CLOUD_KEY);
        String platformName = Setup.getPlatform().name();
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String appPath = Setup.getFromConfigs(Setup.APP_PATH);

        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        String osVersion = String.valueOf(loadedPlatformCapability.get(PLATFORM_VERSION));
        String appIdFromHeadspin;
        if(Setup.getBooleanValueFromConfigs(Setup.CLOUD_UPLOAD_APP)) {
            appIdFromHeadspin = uploadAPKToHeadspin(authenticationKey, appPath);
        } else {
            LOGGER.info("Skip uploading the apk to Device Farm");
            appIdFromHeadspin = getAppIdFromHeadspin(authenticationKey,
                                                     Setup.getFromConfigs(Setup.APP_PACKAGE_NAME));
        }
        LOGGER.info("Using appId: " + appIdFromHeadspin);
        loadedPlatformCapability.put("headspin:appId", appIdFromHeadspin);

        ArrayList hostMachinesList = (ArrayList) loadedCapabilityFile.get("hostMachines");
        Map hostMachines = (Map) hostMachinesList.get(0);
        String remoteServerURL = String.valueOf(hostMachines.get("machineIP"));
        remoteServerURL = remoteServerURL.endsWith("/") ? remoteServerURL + authenticationKey
                                                        : remoteServerURL + "/" + authenticationKey;
        hostMachines.put("machineIP", remoteServerURL);
        loadedPlatformCapability.remove("app");
        loadedPlatformCapability.remove(PLATFORM_VERSION);
        loadedPlatformCapability.put("headspin:selector", "os_version: >=" + osVersion);
        loadedPlatformCapability.put("headspin:capture", true);
        loadedPlatformCapability.put("headspin:capture.video", true);
        loadedPlatformCapability.put("headspin:capture.network", true);
        updateCapabilities(loadedCapabilityFile);
    }

    private static String uploadAPKToHeadspin(String authenticationKey, String appPath) {
        LOGGER.info(String.format("uploadAPKToHeadspin for: '%s'%n", authenticationKey));
        String deviceLabURL = Setup.getFromConfigs(Setup.DEVICE_LAB_URL);

        String[] curlCommand = new String[]{
                "curl --insecure " + Setup.getCurlProxyCommand() + " -X POST ",
                "https://" + authenticationKey + "@" + deviceLabURL + "/v0/apps/apk/upload " +
                "--data-binary '@" + appPath + "'"};
        CommandLineResponse uploadAPKToHeadspinResponse = CommandLineExecutor.execCommand(
                curlCommand);

        JsonObject uploadResponse = JsonFile.convertToMap(uploadAPKToHeadspinResponse.getStdOut())
                                            .getAsJsonObject();
        String uploadedApkId = uploadResponse.get("apk_id").getAsString();
        LOGGER.info(String.format("App: '%s' uploaded to Headspin. Response: '%s'", appPath,
                                  uploadResponse));

        JsonObject listOfAppPackages = getListOfAppPackagesFromHeadSpin(authenticationKey);
        JsonObject uploadedAppDetails = listOfAppPackages.getAsJsonObject(uploadedApkId);
        String uploadedAppName = uploadedAppDetails.get("app_name").getAsString();
        Setup.addToConfigs(Setup.APP_PATH, uploadedAppName);
        return uploadedApkId;
    }

    private static String getAppIdFromHeadspin(String authenticationKey, String appPackageName) {
        LOGGER.info("getAppIdFromHeadspin for package: " + appPackageName);

        AtomicReference<String> uploadedAppId = new AtomicReference<>(NOT_SET);
        JsonObject listOfAppPackages = getListOfAppPackagesFromHeadSpin(authenticationKey);
        if(!listOfAppPackages.keySet().isEmpty()) {
            getAppIdFromAvailableAppsFromHeadspin(appPackageName, listOfAppPackages, uploadedAppId);
        }

        if(uploadedAppId.get().equalsIgnoreCase(NOT_SET)) {
            throw new InvalidTestDataException(
                    String.format("App with package: '%s' not available in Headspin",
                                  appPackageName));
        }

        return uploadedAppId.get();
    }

    static void updateCapabilities(Map<String, Map> loadedCapabilityFile) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        String platformName = Setup.getPlatform().name();
        ArrayList listOfAndroidDevices = new ArrayList();
        for(int numDevices = 0;
            numDevices < Setup.getIntegerValueFromConfigs(Setup.MAX_NUMBER_OF_APPIUM_DRIVERS);
            numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("osVersion", String.valueOf(
                    loadedCapabilityFile.get(platformName).get(PLATFORM_VERSION)));
            deviceInfo.put("deviceName", String.valueOf(
                    loadedCapabilityFile.get(platformName).get("platformName")));
            listOfAndroidDevices.add(deviceInfo);
        }
        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                                            listOfAndroidDevices);
    }

    private static JsonObject getListOfAppPackagesFromHeadSpin(String authenticationKey) {
        String deviceLabURL = Setup.getFromConfigs(Setup.DEVICE_LAB_URL);
        String[] curlCommand = new String[]{"curl --insecure", Setup.getCurlProxyCommand(),
                                            "https://" + authenticationKey + "@" + deviceLabURL + "/v0/apps/apks"};
        CommandLineResponse listOfUploadedFilesInHeadspinResponse = CommandLineExecutor.execCommand(
                curlCommand);

        JsonObject listOfAppPackages = JsonFile.convertToMap(
                listOfUploadedFilesInHeadspinResponse.getStdOut()).getAsJsonObject();
        JsonElement statusCode = listOfAppPackages.get("status_code");
        if(null != statusCode && statusCode.getAsInt() != 200) {
            throw new InvalidTestDataException(
                    "There was a problem getting the list of apps in Headspin");
        }
        return listOfAppPackages;
    }

    private static void getAppIdFromAvailableAppsFromHeadspin(String appPackageName,
                                                              JsonObject listOfAppPackages,
                                                              AtomicReference<String> uploadedAppId) {
        listOfAppPackages.keySet().forEach(appId -> {
            if(uploadedAppId.get().equalsIgnoreCase(NOT_SET)) {
                JsonObject appInfoAsJson = listOfAppPackages.getAsJsonObject(appId);
                String retrievedAppPackage = appInfoAsJson.get("app_package").getAsString();
                LOGGER.info("retrievedAppPackage: " + retrievedAppPackage);
                if(retrievedAppPackage.equals(appPackageName)) {
                    LOGGER.info("\tThis file is available in Device Farm: " + appId);
                    uploadedAppId.set(appId);
                    Setup.addToConfigs(Setup.APP_PATH, appInfoAsJson.get("app_name").getAsString());
                }
            }
        });
    }

}
