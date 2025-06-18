package com.znsio.teswiz.runner.atd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Runner;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.windows.WindowsDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import static com.znsio.teswiz.runner.atd.ConfigFileManager.CAPS;

public class ATD_AppiumDriverManager {
    private static final ThreadLocal<AppiumDriver> appiumDriver = new ThreadLocal<>();
    private static final Logger LOGGER = LogManager.getLogger(ATD_AppiumDriverManager.class.getName());

    public static AppiumDriver getDriver() {
        return appiumDriver.get();
    }

    protected static void setDriver(AppiumDriver driver) {
        String allCapabilities = driver.getCapabilities().getCapabilityNames().stream()
                .map(key -> String.format("%n\t%s:: %s", key,
                                          driver.getCapabilities().getCapability(key)))
                .collect(Collectors.joining(""));
        LOGGER.info(String.format("AppiumDriverManager: Created AppiumDriver with capabilities: %s",
                                  allCapabilities));
        appiumDriver.set(driver);
    }

    private AppiumDriver initialiseDriver(DesiredCapabilities desiredCapabilities) {
        String allCapabilities = desiredCapabilities.getCapabilityNames().stream()
                .map(key -> String.format("%n\t%s:: %s", key,
                                          desiredCapabilities.getCapability(key)))
                .collect(Collectors.joining(""));

        LOGGER.info(String.format("Initialise Driver with Capabilities: %s",
                                  allCapabilities));
        ATD_AppiumServerManager appiumServerManager = new ATD_AppiumServerManager();
        String remoteWDHubIP = appiumServerManager.getRemoteWDHubIP();
        return createAppiumDriver(desiredCapabilities, remoteWDHubIP);
    }

    private AppiumDriver createAppiumDriver(DesiredCapabilities desiredCapabilities,
            String remoteWDHubIP) {
        AppiumDriver currentDriverSession;
        Platform platform = Runner.getPlatform();
        URL remoteUrl = null;
        try {
            remoteUrl = new URL(remoteWDHubIP);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        currentDriverSession = switch (platform) {
            case android -> new AndroidDriver(remoteUrl, desiredCapabilities);
            case iOS -> new IOSDriver(remoteUrl, desiredCapabilities);
            case windows -> new WindowsDriver(remoteUrl, desiredCapabilities);
            default -> throw new IllegalStateException("Unexpected value: " + platform.name());
        };
        Capabilities currentDriverSessionCapabilities = currentDriverSession.getCapabilities();
        LOGGER.info("Session Created for "
                    + platform.name()
                    + "\n\tSession Id: " + currentDriverSession.getSessionId()
                    + "\n\tUDID: " + currentDriverSessionCapabilities.getCapability("udid"));
        String json = new Gson().toJson(currentDriverSessionCapabilities.asMap());
        DriverSession driverSessions = null;
        try {
            driverSessions = (new ObjectMapper().readValue(json, DriverSession.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ATD_AppiumDeviceManager.setDevice(driverSessions);
        return currentDriverSession;
    }

    public AppiumDriver startAppiumDriverInstance(String testMethodName) {
        return startAppiumDriverInstance(testMethodName, buildDesiredCapabilities(CAPS.get()));
    }

    public AppiumDriver startAppiumDriverInstance(String testMethodName,
            String capabilityFilePath) {
        return startAppiumDriverInstance(testMethodName,
                                         buildDesiredCapabilities(capabilityFilePath));
    }

    public AppiumDriver startAppiumDriverInstance(String testMethodName,
            DesiredCapabilities desiredCapabilities) {
        LOGGER.info(String.format("startAppiumDriverInstance for %s using capability file: %s",
                                  testMethodName, CAPS.get()));
        LOGGER.info("startAppiumDriverInstance");
        AppiumDriver currentDriverSession =
                initialiseDriver(desiredCapabilities);
        ATD_AppiumDriverManager.setDriver(currentDriverSession);
        return currentDriverSession;
    }

    //    public void startAppiumDriverInstanceWithUDID(String testMethodName,
    //            String deviceUDID) {
    //        LOGGER.info(String.format("startAppiumDriverInstance for %s using capability file: %s",
    //                                  testMethodName, CAPS.get()));
    //        LOGGER.info("startAppiumDriverInstance");
    //        DesiredCapabilities desiredCapabilities = buildDesiredCapabilities(CAPS.get());
    //        desiredCapabilities.setCapability("appium:udids", deviceUDID);
    //        AppiumDriver currentDriverSession =
    //                initialiseDriver(desiredCapabilities);
    //        ATD_AppiumDriverManager.setDriver(currentDriverSession);
    //    }
    //
    private DesiredCapabilities buildDesiredCapabilities(String capabilityFilePath) {
        if (new File(capabilityFilePath).exists()) {
            return new DesiredCapabilityBuilder()
                    .buildDesiredCapability(capabilityFilePath);
        } else {
            throw new RuntimeException("Capability file not found");
        }
    }

    public void stopAppiumDriver() {
        if (ATD_AppiumDriverManager.getDriver() != null
            && ATD_AppiumDriverManager.getDriver().getSessionId() != null) {
            LOGGER.info("Session Deleting ---- "
                        + ATD_AppiumDriverManager.getDriver().getSessionId() + "---"
                        + ATD_AppiumDriverManager.getDriver().getCapabilities().getCapability("udid"));
            ATD_AppiumDriverManager.getDriver().quit();
        }
    }
}
