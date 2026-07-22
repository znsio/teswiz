package com.znsio.teswiz.web.provider.selenium;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

public final class LambdaTestWebSetup {
    private LambdaTestWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map loadedPlatformCapability = loadedCapabilityFile.get(Platform.web.name());
        return LambdaTestWebCapabilitySetup.updateLambdaTestCapabilities(
                capabilities,
                loadedPlatformCapability,
                Setup.getFromConfigs(Setup.CLOUD_USERNAME),
                Setup.getFromConfigs(Setup.CLOUD_KEY),
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                Runner.getTestExecutionContext(Thread.currentThread().getId()).getTestName(),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING));
    }
}
