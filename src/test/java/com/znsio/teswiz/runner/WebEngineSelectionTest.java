package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WebEngineSelectionTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void tearDown() {
        System.clearProperty("WEB_ENGINE");
    }

    @Test
    void shouldDefaultToSeleniumWhenWebEngineIsNotConfigured() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);

        assertThat(Runner.getWebEngine()).isEqualTo(WebEngine.SELENIUM);
    }

    @Test
    void shouldAllowSelectingPlaywrightTsFromSystemPropertyOverride() {
        System.setProperty("WEB_ENGINE", "playwright-ts");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);

        assertThat(Runner.getWebEngine()).isEqualTo(WebEngine.PLAYWRIGHT_TS);
    }
}
