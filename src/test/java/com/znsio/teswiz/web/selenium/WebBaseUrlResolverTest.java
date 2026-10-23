package com.znsio.teswiz.web.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;

class WebBaseUrlResolverTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";
    private static final String TESWIZ_BASE_URL = "TESWIZ_BASE_URL";

    @AfterEach
    void tearDown() {
        System.clearProperty("WEB_ENGINE");
        System.clearProperty(TESWIZ_BASE_URL);
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldResolveDefaultWebBaseUrlWhenAppUsesTheGenericBaseUrlKey() {
        new TestExecutionContext("web-base-url-default-test");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();

        assertThat(WebBaseUrlResolver.resolve(Runner.DEFAULT))
                .isEqualTo("https://the-internet.herokuapp.com/");
    }

    @Test
    void shouldResolveAppSpecificWebBaseUrlWhenAppNameIsNotDefault() {
        new TestExecutionContext("web-base-url-app-specific-test");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();
        System.setProperty(TESWIZ_BASE_URL, "https://example.com/custom");

        assertThat(WebBaseUrlResolver.resolve("teswiz"))
                .isEqualTo("https://example.com/custom");
    }
}
