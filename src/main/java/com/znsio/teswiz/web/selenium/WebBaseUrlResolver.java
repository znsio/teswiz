package com.znsio.teswiz.web.selenium;

import static com.znsio.teswiz.tools.OverriddenVariable.getOverriddenStringValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.runner.Runner;

public final class WebBaseUrlResolver {
    private static final Logger LOGGER = LogManager.getLogger(WebBaseUrlResolver.class.getName());

    private WebBaseUrlResolver() {
    }

    public static String resolve(String appName) {
        String providedBaseUrlKey = Runner.getBaseURLForWeb();
        if (!appName.equalsIgnoreCase(Runner.DEFAULT)) {
            providedBaseUrlKey = appName.toUpperCase() + "_BASE_URL";
        }
        LOGGER.info(String.format("Using BASE_URL key: %s", providedBaseUrlKey));

        if (null == providedBaseUrlKey) {
            throw new IllegalStateException("baseUrl key not provided");
        }
        String retrievedBaseUrl = String.valueOf(Runner.getFromEnvironmentConfiguration(providedBaseUrlKey));
        retrievedBaseUrl = getOverriddenStringValue(providedBaseUrlKey, retrievedBaseUrl);
        LOGGER.info(String.format("baseUrl: %s", retrievedBaseUrl));
        return retrievedBaseUrl;
    }
}
