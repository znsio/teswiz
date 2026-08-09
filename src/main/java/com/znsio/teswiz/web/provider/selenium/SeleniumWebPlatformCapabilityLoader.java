package com.znsio.teswiz.web.provider.selenium;

import java.util.Map;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;

final class SeleniumWebPlatformCapabilityLoader {
    Map load() {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        return loadedCapabilityFile.get(Platform.web.name());
    }
}
