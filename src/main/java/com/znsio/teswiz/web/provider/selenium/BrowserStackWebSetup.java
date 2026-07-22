package com.znsio.teswiz.web.provider.selenium;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

public final class BrowserStackWebSetup {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackWebSetup.class.getName());

    private BrowserStackWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map<String, Object> loadedPlatformCapability = loadedCapabilityFile.get(Platform.web.name());
        return BrowserStackWebCapabilitySetup.updateBrowserStackCapabilities(
                capabilities,
                loadedPlatformCapability,
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                getSessionName(),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_PROXY),
                Setup.getFromConfigs(Setup.PROXY_URL),
                Setup.getFromConfigs(Setup.CLOUD_KEY));
    }

    private static String getSessionName() {
        try {
            return Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName();
        } catch (RuntimeException e) {
            String fallbackSessionName = Setup.getFromConfigs(Setup.LAUNCH_NAME);
            LOGGER.warn(String.format(
                    "Unable to resolve test context name. Falling back to launch name for sessionName: '%s'",
                    fallbackSessionName));
            return fallbackSessionName;
        }
    }
}
