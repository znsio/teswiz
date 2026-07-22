package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfigResolver;
import com.znsio.teswiz.entities.TEST_CONTEXT;

class PlaywrightBrowserConfigResolverTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("HEADLESS");
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldResolveLegacyBrowserConfigForPlaywrightWithoutRequiringPlaywrightBlock() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-legacy");

        PlaywrightBrowserConfig config = new PlaywrightBrowserConfigResolver().resolve("chrome", context);

        assertThat(config.browserName()).isEqualTo("chrome");
        assertThat(config.headless()).isFalse();
        assertThat(config.launchArgs()).contains("--use-fake-device-for-media-stream");
        assertThat(config.contextOptions()).containsEntry("ignoreHTTPSErrors", true);
        assertThat(config.executablePath()).isNull();
        assertThat(config.channel()).isNull();
    }

    @Test
    void shouldAllowOptionalPlaywrightOverridesWhileKeepingLegacyConfigValid() throws Exception {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-overrides");
        Path customConfig = Files.createTempFile("playwright-browser-config-", ".json");
        Files.writeString(customConfig, """
                {
                  "chrome": {
                    "excludeSwitches": ["enable-automation"],
                    "preferences": {
                      "credentials_enable_service": false,
                      "profile.password_manager_enabled": false,
                      "profile.default_content_setting_values.notifications": 1,
                      "profile.default_content_setting_values.media_stream_mic": 1,
                      "profile.default_content_setting_values.media_stream_camera": 1
                    },
                    "excludedSchemes": { "jhb": true },
                    "arguments": ["use-fake-device-for-media-stream"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": false, "include": ["disable-gpu"] },
                    "playwright": {
                      "launchOptions": {
                        "headless": true,
                        "args": ["lang=en-US"],
                        "channel": "chrome",
                        "executablePath": "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
                      },
                      "contextOptions": {
                        "ignoreHTTPSErrors": false,
                        "viewport": { "width": 1440, "height": 900 }
                      }
                    }
                  },
                  "firefox": {
                    "firefoxProfile": { "dom.push.enabled": false },
                    "excludeSwitches": [],
                    "preferences": { "dom.webnotifications.enabled": false },
                    "arguments": ["--disable-notifications"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": false }
                  }
                }
                """);
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, customConfig.toString());

        PlaywrightBrowserConfig config = new PlaywrightBrowserConfigResolver().resolve("chrome", context);

        assertThat(config.headless()).isTrue();
        assertThat(config.launchArgs()).contains("--use-fake-device-for-media-stream", "--lang=en-US",
                "--disable-gpu");
        assertThat(config.channel()).isEqualTo("chrome");
        assertThat(config.executablePath())
                .isEqualTo("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
        assertThat(config.contextOptions()).containsEntry("ignoreHTTPSErrors", false);
        assertThat((java.util.Map<String, Object>) config.contextOptions().get("viewport")).containsEntry("width", 1440)
                .containsEntry("height", 900);
    }

    @Test
    void shouldPreserveArbitraryPlaywrightContextAndLaunchOptionsWhileApplyingLegacyFallbacks() throws Exception {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-merged-options");
        Path customConfig = Files.createTempFile("playwright-browser-config-merged-", ".json");
        Files.writeString(customConfig, """
                {
                  "chrome": {
                    "excludeSwitches": ["enable-automation"],
                    "preferences": {
                      "credentials_enable_service": false,
                      "profile.password_manager_enabled": false,
                      "profile.default_content_setting_values.notifications": 1,
                      "profile.default_content_setting_values.media_stream_mic": 1,
                      "profile.default_content_setting_values.media_stream_camera": 1
                    },
                    "excludedSchemes": { "jhb": true },
                    "arguments": ["use-fake-device-for-media-stream", "--lang=en-US"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": true, "include": ["disable-gpu", "--lang=en-US"] },
                    "noProxy": "localhost,127.0.0.1",
                    "binary": "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "playwright": {
                      "launchOptions": {
                        "proxy": { "server": "http://playwright-proxy:9090", "bypass": "internal.example.com" }
                      },
                      "contextOptions": {
                        "locale": "en-GB",
                        "timezoneId": "Asia/Kolkata"
                      }
                    }
                  },
                  "firefox": {
                    "firefoxProfile": { "dom.push.enabled": false },
                    "excludeSwitches": [],
                    "preferences": { "dom.webnotifications.enabled": false },
                    "arguments": ["--disable-notifications"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": false }
                  }
                }
                """);
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, customConfig.toString());

        PlaywrightBrowserConfig config = new PlaywrightBrowserConfigResolver().resolve("chrome", context);

        assertThat(config.launchArgs()).containsExactly("--use-fake-device-for-media-stream", "--lang=en-US",
                "--disable-gpu");
        assertThat(config.executablePath())
                .isEqualTo("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
        assertThat(config.contextOptions()).containsEntry("ignoreHTTPSErrors", true)
                .containsEntry("locale", "en-GB")
                .containsEntry("timezoneId", "Asia/Kolkata");
        assertThat((java.util.Map<String, Object>) config.launchOptions().get("proxy"))
                .containsEntry("server", "http://playwright-proxy:9090")
                .containsEntry("bypass", "internal.example.com");
    }

    @Test
    void shouldNotReuseLegacyBinaryWhenLegacyConfigLooksElectronSpecific() throws Exception {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-electron-binary");
        Path customConfig = Files.createTempFile("playwright-browser-config-electron-", ".json");
        Files.writeString(customConfig, """
                {
                  "chrome": {
                    "excludeSwitches": ["enable-automation"],
                    "preferences": {
                      "credentials_enable_service": false,
                      "profile.password_manager_enabled": false,
                      "profile.default_content_setting_values.notifications": 1,
                      "profile.default_content_setting_values.media_stream_mic": 1,
                      "profile.default_content_setting_values.media_stream_camera": 1
                    },
                    "excludedSchemes": { "jhb": true },
                    "arguments": ["use-fake-device-for-media-stream"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": false, "include": ["disable-gpu"] },
                    "binary": "/Applications/JioMeet-Lite.app/Contents/MacOS/JioMeet-Lite",
                    "electronAppLoadingPage": true
                  },
                  "firefox": {
                    "firefoxProfile": { "dom.push.enabled": false },
                    "excludeSwitches": [],
                    "preferences": { "dom.webnotifications.enabled": false },
                    "arguments": ["--disable-notifications"],
                    "maximize": true,
                    "acceptInsecureCerts": true,
                    "verboseLogging": false,
                    "headlessOptions": { "headless": false }
                  }
                }
                """);
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, customConfig.toString());

        PlaywrightBrowserConfig config = new PlaywrightBrowserConfigResolver().resolve("chrome", context);

        assertThat(config.executablePath()).isNull();
    }

    @Test
    void shouldAllowHeadlessFlagToOverrideBrowserConfigForPlaywright() {
        System.setProperty("HEADLESS", "true");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-headless-override");

        PlaywrightBrowserConfig config = new PlaywrightBrowserConfigResolver().resolve("chrome", context);

        assertThat(config.headless()).isTrue();
        assertThat(config.launchArgs()).contains("--disable-gpu");
    }
}
