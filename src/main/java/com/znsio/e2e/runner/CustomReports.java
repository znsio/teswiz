package com.znsio.e2e.runner;

import com.znsio.e2e.entities.TEST_CONTEXT;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.znsio.e2e.runner.Setup.*;

class CustomReports {
    private static final Logger LOGGER = Logger.getLogger(CustomReports.class.getName());

    private CustomReports() {
        LOGGER.debug("CustomReports - private constructor");
    }

    static String generateReport(String reportsDir) {
        LOGGER.info("================================");
        LOGGER.info("Generating reports");
        LOGGER.info("================================");
        Collection<File> jsonFiles = FileUtils.listFiles(new File(reportsDir), new String[]{"json"},
                                                         true);
        LOGGER.info(String.format("\tFound '%s' result files for processing", jsonFiles.size()));
        if(jsonFiles.isEmpty()) {
            return "Reports not generated";
        }
        List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
        jsonFiles.forEach(file -> {
            LOGGER.info("\tProcessing result file: " + file.getAbsolutePath());
            jsonPaths.add(file.getAbsolutePath());
        });
        String richReportsPath = reportsDir + File.separator + "richReports";
        LOGGER.info("\tCreating rich reports: " + richReportsPath);
        Configuration config = new Configuration(new File(richReportsPath),
                                                 Setup.getFromConfigs(APP_NAME));

        String tagsToExclude = System.getProperty(
                TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT);
        if(null != tagsToExclude) {
            config.setTagsToExcludeFromChart(tagsToExclude.trim().split(","));
        }
        addClassifications(config);

        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
        return "Reports available here: " + config.getReportDirectory()
                                                  .getAbsolutePath() + "/cucumber-html-reports" + "/overview-features.html";
    }

    private static void addClassifications(Configuration config) {
        config.addClassifications("Environment", Setup.getFromConfigs(TARGET_ENVIRONMENT));
        config.addClassifications("Platform", Setup.getFromConfigs(PLATFORM));
        config.addClassifications("Tags", Setup.getFromConfigs(TAG));
        config.addClassifications("RUN_IN_CI", Setup.getBooleanValueAsStringFromConfigs(RUN_IN_CI));
        config.addClassifications("IS_VISUAL", Setup.getBooleanValueAsStringFromConfigs(IS_VISUAL));
        config.addClassifications("CLOUD_NAME", Setup.getFromConfigs(CLOUD_NAME));
        config.addClassifications("EXECUTED_ON", Setup.getFromConfigs(EXECUTED_ON));
    }
}
