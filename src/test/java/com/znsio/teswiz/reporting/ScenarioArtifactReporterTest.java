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
    private record PublishedArtifact(String message, Path path) {
    }

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

        List<PublishedArtifact> publishedArtifacts = new ArrayList<>();

        Path metadataFile = ScenarioArtifactReporter.publish(context, userPersonaDetails,
                (message, artifact) -> publishedArtifacts.add(new PublishedArtifact(message, artifact.toPath())));
        Path summaryFile = scenarioDir.resolve("scenario-session-summary.txt");

        assertThat(metadataFile).exists();
        assertThat(summaryFile).exists();
        assertThat(Files.readString(metadataFile))
                .contains("\"scenarioName\": \"reporting-parity\"")
                .contains("\"provider\": \"local\"")
                .contains("\"userPersona\": \"buyer\"")
                .contains("\"engine\": \"playwright-ts\"")
                .contains("\"browserName\": \"chrome\"");
        assertThat(Files.readString(summaryFile))
                .contains("Scenario: reporting-parity")
                .contains("Persona: buyer")
                .contains("Engine: playwright-ts")
                .contains("Provider: local")
                .contains("Platform: web")
                .contains("SessionId: playwright-session-1");
        assertThat(publishedArtifacts.stream().map(PublishedArtifact::path))
                .contains(metadataFile, summaryFile, traceFile, harFile, consoleFile);
        assertThat(publishedArtifacts.stream().map(PublishedArtifact::message))
                .anySatisfy(message -> assertThat(message)
                        .contains("persona=buyer")
                        .contains("platform=web")
                        .contains("engine=playwright-ts")
                        .contains("provider=local")
                        .contains("sessionId=playwright-session-1"));
    }

    @Test
    void shouldPublishPlaywrightJavaArtifactsUsingTheSharedArtifactContract() throws Exception {
        TestExecutionContext context = new TestExecutionContext("playwright-java-reporting-parity");
        Path scenarioDir = Files.createTempDirectory("playwright-java-scenario-artifacts");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, "playwright-java-reporting-parity");
        context.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, 1);
        Path deviceLogsDir = Files.createDirectories(scenarioDir.resolve("deviceLogs"));

        String sessionId = "playwright-java-session-1";
        Path traceFile = Files.writeString(scenarioDir.resolve("seller-" + sessionId + "-trace.zip"), "trace");
        Path harFile = Files.writeString(scenarioDir.resolve("seller-" + sessionId + "-network.har"), "har");
        Path consoleFile = Files.writeString(scenarioDir.resolve("seller-" + sessionId + "-console.log"), "hello");
        Path visualLogFile = Files.writeString(deviceLogsDir.resolve("applitools-web.log"), "visual-log");

        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        userPersonaDetails.addSessionHandle("seller", new SessionHandle(
                "seller",
                Platform.web,
                "playwright-java",
                sessionId,
                scenarioDir.toString(),
                Map.of(
                        "browserName", "chrome",
                        "provider", "local")));

        List<PublishedArtifact> publishedArtifacts = new ArrayList<>();

        Path metadataFile = ScenarioArtifactReporter.publish(context, userPersonaDetails,
                (message, artifact) -> publishedArtifacts.add(new PublishedArtifact(message, artifact.toPath())));

        assertThat(metadataFile).exists();
        assertThat(Files.readString(metadataFile))
                .contains("\"engine\": \"playwright-java\"")
                .contains("\"browserName\": \"chrome\"");
        assertThat(publishedArtifacts.stream().map(PublishedArtifact::path))
                .contains(metadataFile, traceFile, harFile, consoleFile, visualLogFile);
        assertThat(publishedArtifacts.stream().map(PublishedArtifact::message))
                .anySatisfy(message -> assertThat(message)
                        .contains("persona=seller")
                        .contains("platform=web")
                        .contains("engine=playwright-java")
                        .contains("provider=local")
                        .contains("sessionId=playwright-java-session-1"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Visual artifact")
                        .contains("applitools-web.log"));
    }

    @Test
    void shouldPublishWebCloudReportLinksThroughTheSharedReportingPath() throws Exception {
        TestExecutionContext context = new TestExecutionContext("web-cloud-report-links");
        Path scenarioDir = Files.createTempDirectory("web-cloud-report-links");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, "web-cloud-report-links");
        context.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, 1);

        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        userPersonaDetails.addSessionHandle("buyer", new SessionHandle(
                "buyer",
                Platform.web,
                "playwright-ts",
                "playwright-session-1",
                scenarioDir.toString(),
                Map.of(
                        "provider", "browserstack",
                        "providerReportUrl", "https://browserstack.example/session/bs-session-1")));
        userPersonaDetails.addSessionHandle("seller", new SessionHandle(
                "seller",
                Platform.web,
                "playwright-java",
                "playwright-session-2",
                scenarioDir.toString(),
                Map.of(
                        "provider", "lambdatest",
                        "providerSessionId", "lt-session-1")));
        userPersonaDetails.addSessionHandle("approver", new SessionHandle(
                "approver",
                Platform.android,
                "appium",
                "appium-session-1",
                scenarioDir.toString(),
                Map.of(
                        "provider", "browserstack",
                        "providerReportUrl", "https://browserstack.example/mobile-session")));

        List<PublishedArtifact> publishedArtifacts = new ArrayList<>();
        List<String> reportMessages = new ArrayList<>();

        ScenarioArtifactReporter.publish(context, userPersonaDetails,
                (message, artifact) -> publishedArtifacts.add(new PublishedArtifact(message, artifact.toPath())),
                reportMessages::add);

        assertThat(publishedArtifacts).isNotEmpty();
        assertThat(reportMessages)
                .contains("BrowserStack Report link available here: https://browserstack.example/session/bs-session-1")
                .contains("LambdaTest Report link available here: https://automation.lambdatest.com/logs/?sessionID=lt-session-1");
        assertThat(reportMessages)
                .noneMatch(message -> message.contains("mobile-session"));
    }

    @Test
    void shouldWriteAggregatedScenarioMetadataForMixedSessions() throws Exception {
        TestExecutionContext context = new TestExecutionContext("mixed-reporting-parity");
        Path scenarioDir = Files.createTempDirectory("mixed-scenario-artifacts");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME, "mixed-reporting-parity");
        context.addTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT, 2);

        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        userPersonaDetails.addSessionHandle("buyer", new SessionHandle(
                "buyer",
                Platform.web,
                "playwright-ts",
                "playwright-session-1",
                scenarioDir.toString(),
                Map.of(
                        "browserName", "chrome",
                        "provider", "browserstack",
                        "contextId", "context-1")));
        userPersonaDetails.addSessionHandle("approver", new SessionHandle(
                "approver",
                Platform.android,
                "appium",
                "appium-session-1",
                scenarioDir.toString(),
                Map.of(
                        "provider", "browserstack",
                        "deviceName", "Pixel 8")));
        userPersonaDetails.addSessionHandle("auditor", new SessionHandle(
                "auditor",
                Platform.web,
                "selenium",
                "selenium-session-1",
                scenarioDir.toString(),
                Map.of(
                        "browserName", "firefox",
                        "provider", "local")));

        Path metadataFile = ScenarioArtifactReporter.writeScenarioMetadata(context, userPersonaDetails);
        String metadata = Files.readString(metadataFile);

        assertThat(metadata)
                .contains("\"scenarioName\": \"mixed-reporting-parity\"")
                .contains("\"personas\": [")
                .contains("\"buyer\"")
                .contains("\"approver\"")
                .contains("\"auditor\"")
                .contains("\"platforms\": [")
                .contains("\"web\"")
                .contains("\"android\"")
                .contains("\"engines\": [")
                .contains("\"playwright-ts\"")
                .contains("\"appium\"")
                .contains("\"selenium\"")
                .contains("\"providers\": [")
                .contains("\"browserstack\"")
                .contains("\"local\"");
    }
}
