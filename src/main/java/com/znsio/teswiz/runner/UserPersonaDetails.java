package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.Platform;
import org.apache.log4j.Logger;
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
        LOGGER.info(Thread.currentThread().getId() + beforeClearMessagePrefix + map.keySet());

        for(Map.Entry<String, String> item : map.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                map.remove(item.getKey());
            }
        }
        LOGGER.info(Thread.currentThread().getId() + afterClearMessagePrefix + map.keySet());
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
        LOGGER.info(Thread.currentThread()
                          .getId() + " - clearAllDrivers - before: " + drivers.keySet());

        for(Map.Entry<String, Driver> item : drivers.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                drivers.remove(item.getKey());
            }
        }
        LOGGER.info(
                Thread.currentThread().getId() + " - clearAllDrivers - after: " + drivers.keySet());
    }

    Set<String> getAllUserPersonasForAssignedDrivers() {
        return drivers.keySet();
    }

    void replaceDriverFor(String userPersona, Driver newDriver) {
        //        userPersonaDrivers.remove(userPersona);
        drivers.put(keyForCurrentThread(userPersona), newDriver);
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
        LOGGER.info(Thread.currentThread()
                          .getId() + " - clearAllPlatforms - before: " + platforms.keySet());

        for(Map.Entry<String, Platform> item : platforms.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                platforms.remove(item.getKey());
            }
        }
        LOGGER.info(Thread.currentThread()
                          .getId() + " - clearAllPlatforms - after: " + platforms.keySet());
    }

    void replacePlatformFor(String userPersona, Platform newPlatform) {
        platforms.put(keyForCurrentThread(userPersona), newPlatform);
    }

    Capabilities getCapabilitiesAssignedForUser(String userPersona) {
        return capabilities.get(keyForCurrentThread(userPersona));
    }

    void clearAllCapabilities() {
        LOGGER.info(Thread.currentThread()
                          .getId() + " - clearAllCapabilities - before: " + capabilities.keySet());

        for(Map.Entry<String, Capabilities> item : capabilities.entrySet()) {
            if(item.getKey() != null && item.getKey().startsWith(keyPrefix)) {
                capabilities.remove(item.getKey());
            }
        }
        LOGGER.info(Thread.currentThread()
                          .getId() + " - clearAllCapabilities - after: " + capabilities.keySet());
    }

    void replaceCapabilitiesFor(String userPersona, Capabilities newCapabilities) {
        capabilities.put(keyForCurrentThread(userPersona), newCapabilities);
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
                keyForCurrentThread(userPersona) + "-" + platform);
    }

    String getBrowserLogFileNameFor(String userPersona, String platform,
                                           String browserType) {
        return deviceLogFileNameForUserPersonaAndPlatform.get(
                keyForCurrentThread(userPersona) + "-" + (platform + "-" + browserType));
    }

    void addDeviceLogFileNameFor(String userPersona, String platform,
                                        String deviceLogFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                keyForCurrentThread(userPersona) + "-" + platform, deviceLogFileName);
    }

    void addBrowserLogFileNameFor(String userPersona, String forplatform, String browserType,
                                         String logFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                keyForCurrentThread(userPersona) + "-" + (forplatform + "-" + browserType),
                logFileName);
    }

    Map<String, Driver> getAllAssignedUserPersonasAndDrivers() {
        return drivers;
    }
}
