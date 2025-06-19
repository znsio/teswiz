package com.znsio.teswiz.runner;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginClI {
    private static final Logger LOGGER = LogManager.getLogger(PluginClI.class.getName());
    private static PluginClI instance;
    private Plugin plugin;

    public static PluginClI getInstance() {
        if (instance == null) {
            PluginCliRequest plugin = new PluginCliRequest();
            instance = plugin.getCliArgs();
        }
        return instance;
    }

    public String getPlatFormName() {
        return getPlugin().getDeviceFarm().getPlatform();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isCloudExecution() {
        return getPlugin().getDeviceFarm().getCloud() != null;
    }

    public String getCloudName() {
        return PluginClI.getInstance().getPlugin().getDeviceFarm()
                .getCloud().get("cloudName").textValue();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeviceFarm {
        private String platform;
        private String androidDeviceType;
        private String iosDeviceType;
        private JsonNode cloud;
        private boolean skipChromeDownload;
        private JsonNode derivedDataPath;

        public String getPlatform() {
            return platform;
        }

        public JsonNode getCloud() {
            return cloud;
        }
    }

    public static class Plugin {
        @JsonProperty("device-farm")
        @JsonAlias("deviceFarm")
        private DeviceFarm deviceFarm;

        public DeviceFarm getDeviceFarm() {
            return deviceFarm;
        }
    }
}
