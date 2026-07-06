package com.znsio.teswiz.reporting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ReportPortalLogger;

public final class ScenarioArtifactReporter {
    private static final Logger LOGGER = LogManager.getLogger(ScenarioArtifactReporter.class.getName());
    private static final String SCENARIO_METADATA_FILE_NAME = "scenario-session-metadata.json";
    private static final List<String> PLAYWRIGHT_ARTIFACT_SUFFIXES = List.of(
            "-trace.zip",
            "-network.har",
            "-console.log");

    private ScenarioArtifactReporter() {
    }

    public static Path publish(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        return publish(context, userPersonaDetails, ReportPortalLogger::attachFileInReportPortal);
    }

    static Path publish(TestExecutionContext context, UserPersonaDetails userPersonaDetails,
            ScenarioArtifactPublisher artifactPublisher) {
        Path metadataFile = writeScenarioMetadata(context, userPersonaDetails);
        publishArtifact("Scenario session metadata", metadataFile.toFile(), artifactPublisher);
        for (Path artifact : discoverPlaywrightArtifacts(userPersonaDetails)) {
            publishArtifact("Playwright artifact: " + artifact.getFileName(), artifact.toFile(), artifactPublisher);
        }
        return metadataFile;
    }

    private static void publishArtifact(String message, File artifact, ScenarioArtifactPublisher artifactPublisher) {
        if (!artifact.exists()) {
            return;
        }
        artifactPublisher.publish(message, artifact);
    }

    static Path writeScenarioMetadata(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        String scenarioLogDirectory = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        if (null == scenarioLogDirectory || scenarioLogDirectory.isBlank()) {
            throw new IllegalStateException("Scenario log directory is required to publish scenario artifacts");
        }
        Path metadataFile = Path.of(scenarioLogDirectory, SCENARIO_METADATA_FILE_NAME);
        try {
            Files.writeString(metadataFile, buildScenarioMetadata(context, userPersonaDetails).toString(2),
                    StandardCharsets.UTF_8);
            return metadataFile;
        } catch (IOException e) {
            throw new RuntimeException("Unable to write scenario metadata artifact: " + metadataFile, e);
        }
    }

    private static JSONObject buildScenarioMetadata(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        JSONArray sessions = new JSONArray();
        for (Map.Entry<String, SessionHandle> entry : userPersonaDetails.getAllAssignedUserPersonasAndSessionHandles().entrySet()) {
            SessionHandle sessionHandle = entry.getValue();
            sessions.put(new JSONObject()
                    .put("userPersona", sessionHandle.userPersona())
                    .put("platform", sessionHandle.platform().name())
                    .put("engine", sessionHandle.engine())
                    .put("sessionId", sessionHandle.sessionId())
                    .put("artifactPath", sessionHandle.artifactPath())
                    .put("metadata", new JSONObject(sessionHandle.metadata())));
        }

        return new JSONObject()
                .put("scenarioName", context.getTestName())
                .put("normalisedScenarioName", context.getTestStateAsString(TEST_CONTEXT.NORMALISED_SCENARIO_NAME))
                .put("scenarioRunCount", context.getTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT))
                .put("provider", getProvider())
                .put("webEngine", getWebEngine())
                .put("sessions", sessions);
    }

    private static String getProvider() {
        try {
            String cloudName = Runner.getCloudName();
            return null == cloudName || cloudName.isBlank() || Runner.NOT_SET.equalsIgnoreCase(cloudName)
                    ? "local"
                    : cloudName;
        } catch (RuntimeException e) {
            return "local";
        }
    }

    private static String getWebEngine() {
        try {
            return Runner.getWebEngine().getConfigValue();
        } catch (RuntimeException e) {
            return Runner.NOT_SET;
        }
    }

    static List<Path> discoverPlaywrightArtifacts(UserPersonaDetails userPersonaDetails) {
        List<Path> artifacts = new ArrayList<>();
        for (SessionHandle sessionHandle : userPersonaDetails.getAllAssignedUserPersonasAndSessionHandles().values()) {
            if (!"playwright-ts".equalsIgnoreCase(sessionHandle.engine())) {
                continue;
            }
            Path artifactDirectory = Path.of(sessionHandle.artifactPath());
            String artifactPrefix = sessionHandle.userPersona() + "-" + sessionHandle.sessionId();
            for (String artifactSuffix : PLAYWRIGHT_ARTIFACT_SUFFIXES) {
                Path artifact = artifactDirectory.resolve(artifactPrefix + artifactSuffix);
                if (Files.exists(artifact)) {
                    artifacts.add(artifact);
                }
            }
        }
        artifacts.sort(Comparator.naturalOrder());
        LOGGER.info("Discovered '{}' Playwright reporting artifacts", artifacts.size());
        return artifacts;
    }

    @FunctionalInterface
    interface ScenarioArtifactPublisher {
        void publish(String message, File artifact);
    }
}
