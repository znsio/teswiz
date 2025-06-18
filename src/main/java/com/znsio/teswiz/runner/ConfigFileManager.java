package com.znsio.teswiz.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.znsio.teswiz.tools.OverriddenVariable.getOverriddenStringValue;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * ConfigFileManager - Read config file statically into configFileMap
 */
public enum ConfigFileManager {
    CAPS("./caps/capabilities.json");

    private static final Properties PROPERTIES;
    private static final Logger LOGGER = LogManager.getLogger(ConfigFileManager.class.getName());

    static {
        PROPERTIES = new Properties();
        String configFile = getOverriddenStringValue("CONFIG_FILE", "./configs/config.properties");
        LOGGER.info("Using config file from [{}]", configFile);
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            LOGGER.info("Error while loading config file: {}", e.getMessage());
        }
    }

    private final String defaultValue;

    ConfigFileManager(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String get() {
        return getOverriddenStringValue(name(), PROPERTIES.getProperty(name(), defaultValue));
    }

    public boolean isTrue() {
        return parseBoolean(get());
    }

    public int getInt() {
        return parseInt(get());
    }
}
