package com.znsio.e2e.runner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.znsio.e2e.exceptions.EnvironmentSetupException;
import com.znsio.e2e.tools.JsonFile;
import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.znsio.e2e.runner.DeviceSetup.saveNewCapabilitiesFile;
import static com.znsio.e2e.runner.Runner.*;
import static com.znsio.e2e.runner.Setup.*;

public class PCloudySetup {
    private static final Logger LOGGER = Logger.getLogger(PCloudySetup.class.getName());

    static void updatePCloudyCapabilities() {
        String emailID = configs.get(CLOUD_USER);
        String authenticationKey = configs.get(CLOUD_KEY);
        if(configsBoolean.get(CLOUD_UPLOAD_APP)) {
            uploadAPKTopCloudy(emailID, authenticationKey);
        } else {
            LOGGER.info("Skip uploading the apk to Device Farm");
        }
        String capabilityFile = configs.get(CAPS);
        String appPath = configs.get(APP_PATH);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        String platformName = platform.name();
        Map loadedPlatformCapability = loadedCapabilityFile.get(platformName);
        String osVersion = String.valueOf(loadedPlatformCapability.get("platformVersion"));
        loadedPlatformCapability.remove("app");
        loadedPlatformCapability.put("pCloudy_Username", emailID);
        loadedPlatformCapability.put("pCloudy_ApiKey", authenticationKey);
        loadedPlatformCapability.put("pCloudy_ApplicationName", getAppName(appPath));
        loadedPlatformCapability.put("pCloudy_DeviceVersion", osVersion);

        updateCapabilities(loadedCapabilityFile);
    }

    private static void uploadAPKTopCloudy(String emailID, String authenticationKey) {
        LOGGER.info(String.format("uploadAPKTopCloudy for: '%s':'%s'%n", emailID, authenticationKey));
        String appPath = configs.get(APP_PATH);
        String deviceLabURL = configs.get(DEVICE_LAB_URL);

        String authToken = getpCloudyAuthToken(emailID, authenticationKey, appPath, deviceLabURL);
        if(isAPKAlreadyAvailableInPCloudy(authToken, appPath)) {
            LOGGER.info("\tAPK is already available in cloud. No need to upload it again");
        } else {
            LOGGER.info("\tAPK is NOT available in cloud. Upload it");
            configs.put(APP_PATH, uploadAPKToPCloudy(appPath, deviceLabURL, authToken));
        }
    }

    private static String getAppName(String appPath) {
        return new File(appPath).getName();
    }

    static void updateCapabilities(Map<String, Map> loadedCapabilityFile) {
        String capabilityFile = configs.get(CAPS);
        String platformName = platform.name();
        ArrayList listOfAndroidDevices = new ArrayList();
        for(int numDevices = 0; numDevices < configsInteger.get(MAX_NUMBER_OF_APPIUM_DRIVERS); numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap();
            deviceInfo.put("osVersion", String.valueOf(loadedCapabilityFile.get(platformName)
                                                                           .get("platformVersion")));
            deviceInfo.put("deviceName", String.valueOf(loadedCapabilityFile.get(platformName)
                                                                            .get("platformName")));
            listOfAndroidDevices.add(deviceInfo);
        }
        saveNewCapabilitiesFile(platformName, capabilityFile, loadedCapabilityFile, listOfAndroidDevices);
    }

    private static String getpCloudyAuthToken(String emailID, String authenticationKey, String appPath, String deviceLabURL) {
        LOGGER.info("Get pCloudy Auth Token");
        String[] getAppToken = new String[]{"curl --insecure", "-u", "\"" + emailID + ":" + authenticationKey + "\"", deviceLabURL + "/api/access"};
        CommandLineResponse authTokenResponse = CommandLineExecutor.execCommand(getAppToken);
        LOGGER.info("\tauthTokenResponse: " + authTokenResponse.getStdOut());
        if(authTokenResponse.getStdOut()
                            .contains("error")) {
            throw new EnvironmentSetupException(String.format("Unable to get auth: '%s' to '%s'%n%s", appPath, deviceLabURL, authTokenResponse));
        }
        String authToken = JsonFile.convertToMap(authTokenResponse.getStdOut())
                                   .getAsJsonObject("result")
                                   .get("token")
                                   .getAsString();
        LOGGER.info("\tauthToken: " + authToken);
        return authToken;
    }

