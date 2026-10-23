package com.znsio.teswiz.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ScenarioSessionMetadataAggregatorTest {
    @Test
    void shouldAggregateSessionAndProviderArtifactMetadataFromScenarioFiles() throws Exception {
        Path reportsDir = Files.createTempDirectory("scenario-session-metadata-aggregator");
        Path scenarioDir = Files.createDirectories(reportsDir.resolve("scenario-1"));
        Files.writeString(scenarioDir.resolve("scenario-session-metadata.json"), """
                {
                  "personas": ["buyer", "seller"],
                  "platforms": ["web", "android"],
                  "engines": ["playwright-ts", "appium"],
                  "providers": ["browserstack", "local"],
                  "sessions": [
                    {
                      "userPersona": "buyer",
                      "metadata": {
                        "providerSessionId": "bs-session-1",
                        "providerReportUrl": "https://browserstack.example/session/bs-session-1",
                        "providerConsoleLogsUrl": "https://browserstack.example/console/bs-session-1"
                      }
                    }
                  ]
                }
                """);

        Map<String, String> metadata = ScenarioSessionMetadataAggregator.aggregate(reportsDir.toString());

        assertThat(metadata)
                .containsEntry("SESSION_PERSONAS", "buyer, seller")
                .containsEntry("SESSION_PLATFORMS", "android, web")
                .containsEntry("SESSION_ENGINES", "appium, playwright-ts")
                .containsEntry("SESSION_PROVIDERS", "browserstack, local")
                .containsEntry("SESSION_PROVIDER_SESSION_IDS", "bs-session-1")
                .containsEntry("SESSION_PROVIDER_REPORT_URLS",
                        "https://browserstack.example/session/bs-session-1")
                .containsEntry("SESSION_PROVIDER_CONSOLE_LOG_URLS",
                        "https://browserstack.example/console/bs-session-1");
    }
}
