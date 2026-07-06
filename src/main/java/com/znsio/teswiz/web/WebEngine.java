package com.znsio.teswiz.web;

import java.util.Arrays;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

public enum WebEngine {
    SELENIUM("selenium"),
    PLAYWRIGHT_TS("playwright-ts");

    private final String configValue;

    WebEngine(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static WebEngine from(String rawValue) {
        return Arrays.stream(values())
                .filter(engine -> engine.configValue.equalsIgnoreCase(rawValue))
                .findFirst()
                .orElseThrow(() -> new InvalidTestDataException(
                        String.format("Unsupported WEB_ENGINE: '%s'. Supported values are: selenium, playwright-ts",
                                rawValue)));
    }
}
