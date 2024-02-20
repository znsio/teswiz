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

    String getDevice() {
        return device;
    }

    String getOs() {
        return os;
    }

    String getOs_version() {
        return os_version;
    }

    String getBrowser() {
        return browser;
    }

    String getBrowserVersion() {
        return browser_version;
    }

    boolean isRealMobile() {
        return real_mobile;
    }
}
