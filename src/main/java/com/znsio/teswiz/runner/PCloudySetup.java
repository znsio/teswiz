package com.znsio.teswiz.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.JsonFile;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.znsio.teswiz.runner.Setup.*;

class PCloudySetup {
    private static final Logger LOGGER = LogManager.getLogger(PCloudySetup.class.getName());
    private static final String CURL_INSECURE = "curl --insecure";
    private static final String RESULT = "result";

    private PCloudySetup() {
        LOGGER.debug("PCloudySetup - private constructor");
    }

    static void updatePCloudyCapabilities(String deviceLabURL) {
        String emailID = Setup.getFromConfigs(CLOUD_USERNAME);
        String authenticationKey = Setup.getFromConfigs(CLOUD_KEY);
        if(Setup.getBooleanValueFromConfigs(CLOUD_UPLOAD_APP)) {
            fetchAuthTokenAndUploadAPKToPCloudy(emailID, authenticationKey, deviceLabURL);
        } else {
            LOGGER.info("Skip uploading the apk to Device Farm");
        }
        String capabilityFile = Setup.getFromConfigs(CAPS);
        String appPath = Setup.getFromConfigs(APP_PATH);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        String platformName = Setup.getPlatform().name();
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        String deviceVersion = String.valueOf(loadedPlatformCapability.get("os_version"));
        String deviceManufacturer = String.valueOf(loadedPlatformCapability.get("device"));
        String appiumVersion;
        if (loadedPlatformCapability.containsKey("appiumVersion")) {
            appiumVersion = String.valueOf(loadedPlatformCapability.get("appiumVersion"));
            loadedPlatformCapability.remove("appiumVersion");
        } else {
            appiumVersion = String.valueOf(loadedPlatformCapability.get("appium:appiumVersion"));
            loadedPlatformCapability.remove("appium:appiumVersion");
        }
        loadedPlatformCapability.remove("app");
        loadedPlatformCapability.put("platformVersion", loadedPlatformCapability.get("os_version"));
        Map pCloudyOptions = (Map) loadedPlatformCapability.get("pcloudy:options");
        pCloudyOptions.put("pCloudy_Username", emailID);
        pCloudyOptions.put("pCloudy_ApiKey", authenticationKey);
        pCloudyOptions.put("pCloudy_ApplicationName", getAppName(appPath));
        pCloudyOptions.put("pCloudy_DeviceVersion", deviceVersion);
        pCloudyOptions.put("pCloudy_DeviceManufacturer", deviceManufacturer.toUpperCase());
        pCloudyOptions.put("appiumVersion", appiumVersion);
        updateCapabilities(loadedCapabilityFile);
    }

    private static void fetchAuthTokenAndUploadAPKToPCloudy(String emailID,
                                                            String authenticationKey,
                                                            String deviceLabURL) {
        LOGGER.info(
                String.format("uploadAPKTopCloudy for: '%s':'%s'%n", emailID, authenticationKey));
        String appPath = Setup.getFromConfigs(APP_PATH);

        String authToken = getPCloudyAuthToken(emailID, authenticationKey, appPath, deviceLabURL);
        if(isAPKAlreadyAvailableInPCloudy(authToken, appPath, deviceLabURL)) {
            LOGGER.info("\tAPK is already available in cloud. No need to upload it again");
        } else {
            LOGGER.info("\tAPK is NOT available in cloud. Upload it");
            Setup.addToConfigs(APP_PATH, uploadAppToPCloudy(appPath, deviceLabURL, authToken));
        }
    }

    private static String getAppName(String appPath) {
        return new File(appPath).getName();
    }

