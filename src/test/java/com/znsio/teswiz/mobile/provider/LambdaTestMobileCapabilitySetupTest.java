package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

class LambdaTestMobileCapabilitySetupTest {
    @Test
    void shouldNormalizeMobileCapabilitiesAndPopulateLtOptions() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("browserName", "chrome");
        platformCapabilities.put("platformName", "Android");
        platformCapabilities.put("device", "Galaxy S23");
        platformCapabilities.put("os_version", "13");
        platformCapabilities.put("lt:options", new HashMap<>(Map.of("network", false, "visual", true)));

        LambdaTestMobileCapabilitySetup.prepareCapabilities(platformCapabilities,
                "lt-user", "lt-key", "teswiz", "nightly-run", "./target/Jul", true);

        assertThat(platformCapabilities.get("deviceName")).isEqualTo("Galaxy S23");
        assertThat(platformCapabilities.get("platformVersion")).isEqualTo("13");
        assertThat(platformCapabilities).doesNotContainKeys("device", "os_version");

        Map<String, Object> ltOptions = (Map<String, Object>) platformCapabilities.get("lt:options");
        assertThat(ltOptions.get("username")).isEqualTo("lt-user");
        assertThat(ltOptions.get("accessKey")).isEqualTo("lt-key");
        assertThat(ltOptions.get("project")).isEqualTo("teswiz");
        assertThat(ltOptions.get("build")).isEqualTo("nightly-run-.targetJul");
        assertThat(ltOptions.get("w3c")).isEqualTo(true);
        assertThat(ltOptions.get("tunnel")).isEqualTo(true);
        assertThat(ltOptions.get("network")).isEqualTo(false);
        assertThat(ltOptions.get("visual")).isEqualTo(true);
    }

    @Test
    void shouldPreferExistingAppReferenceBeforeConfiguredPath() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("app", "lt://APP456");

        assertThat(LambdaTestMobileCapabilitySetup.resolveAppReference(platformCapabilities, "lt://APP123"))
                .isEqualTo("lt://APP123");
        assertThat(LambdaTestMobileCapabilitySetup.resolveAppReference(platformCapabilities, "temp/sample.ipa"))
                .isEqualTo("lt://APP456");
    }

    @Test
    void shouldFailWhenNoLambdaTestAppReferenceIsAvailable() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("app", "temp/sample.ipa");

        assertThatThrownBy(() -> LambdaTestMobileCapabilitySetup.resolveAppReference(platformCapabilities,
                "temp/sample.ipa"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("valid LambdaTest app id");
    }
}
