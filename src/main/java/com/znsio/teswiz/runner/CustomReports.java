package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.TestExecutionFailedException;
import com.znsio.teswiz.tools.JsonFile;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.znsio.teswiz.runner.DeviceSetup.getCloudNameFromCapabilities;
import static com.znsio.teswiz.runner.Setup.*;

class CustomReports {
    private static final Logger LOGGER = LogManager.getLogger(CustomReports.class.getName());

    private CustomReports() {
        LOGGER.debug("CustomReports - private constructor");
    }

    static Reportable generateReport() {
        String reportsDir = Runner.USER_DIRECTORY + File.separator + Setup.getFromConfigs(
                LOG_DIR) + File.separator + REPORTS_DIR;
        LOGGER.info(
                "================================================================================================");
        LOGGER.info(String.format("Generating reports here: '%s'", reportsDir));
        LOGGER.info(
                "================================================================================================");
        List<String> jsonPaths = processTestResultJsonFiles(reportsDir);

        Configuration config = createCucumberReportsConfiguration(reportsDir);

        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        Reportable overviewReport = reportBuilder.generateReports();
        if (null == overviewReport) {
            String errorMessage = "Could not generate reports. See these file for details:";
            for (String jsonPath : jsonPaths) {
                errorMessage += "\n\t" + jsonPath;
            }
            throw new TestExecutionFailedException(errorMessage);
        }
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
        return addTestExecutionMetaDataToReportConfig(excludeCustomTagsFromReport(config));
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
    private static List<String> processTestResultJsonFiles(String reportsDir) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"},
                                                         true);
        LOGGER.info(String.format("\tFound '%s' result files for processing", jsonFiles.size()));
        if (jsonFiles.isEmpty()) {
            LOGGER.info("Reports not generated");
        }
        List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
        jsonFiles.forEach(file -> {
            LOGGER.info(String.format("\tProcessing result file: %s", file.getAbsolutePath()));
            jsonPaths.add(file.getAbsolutePath());
        });
        return jsonPaths;
    }

    private static Configuration addTestExecutionMetaDataToReportConfig(Configuration config) {
        HashMap testRunMetadata = new HashMap<>();
        testRunMetadata.put(TARGET_ENVIRONMENT, Setup.getFromConfigs(TARGET_ENVIRONMENT));
        testRunMetadata.put(PLATFORM, Setup.getFromConfigs(PLATFORM));
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
}
