package com.znsio.teswiz.reporting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final String SCENARIO_SUMMARY_FILE_NAME = "scenario-session-summary.txt";
    private static final List<String> VISUAL_ARTIFACT_FILE_NAMES = List.of(
            "applitools-web.log",
            "applitools-app.log",
            "applitools-pdf.log");
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
        Path summaryFile = writeScenarioSummary(context, userPersonaDetails);
        publishArtifact("Scenario session metadata", metadataFile.toFile(), artifactPublisher);
        publishArtifact("Scenario session summary", summaryFile.toFile(), artifactPublisher);
        for (Path artifact : discoverVisualArtifacts(context)) {
            publishArtifact("Visual artifact: " + artifact.getFileName(), artifact.toFile(), artifactPublisher);
        }
        for (SessionPlaywrightArtifact artifact : discoverPlaywrightArtifacts(userPersonaDetails)) {
            publishArtifact("Playwright artifact: " + artifact.path().getFileName(), artifact.sessionHandle(),
                    artifact.path().toFile(), artifactPublisher);
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
        Path scenarioLogDirectory = getScenarioLogDirectory(context);
        Path metadataFile = scenarioLogDirectory.resolve(SCENARIO_METADATA_FILE_NAME);
        try {
            Files.writeString(metadataFile, buildScenarioMetadata(context, userPersonaDetails).toString(2),
                    StandardCharsets.UTF_8);
            return metadataFile;
        } catch (IOException e) {
            throw new RuntimeException("Unable to write scenario metadata artifact: " + metadataFile, e);
        }
    }

    static Path writeScenarioSummary(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        Path scenarioLogDirectory = getScenarioLogDirectory(context);
        Path summaryFile = scenarioLogDirectory.resolve(SCENARIO_SUMMARY_FILE_NAME);
        try {
            Files.writeString(summaryFile, buildScenarioSummary(context, userPersonaDetails), StandardCharsets.UTF_8);
            return summaryFile;
        } catch (IOException e) {
            throw new RuntimeException("Unable to write scenario summary artifact: " + summaryFile, e);
        }
    }

    private static Path getScenarioLogDirectory(TestExecutionContext context) {
        String scenarioLogDirectory = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        if (null == scenarioLogDirectory || scenarioLogDirectory.isBlank()) {
            throw new IllegalStateException("Scenario log directory is required to publish scenario artifacts");
        }
        return Path.of(scenarioLogDirectory);
    }

    private static JSONObject buildScenarioMetadata(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        JSONArray sessions = new JSONArray();
        Set<String> personas = new LinkedHashSet<>();
        Set<String> platforms = new LinkedHashSet<>();
        Set<String> engines = new LinkedHashSet<>();
        Set<String> providers = new LinkedHashSet<>();
        for (Map.Entry<String, SessionHandle> entry : userPersonaDetails.getAllAssignedUserPersonasAndSessionHandles().entrySet()) {
            SessionHandle sessionHandle = entry.getValue();
            personas.add(sessionHandle.userPersona());
            platforms.add(sessionHandle.platform().name());
            engines.add(sessionHandle.engine());
            String provider = sessionHandle.metadata().get("provider");
            if (null != provider && !provider.isBlank()) {
                providers.add(provider);
            }
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
                .put("personas", new JSONArray(personas))
                .put("platforms", new JSONArray(platforms))
                .put("engines", new JSONArray(engines))
                .put("providers", new JSONArray(providers.isEmpty() ? List.of(getProvider()) : providers))
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

    private static String buildScenarioSummary(TestExecutionContext context, UserPersonaDetails userPersonaDetails) {
        StringBuilder summary = new StringBuilder();
        summary.append("Scenario: ").append(context.getTestName()).append(System.lineSeparator());
        summary.append("NormalisedScenario: ")
                .append(context.getTestStateAsString(TEST_CONTEXT.NORMALISED_SCENARIO_NAME))
                .append(System.lineSeparator());
        summary.append("ScenarioRunCount: ").append(context.getTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT))
                .append(System.lineSeparator());
        summary.append("Provider: ").append(getProvider()).append(System.lineSeparator());
        summary.append("WebEngine: ").append(getWebEngine()).append(System.lineSeparator());
        summary.append(System.lineSeparator());
        for (SessionHandle sessionHandle : userPersonaDetails.getAllAssignedUserPersonasAndSessionHandles().values()) {
            summary.append("Persona: ").append(sessionHandle.userPersona()).append(System.lineSeparator());
            summary.append("Platform: ").append(sessionHandle.platform().name()).append(System.lineSeparator());
            summary.append("Engine: ").append(sessionHandle.engine()).append(System.lineSeparator());
            summary.append("Provider: ").append(getSessionProvider(sessionHandle)).append(System.lineSeparator());
            summary.append("SessionId: ").append(sessionHandle.sessionId()).append(System.lineSeparator());
            summary.append("ArtifactPath: ").append(sessionHandle.artifactPath()).append(System.lineSeparator());
            for (Map.Entry<String, String> metadataEntry : sessionHandle.metadata().entrySet()) {
                summary.append(metadataEntry.getKey()).append(": ").append(metadataEntry.getValue())
                        .append(System.lineSeparator());
            }
            summary.append(System.lineSeparator());
        }
        return summary.toString();
    }

    static List<Path> discoverPlaywrightArtifactsForTest(UserPersonaDetails userPersonaDetails) {
        return discoverPlaywrightArtifacts(userPersonaDetails).stream().map(SessionPlaywrightArtifact::path).toList();
    }

    static List<Path> discoverVisualArtifacts(TestExecutionContext context) {
        Path deviceLogsDirectory = getScenarioLogDirectory(context).resolve("deviceLogs");
        if (!Files.isDirectory(deviceLogsDirectory)) {
            return List.of();
        }
        List<Path> artifacts = new ArrayList<>();
        for (String fileName : VISUAL_ARTIFACT_FILE_NAMES) {
            Path artifact = deviceLogsDirectory.resolve(fileName);
            if (Files.exists(artifact)) {
                artifacts.add(artifact);
            }
        }
        artifacts.sort(Comparator.naturalOrder());
        return artifacts;
    }

    static List<SessionPlaywrightArtifact> discoverPlaywrightArtifacts(UserPersonaDetails userPersonaDetails) {
        List<SessionPlaywrightArtifact> artifacts = new ArrayList<>();
        for (SessionHandle sessionHandle : userPersonaDetails.getAllAssignedUserPersonasAndSessionHandles().values()) {
            if (!isPlaywrightEngine(sessionHandle.engine())) {
                continue;
            }
            Path artifactDirectory = Path.of(sessionHandle.artifactPath());
            String artifactPrefix = sessionHandle.userPersona() + "-" + sessionHandle.sessionId();
            for (String artifactSuffix : PLAYWRIGHT_ARTIFACT_SUFFIXES) {
                Path artifact = artifactDirectory.resolve(artifactPrefix + artifactSuffix);
                if (Files.exists(artifact)) {
                    artifacts.add(new SessionPlaywrightArtifact(sessionHandle, artifact));
                }
            }
        }
        artifacts.sort(Comparator.comparing(sessionArtifact -> sessionArtifact.path().toString()));
        LOGGER.info("Discovered '{}' Playwright reporting artifacts", artifacts.size());
        return artifacts;
    }

    private static boolean isPlaywrightEngine(String engine) {
        return "playwright-ts".equalsIgnoreCase(engine) || "playwright-java".equalsIgnoreCase(engine);
    }

    private static String getSessionProvider(SessionHandle sessionHandle) {
        String provider = sessionHandle.metadata().get("provider");
        if (null == provider || provider.isBlank()) {
            return getProvider();
        }
        return provider;
    }

    private static String describeSession(SessionHandle sessionHandle) {
        return "persona=" + sessionHandle.userPersona()
                + ", platform=" + sessionHandle.platform().name()
                + ", engine=" + sessionHandle.engine()
                + ", provider=" + getSessionProvider(sessionHandle)
                + ", sessionId=" + sessionHandle.sessionId();
    }

    private static void publishArtifact(String label, SessionHandle sessionHandle, File artifact,
            ScenarioArtifactPublisher artifactPublisher) {
        publishArtifact(label + " [" + describeSession(sessionHandle) + "]", artifact, artifactPublisher);
    }

    @FunctionalInterface
    interface ScenarioArtifactPublisher {
        void publish(String message, File artifact);
    }

    record SessionPlaywrightArtifact(SessionHandle sessionHandle, Path path) {
    }
}
