package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.Platform;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserPersonaDetails {
    private final Logger LOGGER = Logger.getLogger(UserPersonaDetails.class.getName());
    private final ConcurrentHashMap<String, Capabilities> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> apps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Driver> drivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Platform> platforms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> deviceLogFileNameForUserPersonaAndPlatform =
            new ConcurrentHashMap<>();

    private String keyPrefix = Thread.currentThread().getId() + "-";

    void assignNewPersonaForUser(String userPersona, String newUserPersona) {
        replaceCapabilitiesFor(userPersona, newUserPersona);
        replaceDriverFor(userPersona, newUserPersona);
        replaceAppNameFor(userPersona, newUserPersona);
        replacePlatformFor(userPersona, newUserPersona);
        replaceLogFileNameFor(userPersona, newUserPersona);
    }

    private void replaceLogFileNameFor(String userPersona, String newUserPersona) {
        Platform currentPlatform = platforms.get(keyForCurrentThread(newUserPersona));
        String currentKey = getKeyNameForDeviceLogs(userPersona, currentPlatform.name());
        if(currentPlatform.equals(Platform.web)) {
            currentKey = getKeyNameForBrowserLogs(userPersona, currentPlatform.name(),
                                                  Drivers.getBrowserNameForUser(newUserPersona));
        }

        String existingLogFileNameforUser = deviceLogFileNameForUserPersonaAndPlatform.get(
                currentKey);

        String newKey = getKeyNameForDeviceLogs(newUserPersona, currentPlatform.name());
        if(currentPlatform.equals(Platform.web)) {
            newKey = getKeyNameForBrowserLogs(newUserPersona, currentPlatform.name(),
                                                  Drivers.getBrowserNameForUser(newUserPersona));
        }
        LOGGER.debug("userPersona: " + userPersona + ", newUserPersona: " + newUserPersona);
        LOGGER.debug("currentPlatform: " + currentPlatform.name());
        LOGGER.debug("existingLogFileNameforUser: " + existingLogFileNameforUser);
        LOGGER.debug("currentKey: " + currentKey);
        LOGGER.debug("newKey: " + newKey);

        LOGGER.debug("deviceLogFileNameForUserPersonaAndPlatform before removing currentKey: " + deviceLogFileNameForUserPersonaAndPlatform);
        deviceLogFileNameForUserPersonaAndPlatform.remove(currentKey);
        LOGGER.debug("deviceLogFileNameForUserPersonaAndPlatform after removing currentKey: " + deviceLogFileNameForUserPersonaAndPlatform);

        deviceLogFileNameForUserPersonaAndPlatform.put(newKey,
                                                       existingLogFileNameforUser);
        LOGGER.debug("deviceLogFileNameForUserPersonaAndPlatform after adding newKey: " + deviceLogFileNameForUserPersonaAndPlatform);
    }

    private void replaceAppNameFor(String userPersona, String newUserPersona) {
        String existingAppNameForUser = apps.get(keyForCurrentThread(userPersona));
        apps.remove(keyForCurrentThread(userPersona));
        apps.put(keyForCurrentThread(newUserPersona), existingAppNameForUser);
    }

    private void replaceDriverFor(String userPersona, String newUserPersona) {
        Driver existingDriverForUser = drivers.get(keyForCurrentThread(userPersona));
        drivers.remove(keyForCurrentThread(userPersona));
        drivers.put(keyForCurrentThread(newUserPersona), existingDriverForUser);
    }

    private void replacePlatformFor(String userPersona, String newUserName) {
        Platform existingPlatformForUser = platforms.get(keyForCurrentThread(userPersona));
        platforms.remove(keyForCurrentThread(userPersona));
        platforms.put(keyForCurrentThread(newUserName), existingPlatformForUser);
    }

    private void replaceCapabilitiesFor(String userPersona, String newUserPersona) {
        Capabilities existingCapabilitiesForUser = capabilities.get(
                keyForCurrentThread(userPersona));
        capabilities.remove(keyForCurrentThread(userPersona));
        capabilities.put(keyForCurrentThread(newUserPersona), existingCapabilitiesForUser);
    }

    void addAppName(String userPersona, String appName) {
        apps.put(keyForCurrentThread(userPersona), appName);
    }

    private String keyForCurrentThread(String key) {
        if(!key.startsWith(keyPrefix)) {
            key = keyPrefix + key;
        }
        return key;
    }

    String getAppName(String userPersona) {
        return apps.get(keyForCurrentThread(userPersona));
    }

    void clearAllAppNames() {
        clearMap(" - clearAllAppNames - before: ", apps, " - clearAllAppNames - after: ");
    }

    private void clearMap(String beforeClearMessagePrefix, ConcurrentHashMap<String, String> map,
                          String afterClearMessagePrefix) {
        LOGGER.debug(Thread.currentThread().getId() + beforeClearMessagePrefix + map.keySet());

        for(Map.Entry<String, String> item : map.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                map.remove(item.getKey());
            }
        }
        LOGGER.debug(Thread.currentThread().getId() + afterClearMessagePrefix + map.keySet());
    }

    void addDriver(String userPersona, Driver driver) {
        drivers.put(keyForCurrentThread(userPersona), driver);
    }

    Driver getDriverAssignedForUser(String userPersona) {
        return drivers.get(keyForCurrentThread(userPersona));
    }

    boolean isDriverAssignedForUser(String userPersona) {
        return drivers.containsKey(keyForCurrentThread(userPersona));
    }

    void clearAllDrivers() {
        LOGGER.debug(Thread.currentThread()
                          .getId() + " - clearAllDrivers - before: " + drivers.keySet());

        for(Map.Entry<String, Driver> item : drivers.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                drivers.remove(item.getKey());
            }
        }
        LOGGER.debug(
                Thread.currentThread().getId() + " - clearAllDrivers - after: " + drivers.keySet());
    }

    Set<String> getAllUserPersonasForAssignedDrivers() {
        return drivers.keySet();
    }

    void addPlatform(String userPersona, Platform platform) {
        platforms.put(keyForCurrentThread(userPersona), platform);
    }

    Set<String> getAllUserPersonasForAssignedPlatforms() {
        return platforms.keySet();
    }

    Platform getPlatformAssignedForUser(String userPersona) {
        return platforms.get(keyForCurrentThread(userPersona));
    }

    void clearAllPlatforms() {
        LOGGER.debug(Thread.currentThread()
                          .getId() + " - clearAllPlatforms - before: " + platforms.keySet());

        for(Map.Entry<String, Platform> item : platforms.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                platforms.remove(item.getKey());
            }
        }
        LOGGER.debug(Thread.currentThread()
                          .getId() + " - clearAllPlatforms - after: " + platforms.keySet());
    }

    Capabilities getCapabilitiesAssignedForUser(String userPersona) {
        return capabilities.get(keyForCurrentThread(userPersona));
    }

    void clearAllCapabilities() {
        LOGGER.debug(Thread.currentThread()
                          .getId() + " - clearAllCapabilities - before: " + capabilities.keySet());

        for(Map.Entry<String, Capabilities> item : capabilities.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                capabilities.remove(item.getKey());
            }
        }
        LOGGER.debug(Thread.currentThread()
                          .getId() + " - clearAllCapabilities - after: " + capabilities.keySet());
    }

    void addCapabilities(String userPersona, Capabilities capabilities) {
        this.capabilities.put(keyForCurrentThread(userPersona), capabilities);
    }

    void clearLogFileNames() {
        clearMap(" - clearLogFileNames - before: ", deviceLogFileNameForUserPersonaAndPlatform,
                 " - clearLogFileNames - after: ");
    }

    String getDeviceLogFileNameFor(String userPersona, String platform) {
        return deviceLogFileNameForUserPersonaAndPlatform.get(
                getKeyNameForDeviceLogs(userPersona, platform));
    }

    String getBrowserLogFileNameFor(String userPersona, String platform, String browserType) {
        return deviceLogFileNameForUserPersonaAndPlatform.get(
                getKeyNameForBrowserLogs(userPersona, platform, browserType));
    }

    void addDeviceLogFileNameFor(String userPersona, String platform, String deviceLogFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                getKeyNameForDeviceLogs(userPersona, platform), deviceLogFileName);
    }

    @NotNull
    private String getKeyNameForDeviceLogs(String userPersona, String platform) {
        return keyForCurrentThread(userPersona) + "-" + platform;
    }

    void addBrowserLogFileNameFor(String userPersona, String forplatform, String browserType,
                                  String logFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                getKeyNameForBrowserLogs(userPersona, forplatform, browserType), logFileName);
    }

    @NotNull
    private String getKeyNameForBrowserLogs(String userPersona, String forplatform,
                                            String browserType) {
        return keyForCurrentThread(userPersona) + "-" + forplatform + "-" + browserType;
    }

    Map<String, Driver> getAllAssignedUserPersonasAndDrivers() {
        return drivers;
    }

}
