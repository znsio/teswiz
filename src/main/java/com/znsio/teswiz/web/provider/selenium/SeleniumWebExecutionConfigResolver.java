package com.znsio.teswiz.web.provider.selenium;

import com.znsio.teswiz.runner.Setup;

final class SeleniumWebExecutionConfigResolver {
    private final SeleniumWebSessionNameResolver sessionNameResolver = new SeleniumWebSessionNameResolver();

    SeleniumWebExecutionConfig resolve() {
        return new SeleniumWebExecutionConfig(
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                sessionNameResolver.resolve(),
                Setup.getFromConfigs(Setup.CLOUD_USERNAME),
                Setup.getFromConfigs(Setup.CLOUD_KEY),
                Setup.getFromConfigs(Setup.PROXY_URL),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_PROXY));
    }
}
