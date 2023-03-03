package com.znsio.teswiz.runner;

import com.fasterxml.jackson.annotation.JsonProperty;

class BrowserStackDevice {

    private String name;
    private String device;
    private String os;
    private String os_version;
    private String browser;
    @JsonProperty("browser_version")
    private String browser_version;
    @JsonProperty("real_mobile")
    private boolean real_mobile;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getDevice() {
        return device;
    }

    void setDevice(String device) {
        this.device = device;
    }

    String getOs() {
        return os;
    }

    void setOs(String os) {
        this.os = os;
    }

    String getOs_version() {
        return os_version;
    }

    void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    String getBrowser() {
        return browser;
    }

    void setBrowser(String browser) {
        this.browser = browser;
    }

    String getBrowserVersion() {
        return browser_version;
    }

    void setBrowserVersion(String browser_version) {
        this.browser_version = browser_version;
    }

    boolean isRealMobile() {
        return real_mobile;
    }

    void setRealMobile(boolean real_mobile) {
        this.real_mobile = real_mobile;
    }
}
