package com.znsio.teswiz.runner;

import com.znsio.teswiz.entities.TEST_CONTEXT;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.znsio.teswiz.runner.Setup.*;

class CustomReports {
    private static final Logger LOGGER = Logger.getLogger(CustomReports.class.getName());

    private CustomReports() {
        LOGGER.debug("CustomReports - private constructor");
    }

    static void generateReport() {
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
        reportBuilder.generateReports();
        String generatedReportsMessage = "Reports available here: " + config.getReportDirectory()
                                                                            .getAbsolutePath() +
                                         "/cucumber-html" + "-reports" + "/overview-features.html";
        LOGGER.info(generatedReportsMessage);
    }

    @NotNull
    private static Configuration createCucumberReportsConfiguration(String reportsDir) {
        String richReportsPath = reportsDir + File.separator + "richReports";
        LOGGER.info("\tCreating rich reports: " + richReportsPath);
        Configuration config = new Configuration(new File(richReportsPath),
                                                 Setup.getFromConfigs(APP_NAME));
        return addTestExecutionMetaDataToReportConfig(excludeCustomTagsFromReport(config));
    }

    private static Configuration excludeCustomTagsFromReport(Configuration config) {
        String tagsToExclude = System.getProperty(
                TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT);
        if(null != tagsToExclude) {
            config.setTagsToExcludeFromChart(tagsToExclude.trim().split(","));
        }
        return config;
    }

    @NotNull
    private static List<String> processTestResultJsonFiles(String reportsDir) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"},
                                                         true);
        LOGGER.info(String.format("\tFound '%s' result files for processing", jsonFiles.size()));
        if(jsonFiles.isEmpty()) {
            LOGGER.info("Reports not generated");
        }
        List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
        jsonFiles.forEach(file -> {
            LOGGER.info("\tProcessing result file: " + file.getAbsolutePath());
            jsonPaths.add(file.getAbsolutePath());
        });
        return jsonPaths;
    }

    private static Configuration addTestExecutionMetaDataToReportConfig(Configuration config) {
        config.addClassifications("Environment", Setup.getFromConfigs(TARGET_ENVIRONMENT));
        config.addClassifications("Platform", Setup.getFromConfigs(PLATFORM));
        config.addClassifications("Tags", Setup.getFromConfigs(TAG));
        config.addClassifications("RUN_IN_CI", Setup.getBooleanValueAsStringFromConfigs(RUN_IN_CI));
        config.addClassifications("IS_VISUAL", Setup.getBooleanValueAsStringFromConfigs(IS_VISUAL));
        config.addClassifications("CLOUD_NAME", Setup.getFromConfigs(CLOUD_NAME));
        config.addClassifications("EXECUTED_ON", Setup.getFromConfigs(EXECUTED_ON));
        return config;
    }
}