    static void updateCapabilities(Map<String, Map> loadedCapabilityFile) {
        String capabilityFile = Setup.getFromConfigs(CAPS);
        String platformName = Setup.getPlatform().name();
        ArrayList listOfAndroidDevices = new ArrayList();
        for(int numDevices = 0;
            numDevices < Setup.getIntegerValueFromConfigs(MAX_NUMBER_OF_APPIUM_DRIVERS);
            numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("pCloudy_DeviceManufacturer",
                    loadedCapabilityFile.get(platformName).get("device").toString().toUpperCase());
            deviceInfo.put("pCloudy_DeviceVersion", String.valueOf(
                    loadedCapabilityFile.get(platformName).get("os_version")));
            deviceInfo.put("platform",
                    loadedCapabilityFile.get(platformName).get("platformName").toString().toLowerCase());
            listOfAndroidDevices.add(deviceInfo);
        }
        DeviceSetup.saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile,
                                            listOfAndroidDevices);
    }

    private static String getPCloudyAuthToken(String emailID, String authenticationKey,
                                              String appPath, String deviceLabURL) {
        LOGGER.info("Get pCloudy Auth Token");
        String[] getAppToken = new String[]{CURL_INSECURE, getCurlProxyCommand(), "-u",
                                            "\"" + emailID + ":" + authenticationKey + "\"",
                                            deviceLabURL + "/api/access"};
        CommandLineResponse authTokenResponse = CommandLineExecutor.execCommand(getAppToken);
        LOGGER.info("\tauthTokenResponse: " + authTokenResponse.getStdOut());
        if(authTokenResponse.getStdOut().contains("error")) {
            throw new EnvironmentSetupException(
                    String.format("Unable to get auth: '%s' to '%s'%n%s", appPath, deviceLabURL,
                                  authTokenResponse));
        }
        String authToken = JsonFile.convertToMap(authTokenResponse.getStdOut())
                                   .getAsJsonObject(RESULT).get("token").getAsString();
        LOGGER.info("\tauthToken: " + authToken);
        return authToken;
    }

    private static boolean isAPKAlreadyAvailableInPCloudy(String authToken, String appPath,
                                                          String deviceLabURL) {
        Path path = Paths.get(appPath);
        String appNameFromPath = path.getFileName().toString();
        LOGGER.info("isAPKAlreadyAvailableInCloud: Start: " + appPath);

        CommandLineResponse uploadResponse = getListOfUploadedFilesInPCloudy(authToken,
                deviceLabURL);
        JsonObject result = JsonFile.convertToMap(uploadResponse.getStdOut())
                                    .getAsJsonObject(RESULT);
        JsonArray availableFiles = result.getAsJsonArray("files");
        AtomicBoolean isFileAlreadyUploaded = new AtomicBoolean(false);
        availableFiles.forEach(file -> {
            String fileName = ((JsonObject) file).get("file").getAsString();
            LOGGER.info("\tThis file is available in Device Farm: " + fileName);
            if(appNameFromPath.equals(fileName)) {
                isFileAlreadyUploaded.set(true);
            }
        });
        return isFileAlreadyUploaded.get();
    }

    private static String uploadAppToPCloudy(String appPath, String deviceLabURL,
                                             String authToken) {
        LOGGER.info("uploadAPKTopCloudy: " + appPath);
        StringBuilder apptype = getAppType();
        String[] listOfDevices = new String[]{CURL_INSECURE, getCurlProxyCommand(), "-X", "POST",
                "-F", "file=@\"" + appPath + "\"", "-F",
                "\"source_type=raw\"", "-F",
                "\"token=" + authToken + "\"", "-F", "\"filter=" + apptype + "\"",
                deviceLabURL + "/api/upload_file"};

        CommandLineResponse uploadApkResponse = CommandLineExecutor.execCommand(listOfDevices);
        LOGGER.info("\tuploadApkResponse: " + uploadApkResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(uploadApkResponse.getStdOut()).getAsJsonObject(RESULT);
        int uploadStatus = result.get("code").getAsInt();
        if(200 != uploadStatus) {
            throw new EnvironmentSetupException(String.format("Unable to upload app to pCloudy: '%s' to '%s'%n%s", appPath, deviceLabURL, uploadApkResponse));
        }
        String uploadedFileName = result.get("file").getAsString();
        LOGGER.info("\tuploadAppToPCloudy: Uploaded: " + uploadedFileName);
        return uploadedFileName;
    }

    @NotNull
    private static StringBuilder getAppType() {
        StringBuilder apptype= new StringBuilder();
        if(Runner.getPlatform().equals(Platform.android)) {
            apptype = new StringBuilder("apk");
        }
        else if (Runner.getPlatform().equals(Platform.iOS)) {
            apptype= new StringBuilder("ipa");
        }
        return apptype;
    }

    @NotNull
    private static CommandLineResponse getListOfUploadedFilesInPCloudy(String authToken, String deviceLabURL) {
        String updatedPayload = createPayloadToGetListOfUploadedFiles(authToken);

        String[] listOfUploadedFiles;
        listOfUploadedFiles = new String[]{CURL_INSECURE, getCurlProxyCommand(), "-H",
                                           "Content-Type:application/json", "-d",
                                           "\"" + updatedPayload + "\"",
                                           deviceLabURL + "/api/drive"};

        CommandLineResponse listFilesInPCloudyResponse = CommandLineExecutor.execCommand(
                listOfUploadedFiles);
        LOGGER.info("\tlistFilesInPCloudyResponse: " + listFilesInPCloudyResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(listFilesInPCloudyResponse.getStdOut())
                                    .getAsJsonObject(RESULT);
        JsonElement resultCode = result.get("code");
        int uploadStatus = (null == resultCode) ? 400 : resultCode.getAsInt();
        if(200 != uploadStatus) {
            throw new EnvironmentSetupException(
                    String.format("Unable to get list of uploaded files%n%s",
                                  listFilesInPCloudyResponse));
        }

        return listFilesInPCloudyResponse;
    }

    @NotNull
    private static String createPayloadToGetListOfUploadedFiles(String authToken) {
        Map payload = new HashMap();
        payload.put("\"token\"", "\"" + authToken + "\"");
        payload.put("\"limit\"", 15);
        payload.put("\"filter\"", "\"all\"");
        String updatedPayload = payload.toString().replace("\"", "\\\"").replace("=", ":");
        return updatedPayload;
    }

}
