package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class CustomReportsTest {
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
}
