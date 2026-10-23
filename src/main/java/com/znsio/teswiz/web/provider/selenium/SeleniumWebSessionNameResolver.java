package com.znsio.teswiz.web.provider.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;

final class SeleniumWebSessionNameResolver {
    private static final Logger LOGGER = LogManager.getLogger(SeleniumWebSessionNameResolver.class.getName());

    String resolve() {
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
