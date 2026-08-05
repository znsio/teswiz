package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.tools.OsUtils;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.znsio.teswiz.runner.DeviceSetup.getCloudNameFromCapabilities;
import static com.znsio.teswiz.runner.Setup.*;

class CustomReports {
    private static final Logger LOGGER = LogManager.getLogger(CustomReports.class.getName());
    private static final Map<String, String> PROVIDER_ARTIFACT_METADATA_KEYS = Map.of(
            "providerSessionId", "SESSION_PROVIDER_SESSION_IDS",
            "providerReportUrl", "SESSION_PROVIDER_REPORT_URLS",
            "providerConsoleLogsUrl", "SESSION_PROVIDER_CONSOLE_LOG_URLS",
            "providerNetworkLogsUrl", "SESSION_PROVIDER_NETWORK_LOG_URLS",
            "providerVideoUrl", "SESSION_PROVIDER_VIDEO_URLS",
            "providerPlaywrightLogsUrl", "SESSION_PROVIDER_PLAYWRIGHT_LOG_URLS",
            "providerCommandLogsUrl", "SESSION_PROVIDER_COMMAND_LOG_URLS",
            "providerScreenshotUrl", "SESSION_PROVIDER_SCREENSHOT_URLS");

    private CustomReports() {
        LOGGER.debug("CustomReports - private constructor");
    }

    static Reportable generateReport() {
        String reportsDir = Setup.getFromConfigs(LOG_DIR) + File.separator + REPORTS_DIR;
        LOGGER.info(
                "================================================================================================");
        LOGGER.info(String.format("Generating reports here: '%s'", reportsDir));
        LOGGER.info(
                "================================================================================================");
        List<String> jsonPaths = processTestResultJsonFiles(reportsDir);

        Configuration config = createCucumberReportsConfiguration(reportsDir);

        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        Reportable overviewReport = reportBuilder.generateReports();
        String generatedReportsMessage = String.format(
                "Reports available here: file://%s/cucumber-html-reports/overview-features.html",
                config.getReportDirectory().getAbsolutePath());
        LOGGER.info(generatedReportsMessage);
        return overviewReport;
    }

    @NotNull
    private static Configuration createCucumberReportsConfiguration(String reportsDir) {
        String richReportsPath = reportsDir + File.separator + "richReports";
        LOGGER.info(String.format("\tCreating rich reports: %s", richReportsPath));
        Configuration config = new Configuration(new File(richReportsPath),
                                                 Setup.getFromConfigs(APP_NAME));
        return addTestExecutionMetaDataToReportConfig(excludeCustomTagsFromReport(config), reportsDir);
    }

    private static Configuration excludeCustomTagsFromReport(Configuration config) {
        String tagsToExclude = System.getProperty(
                TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT);
        if (null != tagsToExclude) {
            config.setTagsToExcludeFromChart(tagsToExclude.trim().split(","));
        }
        return config;
    }

    @NotNull
    static List<String> processTestResultJsonFiles(String reportsDir) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"},
                                                         true);
        List<File> cucumberJsonFiles = jsonFiles.stream()
                .filter(CustomReports::isCucumberResultJsonFile)
                .sorted(Comparator.comparing(File::getAbsolutePath))
                .toList();
        LOGGER.info(String.format("\tFound '%s' Cucumber result files for processing", cucumberJsonFiles.size()));
        if (cucumberJsonFiles.isEmpty()) {
            LOGGER.info("Reports not generated");
        }
        List<String> jsonPaths = new ArrayList<>(cucumberJsonFiles.size());
        cucumberJsonFiles.forEach(file -> {
            LOGGER.info(String.format("\tProcessing result file: %s", file.getAbsolutePath()));
            jsonPaths.add(file.getAbsolutePath());
        });
        return jsonPaths;
    }

    private static boolean isCucumberResultJsonFile(File file) {
        String fileName = file.getName();
        return fileName.startsWith("cucumber-") && fileName.endsWith(".json");
    }

    private static Configuration addTestExecutionMetaDataToReportConfig(Configuration config, String reportsDir) {
        HashMap testRunMetadata = buildTestRunMetadata(reportsDir);

        // Convert hashmap entries to a list
        List<Map.Entry<String, Integer>> sortedTestMetaDataKeys = new ArrayList<>(testRunMetadata.entrySet());

        // Sort the list by keys
        Collections.sort(sortedTestMetaDataKeys, (Comparator<Map.Entry<String, Integer>>) (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        LOGGER.info("Added test execution metadata to cucumber reports");
        for (Map.Entry<String, Integer> testMetadataItem : sortedTestMetaDataKeys) {
            LOGGER.info("\t: " + testMetadataItem.getKey() + " : " + testMetadataItem.getValue());
            config.addClassifications(testMetadataItem.getKey(), String.valueOf(testMetadataItem.getValue()));
        }

        return config;
    }

    static HashMap<String, Object> buildTestRunMetadata(String reportsDir) {
        HashMap<String, Object> testRunMetadata = new HashMap<>();
        testRunMetadata.put(TARGET_ENVIRONMENT, Setup.getFromConfigs(TARGET_ENVIRONMENT));
        testRunMetadata.put(PLATFORM, Setup.getFromConfigs(PLATFORM));
        testRunMetadata.put(WEB_ENGINE, Setup.getFromConfigs(WEB_ENGINE));
        testRunMetadata.put(TAG, Setup.getFromConfigs(TAG_FOR_REPORTPORTAL));
        testRunMetadata.put(RUN_IN_CI, Setup.getBooleanValueAsStringFromConfigs(RUN_IN_CI));
        testRunMetadata.put("CLOUD_NAME", getCloudNameFromCapabilities());
        testRunMetadata.put(EXECUTED_ON, Setup.getFromConfigs(EXECUTED_ON));
        testRunMetadata.put(IS_VISUAL, Setup.getBooleanValueAsStringFromConfigs(IS_VISUAL));
        testRunMetadata.put(SET_HARD_GATE, Setup.getBooleanValueAsStringFromConfigs(SET_HARD_GATE));
        testRunMetadata.put(IS_FAILING_TEST_SUITE, Setup.getBooleanValueAsStringFromConfigs(IS_FAILING_TEST_SUITE));
        testRunMetadata.put(PARALLEL, Setup.getIntegerValueFromConfigs(PARALLEL));
        testRunMetadata.put("OS", System.getProperty("os.name"));
        testRunMetadata.put(HOST_NAME, Setup.getHostMachineName());
        testRunMetadata.put(BUILD_ID, Setup.getFromConfigs(BUILD_ID));
        testRunMetadata.put(BUILD_INITIATION_REASON, Setup.getFromConfigs(BUILD_INITIATION_REASON));
        testRunMetadata.putAll(buildAggregatedSessionMetadata(reportsDir));
        return testRunMetadata;
    }

    private static Map<String, String> buildAggregatedSessionMetadata(String reportsDir) {
        if (null == reportsDir || reportsDir.isBlank()) {
            return Collections.emptyMap();
        }
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"}, true);
        SortedSet<String> personas = new TreeSet<>();
        SortedSet<String> platforms = new TreeSet<>();
        SortedSet<String> engines = new TreeSet<>();
        SortedSet<String> providers = new TreeSet<>();
        Map<String, SortedSet<String>> providerArtifactMetadata = createProviderArtifactMetadataAccumulator();
        jsonFiles.stream()
                .filter(CustomReports::isScenarioSessionMetadataFile)
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
