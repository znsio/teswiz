package com.znsio.teswiz.reporting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ScenarioSessionMetadataAggregator {
    private static final Logger LOGGER = LogManager.getLogger(ScenarioSessionMetadataAggregator.class.getName());
    private static final Map<String, String> PROVIDER_ARTIFACT_METADATA_KEYS = Map.of(
            "providerSessionId", "SESSION_PROVIDER_SESSION_IDS",
            "providerReportUrl", "SESSION_PROVIDER_REPORT_URLS",
            "providerConsoleLogsUrl", "SESSION_PROVIDER_CONSOLE_LOG_URLS",
            "providerNetworkLogsUrl", "SESSION_PROVIDER_NETWORK_LOG_URLS",
            "providerVideoUrl", "SESSION_PROVIDER_VIDEO_URLS",
            "providerPlaywrightLogsUrl", "SESSION_PROVIDER_PLAYWRIGHT_LOG_URLS",
            "providerCommandLogsUrl", "SESSION_PROVIDER_COMMAND_LOG_URLS",
            "providerScreenshotUrl", "SESSION_PROVIDER_SCREENSHOT_URLS");

    private ScenarioSessionMetadataAggregator() {
    }

    public static Map<String, String> aggregate(String reportsDir) {
        if (null == reportsDir || reportsDir.isBlank()) {
            return Collections.emptyMap();
        }
        var jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[] { "json" }, true);
        SortedSet<String> personas = new TreeSet<>();
        SortedSet<String> platforms = new TreeSet<>();
        SortedSet<String> engines = new TreeSet<>();
        SortedSet<String> providers = new TreeSet<>();
        Map<String, SortedSet<String>> providerArtifactMetadata = createProviderArtifactMetadataAccumulator();
        jsonFiles.stream()
                .filter(ScenarioSessionMetadataAggregator::isScenarioSessionMetadataFile)
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .forEach(file -> addSessionMetadataFrom(file, personas, platforms, engines, providers,
                        providerArtifactMetadata));

        Map<String, String> aggregatedMetadata = new LinkedHashMap<>();
        addJoinedMetadata(aggregatedMetadata, "SESSION_PERSONAS", personas);
        addJoinedMetadata(aggregatedMetadata, "SESSION_PLATFORMS", platforms);
        addJoinedMetadata(aggregatedMetadata, "SESSION_ENGINES", engines);
        addJoinedMetadata(aggregatedMetadata, "SESSION_PROVIDERS", providers);
        addProviderArtifactMetadata(aggregatedMetadata, providerArtifactMetadata);
        return aggregatedMetadata;
    }

    private static boolean isScenarioSessionMetadataFile(File file) {
        return "scenario-session-metadata.json".equals(file.getName());
    }

    private static void addSessionMetadataFrom(File file, Set<String> personas, Set<String> platforms,
            Set<String> engines, Set<String> providers, Map<String, SortedSet<String>> providerArtifactMetadata) {
        try {
            JSONObject metadata = new JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            addJsonArrayValues(metadata.optJSONArray("personas"), personas);
            addJsonArrayValues(metadata.optJSONArray("platforms"), platforms);
            addJsonArrayValues(metadata.optJSONArray("engines"), engines);
            addJsonArrayValues(metadata.optJSONArray("providers"), providers);
            addSessionDetails(metadata.optJSONArray("sessions"), providerArtifactMetadata);
        } catch (IOException e) {
            LOGGER.warn("Unable to read scenario session metadata file: {}", file.getAbsolutePath(), e);
        }
    }

    private static void addSessionDetails(JSONArray sessions, Map<String, SortedSet<String>> providerArtifactMetadata) {
        if (null == sessions) {
            return;
        }
        for (int index = 0; index < sessions.length(); index++) {
            JSONObject session = sessions.optJSONObject(index);
            if (null == session) {
                continue;
            }
            JSONObject metadata = session.optJSONObject("metadata");
            if (null == metadata) {
                continue;
            }
            addProviderArtifactMetadata(metadata, providerArtifactMetadata);
        }
    }

    private static Map<String, SortedSet<String>> createProviderArtifactMetadataAccumulator() {
        Map<String, SortedSet<String>> accumulator = new LinkedHashMap<>();
        for (String metadataKey : PROVIDER_ARTIFACT_METADATA_KEYS.keySet()) {
            accumulator.put(metadataKey, new TreeSet<>());
        }
        return accumulator;
    }

    private static void addProviderArtifactMetadata(JSONObject sessionMetadata,
            Map<String, SortedSet<String>> providerArtifactMetadata) {
        for (Map.Entry<String, String> entry : PROVIDER_ARTIFACT_METADATA_KEYS.entrySet()) {
            addJsonValue(sessionMetadata, entry.getKey(), providerArtifactMetadata.get(entry.getKey()));
        }
    }

    private static void addProviderArtifactMetadata(Map<String, String> aggregatedMetadata,
            Map<String, SortedSet<String>> providerArtifactMetadata) {
        for (Map.Entry<String, String> entry : PROVIDER_ARTIFACT_METADATA_KEYS.entrySet()) {
            addJoinedMetadata(aggregatedMetadata, entry.getValue(), providerArtifactMetadata.get(entry.getKey()));
        }
    }

    private static void addJsonArrayValues(JSONArray values, Set<String> target) {
        if (null == values) {
            return;
        }
        for (int index = 0; index < values.length(); index++) {
            addValue(values.optString(index, ""), target);
        }
    }

    private static void addJsonValue(JSONObject source, String key, Set<String> target) {
        addValue(source.optString(key, ""), target);
    }

    private static void addValue(String value, Set<String> target) {
        String normalizedValue = value.trim();
        if (!normalizedValue.isBlank()) {
            target.add(normalizedValue);
        }
    }

    private static void addJoinedMetadata(Map<String, String> metadata, String key, SortedSet<String> values) {
        if (values.isEmpty()) {
            return;
        }
        metadata.put(key, String.join(", ", values));
    }
}
