package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.Setup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgCucumberStyleReportWriterTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldGenerateRichHtmlReportFromScenarioData(@TempDir File outputDirectory) throws Exception {
        TestNgScenarioReportData scenario = new TestNgScenarioReportData(
                "SampleTestNgTest", "sampleScenarioName", List.of("smoke"),
                TestNgCapturedStep.PASSED, 42L,
                List.of(new TestNgCapturedStep("SampleBL.doSomething", "com.znsio.teswiz.businessLayer.SampleBL.doSomething()",
                        0, TestNgCapturedStep.PASSED, 10_000_000L)));

        TestNgCucumberStyleReportWriter.write(List.of(scenario), outputDirectory, "SampleApp");

        File overviewFeatures = new File(outputDirectory, "cucumber-html-reports/overview-features.html");
        assertThat(overviewFeatures).exists();
        String content = Files.readString(overviewFeatures.toPath());
        assertThat(content).contains("SampleTestNgTest");

        File featureReport = findGeneratedFeatureReportFile(outputDirectory);
        String featureReportContent = Files.readString(featureReport.toPath());
        assertThat(featureReportContent).contains("sampleScenarioName");
        assertThat(featureReportContent).contains("SampleBL.doSomething");

        assertThat(content)
                .as("the Features overview page should carry test-execution metadata, same as Cucumber mode's report")
                .contains(Setup.PLATFORM)
                .contains(Setup.getFromConfigs(Setup.PLATFORM))
                .contains(Setup.TARGET_ENVIRONMENT)
                .contains(Setup.HOST_NAME);
    }

    private File findGeneratedFeatureReportFile(File outputDirectory) {
        File reportsDir = new File(outputDirectory, "cucumber-html-reports");
        File[] featureReports = reportsDir.listFiles((dir, name) -> name.startsWith("report-feature_"));
        assertThat(featureReports).isNotNull().isNotEmpty();
        return featureReports[0];
    }
}
