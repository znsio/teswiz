package com.znsio.teswiz.testng;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

// Feeds synthetic Cucumber-JSON built from TestNG scenario data into masterthought's
// cucumber-reporting ReportBuilder, producing the same rich HTML report (Bootstrap
// navbar, tag charts, collapsible step trees) that Cucumber mode's CustomReports
// already generates for real Cucumber runs.
public final class TestNgCucumberStyleReportWriter {
    private static final String CUCUMBER_JSON_FILE_NAME = "testng-cucumber-style-report.json";

    private TestNgCucumberStyleReportWriter() { }

    public static void write(List<TestNgScenarioReportData> scenarios, File outputDirectory, String appName) {
        JSONArray cucumberJson = TestNgCucumberJsonBuilder.build(scenarios);
        File jsonFile = new File(outputDirectory, CUCUMBER_JSON_FILE_NAME);
        writeJsonFile(cucumberJson, jsonFile);

        Configuration config = new Configuration(outputDirectory, appName);
        ReportBuilder reportBuilder = new ReportBuilder(List.of(jsonFile.getAbsolutePath()), config);
        reportBuilder.generateReports();
    }

    private static void writeJsonFile(JSONArray cucumberJson, File jsonFile) {
        try {
            Files.writeString(jsonFile.toPath(), cucumberJson.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidTestDataException("Unable to write TestNG cucumber-style report JSON to: " + jsonFile, e);
        }
    }
}
