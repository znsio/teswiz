package com.znsio.teswiz.runner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverSession {
    private static final Logger LOGGER = LogManager.getLogger(DriverSession.class.getName());
    @JsonProperty("appium:adbExecTimeout")
    @JsonAlias("adbExecTimeout")
    private int adbExecTimeout;

    @JsonProperty("appium:app")
    @JsonAlias("app")
    private String app;

    @JsonProperty("appium:appPackage")
    @JsonAlias("appPackage")
    private String appPackage;

    @JsonProperty("appium:automationName")
    @JsonAlias("automationName")
    private String automationName;

    @JsonProperty("appium:chromeDriverPort")
    @JsonAlias("chromeDriverPort")
    private String chromeDriverPort;

    @JsonProperty("appium:deviceApiLevel")
    @JsonAlias("deviceApiLevel")
    private int deviceApiLevel;

    @JsonProperty("appium:deviceManufacturer")
    @JsonAlias("deviceManufacturer")
    private String deviceManufacturer;

    @JsonProperty("appium:deviceModel")
    @JsonAlias("deviceModel")
    private String deviceModel;

    @JsonProperty("appium:deviceName")
    @JsonAlias("deviceName")
    private String deviceName;

    @JsonProperty("appium:deviceScreenDensity")
    @JsonAlias("deviceScreenDensity")
    private String deviceScreenDensity;

    @JsonProperty("appium:deviceScreenSize")
    @JsonAlias("deviceScreenSize")
    private String deviceScreenSize;

    @JsonProperty("appium:deviceUDID")
    @JsonAlias("deviceUDID")
    private String deviceUDID;

    @JsonProperty("appium:mjpegServerPort")
    @JsonAlias("mjpegServerPort")
    private String mjpegServerPort;

    @JsonProperty("appium:platformVersion")
    @JsonAlias("platformVersion")
    private String platformVersion;

    @JsonProperty("appium:systemPort")
    @JsonAlias("systemPort")
    private String systemPort;

    @JsonProperty("appium:udid")
    @JsonAlias("udid")
    private String udid;

    private String platformName;

    public String getPlatformName() {
        return platformName;
    }

    public String getUdid() {
        return udid;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
