package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SeleniumContractConditionsTest {
    @AfterEach
    void tearDown() {
        System.clearProperty(SeleniumContractConditions.ENABLE_PROPERTY);
        System.clearProperty(SeleniumContractConditions.CHROME_BINARY_PROPERTY);
    }

    @Test
    void shouldBeDisabledByDefault() {
        assertThat(SeleniumContractConditions.isEnabled()).isFalse();
    }

    @Test
    void shouldAllowExplicitEnablement() {
        System.setProperty(SeleniumContractConditions.ENABLE_PROPERTY, "true");

        assertThat(SeleniumContractConditions.isEnabled()).isTrue();
    }

    @Test
    void shouldPreferConfiguredChromeBinary() {
        System.setProperty(SeleniumContractConditions.CHROME_BINARY_PROPERTY, "/custom/chrome");

        assertThat(SeleniumContractConditions.resolveChromeBinary()).contains("/custom/chrome");
    }
}
