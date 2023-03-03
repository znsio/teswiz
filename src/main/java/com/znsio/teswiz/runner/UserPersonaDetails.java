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

    public void clearAllAppNames() {
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

    public void clearAllDrivers() {
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

    public Set<String> getAllUserPersonasForAssignedDrivers() {
        return drivers.keySet();
    }

    public void replaceDriverFor(String userPersona, Driver newDriver) {
        //        userPersonaDrivers.remove(userPersona);
        drivers.put(keyForCurrentThread(userPersona), newDriver);
    }

    public void addPlatform(String userPersona, Platform platform) {
        platforms.put(keyForCurrentThread(userPersona), platform);
    }

    public Set<String> getAllUserPersonasForAssignedPlatforms() {
        return platforms.keySet();
    }

    public Platform getPlatformAssignedForUser(String userPersona) {
        return platforms.get(keyForCurrentThread(userPersona));
    }

    public void clearAllPlatforms() {
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

    public void replacePlatformFor(String userPersona, Platform newPlatform) {
        platforms.put(keyForCurrentThread(userPersona), newPlatform);
    }

    public Capabilities getCapabilitiesAssignedForUser(String userPersona) {
        return capabilities.get(keyForCurrentThread(userPersona));
    }

    public void clearAllCapabilities() {
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

    public void replaceCapabilitiesFor(String userPersona, Capabilities newCapabilities) {
        capabilities.put(keyForCurrentThread(userPersona), newCapabilities);
    }

    public void addCapabilities(String userPersona, Capabilities capabilities) {
        this.capabilities.put(keyForCurrentThread(userPersona), capabilities);
    }

    public void clearLogFileNames() {
        clearMap(" - clearLogFileNames - before: ", deviceLogFileNameForUserPersonaAndPlatform,
                 " - clearLogFileNames - after: ");
    }

    public String getDeviceLogFileNameFor(String userPersona, String platform) {
        return deviceLogFileNameForUserPersonaAndPlatform.get(
                keyForCurrentThread(userPersona) + "-" + platform);
    }

    public String getBrowserLogFileNameFor(String userPersona, String platform,
                                           String browserType) {
        return deviceLogFileNameForUserPersonaAndPlatform.get(
                keyForCurrentThread(userPersona) + "-" + (platform + "-" + browserType));
    }

    public void addDeviceLogFileNameFor(String userPersona, String platform,
                                        String deviceLogFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                keyForCurrentThread(userPersona) + "-" + platform, deviceLogFileName);
    }

    public void addBrowserLogFileNameFor(String userPersona, String forplatform, String browserType,
                                         String logFileName) {
        deviceLogFileNameForUserPersonaAndPlatform.put(
                keyForCurrentThread(userPersona) + "-" + (forplatform + "-" + browserType),
                logFileName);
    }

    public Map<String, Driver> getAllAssignedUserPersonasAndDrivers() {
        return drivers;
    }
}
