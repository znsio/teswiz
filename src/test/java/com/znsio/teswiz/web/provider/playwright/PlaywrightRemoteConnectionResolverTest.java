package com.znsio.teswiz.web.provider.playwright;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;

class PlaywrightRemoteConnectionResolverTest {
    private final PlaywrightRemoteConnectionResolver resolver = new PlaywrightRemoteConnectionResolver();

    @Test
    void shouldReturnEmptyForLocalExecution() {
        assertThat(resolver.resolve("chrome", browserConfig("chrome"), PlaywrightExecutionProviderConfig.local(), "buyer"))
                .isEmpty();
    }

    @Test
    void shouldBuildBrowserStackWebSocketEndpoint() {
        PlaywrightRemoteConnectionDescriptor descriptor = resolver.resolve("chrome",
                browserConfig("chrome"),
                new PlaywrightExecutionProviderConfig("browserstack",
                        "https://hub-cloud.browserstack.com",
                        "https://api-cloud.browserstack.com/app-automate/",
                        "browserstack_user",
                        "browserstack_key",
                        Map.of("browserName", "chrome",
                                "browserstackOptions", Map.of(
                                        "os", "Windows",
                                        "osVersion", "11",
                                        "browserVersion", "latest",
                                        "debug", true))),
                "buyer").orElseThrow();

        assertThat(descriptor.wsEndpoint()).startsWith("wss://cdp.browserstack.com/playwright?caps=");
        JSONObject capabilities = decodeCapabilities(descriptor.wsEndpoint(), "caps=");
        assertThat(capabilities.getString("browser")).isEqualTo("chrome");
        assertThat(capabilities.getString("browser_version")).isEqualTo("latest");
        assertThat(capabilities.getString("os")).isEqualTo("Windows");
        assertThat(capabilities.getString("os_version")).isEqualTo("11");
        assertThat(capabilities.getString("browserstack.username")).isEqualTo("browserstack_user");
        assertThat(capabilities.getString("browserstack.accessKey")).isEqualTo("browserstack_key");
        assertThat(capabilities.getString("name")).isEqualTo("buyer");
    }

    @Test
    void shouldBuildLambdaTestWebSocketEndpointFromCurrentTeswizWebCapabilityShape() {
        PlaywrightRemoteConnectionDescriptor descriptor = resolver.resolve("chrome",
                browserConfig("chrome"),
                new PlaywrightExecutionProviderConfig("lambdatest",
                        "https://mobile-hub.lambdatest.com",
                        "https://manual-api.lambdatest.com",
                        "lt_user",
                        "lt_key",
                        Map.of("browserName", "chrome",
                                "version", "latest",
                                "platform", "Windows 11",
                                "resolution", "1920x1080",
                                "network", true,
                                "appProfiling", true,
                                "console", true,
                                "visual", true,
                                "tunnel", false)),
                "buyer").orElseThrow();

        assertThat(descriptor.wsEndpoint()).startsWith("wss://cdp.lambdatest.com/playwright?capabilities=");
        JSONObject capabilities = decodeCapabilities(descriptor.wsEndpoint(), "capabilities=");
        assertThat(capabilities.getString("browserName")).isEqualTo("chrome");
        assertThat(capabilities.getString("browserVersion")).isEqualTo("latest");
        assertThat(capabilities.getString("platformName")).isEqualTo("Windows 11");

        JSONObject ltOptions = capabilities.getJSONObject("LT:Options");
        assertThat(ltOptions.getString("platform")).isEqualTo("Windows 11");
        assertThat(ltOptions.getString("platformName")).isEqualTo("Windows 11");
        assertThat(ltOptions.getString("user")).isEqualTo("lt_user");
        assertThat(ltOptions.getString("username")).isEqualTo("lt_user");
        assertThat(ltOptions.getString("accessKey")).isEqualTo("lt_key");
        assertThat(ltOptions.getString("name")).isEqualTo("buyer");
        assertThat(ltOptions.getBoolean("headless")).isTrue();
    }

    @Test
    void shouldFailFastForHeadSpinUntilAdapterExists() {
        assertThatThrownBy(() -> resolver.resolve("chrome",
                browserConfig("chrome"),
                new PlaywrightExecutionProviderConfig("headspin",
                        "https://headspin.example.com",
                        null,
                        "headspin_user",
                        "headspin_key",
                        Map.of("browserName", "chrome")),
                "buyer"))
                        .isInstanceOf(UnsupportedOperationException.class)
                        .hasMessage("HeadSpin is not supported with Playwright web engines in teswiz.");
    }

    private static PlaywrightBrowserConfig browserConfig(String browserName) {
        return new PlaywrightBrowserConfig(browserName, true, List.of("--headless=new"), null, null, Map.of(), Map.of());
    }

    private static JSONObject decodeCapabilities(String wsEndpoint, String parameterName) {
        String encodedCapabilities = wsEndpoint.substring(wsEndpoint.indexOf(parameterName) + parameterName.length());
        return new JSONObject(URLDecoder.decode(encodedCapabilities, StandardCharsets.UTF_8));
    }
}
