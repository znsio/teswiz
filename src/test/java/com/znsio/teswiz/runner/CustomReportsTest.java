package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CustomReportsTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("WEB_ENGINE");
    }

    @Test
    void shouldOnlyPickCucumberJsonFilesFromReportsDirectory() throws Exception {
        Path reportsDir = Files.createTempDirectory("custom-reports");
        Path scenarioDir = Files.createDirectories(reportsDir.resolve("scenario-1"));

        Path cucumberJson = Files.writeString(reportsDir.resolve("cucumber-json-report.json"), "{}");
        Files.writeString(reportsDir.resolve("browser_config-playwright-recommended.json"), "{}");
        Files.writeString(scenarioDir.resolve("scenario-session-metadata.json"), "{}");

        List<String> jsonPaths = CustomReports.processTestResultJsonFiles(reportsDir.toString());

        assertThat(jsonPaths).containsExactly(cucumberJson.toAbsolutePath().toString());
    }

    @Test
    void shouldIncludeWebEngineInReportMetadata() {
        System.setProperty("WEB_ENGINE", "playwright-ts");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();

        HashMap<String, Object> metadata = CustomReports.buildTestRunMetadata(null);

        assertThat(metadata).containsEntry(Setup.WEB_ENGINE, "playwright-ts");
    }

    @Test
    void shouldIncludeAggregatedSessionMetadataInReportMetadataWhenAvailable() throws Exception {
        Path reportsDir = Files.createTempDirectory("custom-reports-metadata");
        Path scenarioDir = Files.createDirectories(reportsDir.resolve("scenario-1"));
        Files.writeString(scenarioDir.resolve("scenario-session-metadata.json"), """
                {
                  "personas": ["buyer", "seller"],
                  "platforms": ["web", "android"],
                  "engines": ["playwright-java", "appium"],
                  "providers": ["local", "browserstack"]
                }
                """);

        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();

        HashMap<String, Object> metadata = CustomReports.buildTestRunMetadata(reportsDir.toString());

        assertThat(metadata)
                .containsEntry("SESSION_PERSONAS", "buyer, seller")
                .containsEntry("SESSION_PLATFORMS", "android, web")
                .containsEntry("SESSION_ENGINES", "appium, playwright-java")
                .containsEntry("SESSION_PROVIDERS", "browserstack, local");
    }

    @Test
    void shouldIncludeAggregatedProviderSessionMetadataInReportMetadataWhenAvailable() throws Exception {
        Path reportsDir = Files.createTempDirectory("custom-reports-provider-metadata");
        Path scenarioDir = Files.createDirectories(reportsDir.resolve("scenario-1"));
        Files.writeString(scenarioDir.resolve("scenario-session-metadata.json"), """
                {
                  "sessions": [
                    {
                      "userPersona": "buyer",
                      "metadata": {
                        "providerSessionId": "bs-session-1",
                        "providerReportUrl": "https://browserstack.example/session/bs-session-1"
                      }
                    },
                    {
                      "userPersona": "seller",
                      "metadata": {
                        "providerSessionId": "lt-session-1",
                        "providerReportUrl": "https://automation.lambdatest.com/logs/?sessionID=lt-session-1"
                      }
                    }
                  ]
                }
                """);

        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();

        HashMap<String, Object> metadata = CustomReports.buildTestRunMetadata(reportsDir.toString());

        assertThat(metadata)
                .containsEntry("SESSION_PROVIDER_SESSION_IDS", "bs-session-1, lt-session-1")
                .containsEntry("SESSION_PROVIDER_REPORT_URLS",
                        "https://automation.lambdatest.com/logs/?sessionID=lt-session-1, https://browserstack.example/session/bs-session-1");
    }
}