    private static boolean isAPKAlreadyAvailableInPCloudy(String authToken, String appPath) {
        Path path = Paths.get(appPath);
        String appNameFromPath = path.getFileName()
                                     .toString();
        LOGGER.info("isAPKAlreadyAvailableInCloud: Start: " + appPath);

        CommandLineResponse uploadResponse = getListOfUploadedFilesInPCloudy(authToken);
        JsonObject result = JsonFile.convertToMap(uploadResponse.getStdOut())
                                    .getAsJsonObject("result");
        JsonArray availableFiles = result.getAsJsonArray("files");
        AtomicBoolean isFileAlreadyUploaded = new AtomicBoolean(false);
        availableFiles.forEach(file -> {
            String fileName = ((JsonObject) file).get("file")
                                                 .getAsString();
            LOGGER.info("\tThis file is available in Device Farm: " + fileName);
            if(appNameFromPath.equals(fileName)) {
                isFileAlreadyUploaded.set(true);
            }
        });
        return isFileAlreadyUploaded.get();
    }

    private static String uploadAPKToPCloudy(String appPath, String deviceLabURL, String authToken) {
        LOGGER.info("uploadAPKTopCloudy: " + appPath);
        String[] listOfDevices = new String[]{"curl --insecure", "-X", "POST", "-F", "file=@\"" + appPath + "\"", "-F", "\"source_type=raw\"", "-F", "\"token=" + authToken + "\"",
                                              "-F", "\"filter=apk\"", deviceLabURL + "/api/upload_file"};

        CommandLineResponse uploadApkResponse = CommandLineExecutor.execCommand(listOfDevices);
        LOGGER.info("\tuploadApkResponse: " + uploadApkResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(uploadApkResponse.getStdOut())
                                    .getAsJsonObject("result");
        int uploadStatus = result.get("code")
                                 .getAsInt();
        if(200 != uploadStatus) {
            throw new EnvironmentSetupException(String.format("Unable to upload app: '%s' to '%s'%n%s", appPath, deviceLabURL, uploadApkResponse));
        }
        String uploadedFileName = result.get("file")
                                        .getAsString();
        LOGGER.info("\tuploadAPKToPCloudy: Uploaded: " + uploadedFileName);
        return uploadedFileName;
    }

    @NotNull
    private static CommandLineResponse getListOfUploadedFilesInPCloudy(String authToken) {
        String deviceLabURL = configs.get(DEVICE_LAB_URL);
        Map payload = new HashMap();
        payload.put("\"token\"", "\"" + authToken + "\"");
        payload.put("\"limit\"", 15);
        payload.put("\"filter\"", "\"all\"");
        String updatedPayload = payload.toString()
                                       .replace("\"", "\\\"")
                                       .replaceAll("=", ":");

        String[] listOfUploadedFiles;
        listOfUploadedFiles = new String[]{"curl --insecure", "-H", "Content-Type:application/json", "-d", "\"" + updatedPayload + "\"", deviceLabURL + "/api/drive"};

        CommandLineResponse listFilesInPCloudyResponse = CommandLineExecutor.execCommand(listOfUploadedFiles);
        LOGGER.info("\tlistFilesInPCloudyResponse: " + listFilesInPCloudyResponse.getStdOut());
        JsonObject result = JsonFile.convertToMap(listFilesInPCloudyResponse.getStdOut())
                                    .getAsJsonObject("result");
        JsonElement resultCode = result.get("code");
        int uploadStatus = (null == resultCode) ? 400 : resultCode.getAsInt();
        if(200 != uploadStatus) {
            throw new EnvironmentSetupException(String.format("Unable to get list of uploaded files%n%s", listFilesInPCloudyResponse));
        }

        return listFilesInPCloudyResponse;
    }

}
