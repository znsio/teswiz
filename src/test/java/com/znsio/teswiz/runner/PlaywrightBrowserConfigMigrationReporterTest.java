package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;

class PlaywrightBrowserConfigMigrationReporterTest {
    @BeforeEach
    void setUp() {
        PlaywrightBrowserConfigMigrationReporter.clear();
    }

    @AfterEach
    void cleanUp() {
        PlaywrightBrowserConfigMigrationReporter.clear();
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldGenerateRecommendedConfigInCurrentReportsDirectoryAndRegisterNotice() throws Exception {
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-migration");
        Path reportsDir = Files.createTempDirectory("playwright-reports");
        Path scenarioDir = Files.createDirectory(reportsDir.resolve("1-sample_1"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        Path sourceConfig = Files.createTempFile("browser-config-", ".json");
        Files.writeString(sourceConfig, """
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
                    "binary": "/Applications/JioMeet-Lite.app/Contents/MacOS/JioMeet-Lite"
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
        JSONObject originalConfig = new JSONObject(Files.readString(sourceConfig));

        Path generatedFile = PlaywrightBrowserConfigMigrationReporter.registerMigrationArtifact(context,
                sourceConfig.toString(), originalConfig, new PlaywrightBrowserConfigMigrator());

        assertThat(generatedFile).isNotNull();
        assertThat(generatedFile.getParent()).isEqualTo(reportsDir);
        assertThat(generatedFile.getFileName().toString()).contains("playwright-recommended");
        JSONObject generatedConfig = new JSONObject(Files.readString(generatedFile));
        assertThat(generatedConfig.getJSONObject("chrome").has("playwright")).isTrue();
        assertThat(generatedConfig.getJSONObject("firefox").has("playwright")).isTrue();
        assertThat(generatedConfig.getJSONObject("chrome").getJSONObject("playwright")
                .getJSONObject("contextOptions").getBoolean("ignoreHTTPSErrors")).isTrue();
        assertThat(PlaywrightBrowserConfigMigrationReporter.buildSummaryMessage())
                .contains(generatedFile.toString())
                .contains(sourceConfig.toString())
                .contains("binary");
    }

    @Test
    void shouldNotGenerateMigrationArtifactWhenBrowserConfigAlreadyHasPlaywrightBlocks() throws Exception {
        TestExecutionContext context = new TestExecutionContext("playwright-browser-config-no-migration");
        Path reportsDir = Files.createTempDirectory("playwright-reports-ready");
        Path scenarioDir = Files.createDirectory(reportsDir.resolve("1-sample_1"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        Path sourceConfig = Files.createTempFile("browser-config-ready-", ".json");
        Files.writeString(sourceConfig, """
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
                      "launchOptions": { "headless": false, "args": ["--use-fake-device-for-media-stream"] },
                      "contextOptions": { "ignoreHTTPSErrors": true }
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
                    "headlessOptions": { "headless": false },
                    "playwright": {
                      "launchOptions": { "headless": false, "args": ["--disable-notifications"] },
                      "contextOptions": { "ignoreHTTPSErrors": true }
                    }
                  }
                }
                """);
        JSONObject originalConfig = new JSONObject(Files.readString(sourceConfig));

        Path generatedFile = PlaywrightBrowserConfigMigrationReporter.registerMigrationArtifact(context,
                sourceConfig.toString(), originalConfig, new PlaywrightBrowserConfigMigrator());

        assertThat(generatedFile).isNull();
        assertThat(PlaywrightBrowserConfigMigrationReporter.buildSummaryMessage()).isEmpty();
    }

    @Test
    void shouldBuildVisibleSummaryWithReplacementInstructions() {
        PlaywrightBrowserConfigMigrationReporter.registerNotice("/tmp/source.json", "/tmp/recommended.json",
                List.of("Legacy binary was not migrated automatically."));

        assertThat(PlaywrightBrowserConfigMigrationReporter.buildSummaryMessage())
                .contains("Playwright-ready browser config")
                .contains("/tmp/source.json")
                .contains("/tmp/recommended.json")
                .contains("replace the old config")
                .contains("Legacy binary was not migrated automatically.");
    }
}
