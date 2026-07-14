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

        HashMap<String, Object> metadata = CustomReports.buildTestRunMetadata();

        assertThat(metadata).containsEntry(Setup.WEB_ENGINE, "playwright-ts");
    }
}
