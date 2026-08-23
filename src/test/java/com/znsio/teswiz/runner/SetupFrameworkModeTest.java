package com.znsio.teswiz.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SetupFrameworkModeTest {
    private static final String cucumberConfigFilePath = "./configs/cli_local_config.properties";
    private static final String testNgConfigFilePath = "./configs/notepad/notepad_windows_config.properties";

    @AfterEach
    void clearFrameworkOverride() {
        System.clearProperty(Setup.FRAMEWORK);
    }

    @Test
    void shouldNotBeTestNgModeWhenConfigFileSaysCucumber() {
        Setup.load(cucumberConfigFilePath);
        Setup.loadAndUpdateConfigParameters(cucumberConfigFilePath);

        assertThat(Setup.isTestNgExecutionMode()).isFalse();
    }

    @Test
    void shouldBeTestNgModeWhenConfigFileSaysTestNg() {
        Setup.load(testNgConfigFilePath);
        Setup.loadAndUpdateConfigParameters(testNgConfigFilePath);

        assertThat(Setup.isTestNgExecutionMode()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"testng", "TestNG", "TESTNG"})
    void shouldBeTestNgModeIrrespectiveOfCase(String frameworkValue) {
        System.setProperty(Setup.FRAMEWORK, frameworkValue);
        Setup.load(cucumberConfigFilePath);
        Setup.loadAndUpdateConfigParameters(cucumberConfigFilePath);

        assertThat(Setup.isTestNgExecutionMode()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cucumber", "bogus", ""})
    void shouldFallBackToCucumberModeWhenFrameworkIsNotRecognisedAsTestNg(String frameworkValue) {
        System.setProperty(Setup.FRAMEWORK, frameworkValue);
        Setup.load(cucumberConfigFilePath);
        Setup.loadAndUpdateConfigParameters(cucumberConfigFilePath);

        assertThat(Setup.isTestNgExecutionMode()).isFalse();
    }
}
