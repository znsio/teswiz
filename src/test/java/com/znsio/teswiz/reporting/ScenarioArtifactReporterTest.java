package com.znsio.teswiz.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.session.UserPersonaDetails;

class ScenarioArtifactReporterTest {
    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldWriteScenarioMetadataAndPublishPlaywrightArtifacts() throws Exception {
        TestExecutionContext context = new TestExecutionContext("reporting-parity");
        Path scenarioDir = Files.createTempDirectory("scenario-artifacts");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, "reporting-parity");
        context.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, 7);

        String sessionId = "playwright-session-1";
        Path traceFile = Files.writeString(scenarioDir.resolve("buyer-" + sessionId + "-trace.zip"), "trace");
        Path harFile = Files.writeString(scenarioDir.resolve("buyer-" + sessionId + "-network.har"), "har");
        Path consoleFile = Files.writeString(scenarioDir.resolve("buyer-" + sessionId + "-console.log"), "hello");

        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        userPersonaDetails.addSessionHandle("buyer", new SessionHandle(
                "buyer",
                Platform.web,
                "playwright-ts",
                sessionId,
                scenarioDir.toString(),
                Map.of(
                        "browserName", "chrome",
                        "contextId", "context-1",
                        "pageId", "page-1")));

        List<Path> publishedArtifacts = new ArrayList<>();

        Path metadataFile = ScenarioArtifactReporter.publish(context, userPersonaDetails,
                (message, artifact) -> publishedArtifacts.add(artifact.toPath()));

        assertThat(metadataFile).exists();
        assertThat(Files.readString(metadataFile))
                .contains("\"scenarioName\": \"reporting-parity\"")
                .contains("\"provider\": \"local\"")
                .contains("\"userPersona\": \"buyer\"")
                .contains("\"engine\": \"playwright-ts\"")
                .contains("\"browserName\": \"chrome\"");
        assertThat(publishedArtifacts)
                .contains(metadataFile, traceFile, harFile, consoleFile);
    }
}
