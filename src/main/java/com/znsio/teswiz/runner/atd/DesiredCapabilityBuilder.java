package com.znsio.teswiz.runner.atd;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;

import static com.znsio.teswiz.runner.atd.ConfigFileManager.CAPS;

public class DesiredCapabilityBuilder {

    private static final Logger LOGGER = LogManager.getLogger(DesiredCapabilityBuilder.class.getName());

    // W3C standard capability keys that should NOT be prefixed with "appium:"
    private static final Set<String> W3C_STANDARD_KEYS = Set.of(
            "platformName", "browserName", "acceptInsecureCerts", "pageLoadStrategy",
            "proxy", "timeouts", "unhandledPromptBehavior"
    );

    public DesiredCapabilities buildDesiredCapability(String capabilityFilePath) {
        String platform = PluginClI.getInstance().getPlatFormName();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        JSONObject platformCapabilities;
        JSONObject fullCapabilities;

        if (CAPS.get().equalsIgnoreCase(capabilityFilePath)) {
            LOGGER.info("Capabilities file is not specified. Using default capabilities file");
            fullCapabilities = CustomCapabilities.getInstance().getCapabilities();
        } else {
            LOGGER.info("Capabilities file is specified. Using specified capabilities file: " + capabilityFilePath);
            fullCapabilities = CustomCapabilities.getInstance().createInstance(capabilityFilePath);
        }

        platformCapabilities = fullCapabilities.getJSONObject(platform);
        JSONObject finalPlatformCapabilities = platformCapabilities;

        platformCapabilities.keySet().forEach(key -> {
            Object value = finalPlatformCapabilities.get(key);
            if (W3C_STANDARD_KEYS.contains(key)) {
                desiredCapabilities.setCapability(key, value);
            } else {
                desiredCapabilities.setCapability("appium:" + key, value);
            }
        });

        // Resolve and set "app" path if defined
        String resolvedAppPath = getAppPathInCapabilities(platform, fullCapabilities);
        if (resolvedAppPath != null) {
            desiredCapabilities.setCapability("appium:app", resolvedAppPath);
        }

        return desiredCapabilities;
    }

    private String getAppPathInCapabilities(String platform, JSONObject fullCapabilities) {
        if (fullCapabilities.getJSONObject(platform).has("app")) {
            Object app = fullCapabilities.getJSONObject(platform).get("app");
            boolean isLocal = PluginClI.getInstance().getPlugin().getDeviceFarm().getCloud() == null;
            boolean isUrl = new UrlValidator().isValid(app.toString());

            if (isLocal && !isUrl) {
                Path path = FileSystems.getDefault().getPath(app.toString());
                return path.normalize().toAbsolutePath().toString();
            } else {
                return app.toString();
            }
        }
        return null;
    }
}
